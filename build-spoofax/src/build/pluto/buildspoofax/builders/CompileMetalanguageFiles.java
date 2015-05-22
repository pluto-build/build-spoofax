package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.context.ContextException;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ResourceExtensionFacet;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.FileCommands;
import org.sugarj.common.util.Pair;

import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.builders.aux.DiscoverSpoofaxLanguage;
import build.pluto.buildspoofax.util.KryoWrapper;
import build.pluto.buildspoofax.util.PatternFileFilter;
import build.pluto.output.None;
import build.pluto.output.Out;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class CompileMetalanguageFiles extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, CompileMetalanguageFiles> factory = SpoofaxBuilderFactory.of(CompileMetalanguageFiles.class,
			SpoofaxInput.class);
	
	public CompileMetalanguageFiles(SpoofaxInput input) {
		super(input);
	}

	@Override
	protected String description(SpoofaxInput input) {
		return "Compile metalanguage files";
	}
	
	@Override
	protected File persistentPath(SpoofaxInput input) {
		return context.depPath("metalang.compile.dep");
	}

	@Override
	public None build(SpoofaxInput input) throws Exception {
		// XXX really need to delete old sdf3 files? Or is it sufficient to remove them from `paths` below?
		List<Path> oldSdf3Paths = FileCommands.listFilesRecursive(context.basePath("src-gen").toPath(), new SuffixFileFilter("sdf3"));
		for (Path p : oldSdf3Paths)
			FileCommands.delete(p);
		
		List<ILanguage> metalangs = loadMetalanguages();
		Map<String, ILanguage> metalangsByExtension = getMetalanguageExtensions(metalangs);
		
		File include = context.basePath("${include}");
		List<Path> paths = FileCommands.listFilesRecursive(
				context.baseDir.toPath(),
				new AndFileFilter(Lists.newArrayList(
						new NotFileFilter(DirectoryFileFilter.INSTANCE),
						new NotFileFilter(new PatternFileFilter(true, Pattern.quote(include.getAbsolutePath()) + ".*")),
						new SuffixFileFilter(metalangsByExtension.keySet().toArray(new String[0])))));
		
		Map<File, Pair<ILanguage, IStrategoTerm>> parseResults = parseFiles(metalangsByExtension, paths);
		Map<IContext, Map<File, IStrategoTerm>> analysisResults = analyzeFiles(parseResults);
		@SuppressWarnings("unused")
		Map<File, IStrategoTerm> compilerResults = transformFiles(parseResults, analysisResults);
		
		return None.val;
	}

	private List<ILanguage> loadMetalanguages() throws IOException {
		Class<?> sdf3Class = org.strategoxt.imp.editors.template.strategies.InteropRegisterer.class;
		Out<KryoWrapper<ILanguage>> sdf3 = requireBuild(DiscoverSpoofaxLanguage.factory, new DiscoverSpoofaxLanguage.Input(context, sdf3Class));
		
		Class<?> nablClass = org.metaborg.meta.lang.nabl.strategies.InteropRegisterer.class;

		assert !new BuildRequest<>(DiscoverSpoofaxLanguage.factory, new DiscoverSpoofaxLanguage.Input(context, sdf3Class)).equals(new BuildRequest<>(
				DiscoverSpoofaxLanguage.factory, new DiscoverSpoofaxLanguage.Input(context, nablClass)));

		Out<KryoWrapper<ILanguage>> nabl = requireBuild(DiscoverSpoofaxLanguage.factory, new DiscoverSpoofaxLanguage.Input(context, nablClass));
		
		Class<?> tsClass = org.metaborg.meta.lang.ts.strategies.InteropRegisterer.class;
		Out<KryoWrapper<ILanguage>> ts = requireBuild(DiscoverSpoofaxLanguage.factory, new DiscoverSpoofaxLanguage.Input(context, tsClass));
		
		return Lists.newArrayList(sdf3.val.get(), nabl.val.get(), ts.val.get());
	}
	
	private Map<String, ILanguage> getMetalanguageExtensions(List<ILanguage> metalangs) {
		Map<String, ILanguage> extensions = new HashMap<>(metalangs.size());
		
		for (ILanguage lang : metalangs) {
			ResourceExtensionFacet facet = lang.facet(ResourceExtensionFacet.class);
			if (facet != null)
				for (String ext : facet.extensions())
					extensions.put(ext, lang);
		}
		
		return extensions;
	}
	
	private Map<File, Pair<ILanguage, IStrategoTerm>> parseFiles(Map<String, ILanguage> metalangsByExtension, List<Path> paths) throws IOException {
		Map<File, Pair<ILanguage, IStrategoTerm>> parseResults = new HashMap<>();
		for (Path p : paths) {
			ILanguage lang = metalangsByExtension.get(FileCommands.getExtension(p.toFile()));
			Out<IStrategoTerm> parseResult = requireBuild(CompileMetalanguageFiles_Parse.factory,
					new CompileMetalanguageFiles_Parse.Input(context, p.toFile(), lang));
			parseResults.put(p.toFile(), new Pair<>(lang, parseResult.val));
		}
		return parseResults;
	}

	private Map<IContext, Map<File, IStrategoTerm>> analyzeFiles(Map<File, Pair<ILanguage, IStrategoTerm>> parseResults) throws ContextException, IOException {
		IResourceService resourceService = context.getResourceService();
		IContextService contextService = context.guiceInjector().getInstance(IContextService.class);

		Multimap<IContext, Pair<File, IStrategoTerm>> parseResultsByContext = ArrayListMultimap.create();
		for (Entry<File, Pair<ILanguage, IStrategoTerm>> e : parseResults.entrySet()) {
			FileObject source = resourceService.resolve(e.getKey());
            IContext context = contextService.get(source, e.getValue().a);
			parseResultsByContext.put(context, new Pair<>(e.getKey(), e.getValue().b));
        }
        
        // TODO better separation of analysis tasks possible? AnalysisMode:Single/Multi
		Map<IContext, Map<File, IStrategoTerm>> analysisResults = Maps.newHashMapWithExpectedSize(parseResultsByContext.keySet().size());
		for (Entry<IContext, Collection<Pair<File, IStrategoTerm>>> e : parseResultsByContext.asMap().entrySet()) {
			Out<HashMap<File, IStrategoTerm>> analysisResult =
            		requireBuild(CompileMetalanguageFiles_Analyze.factory, new CompileMetalanguageFiles_Analyze.Input(context, e.getKey(), Pair.asMap(e.getValue())));
			analysisResults.put(e.getKey(), analysisResult.val);
        }
        
        return analysisResults;
	}

	private Map<File, IStrategoTerm> transformFiles(Map<File, Pair<ILanguage, IStrategoTerm>> parseResults,
			Map<IContext, Map<File, IStrategoTerm>> analysisResults) throws IOException {

		Map<File, IStrategoTerm> compileResults = new HashMap<>();
		
		for (Entry<IContext, Map<File, IStrategoTerm>> e : analysisResults.entrySet()) {
			IContext context = e.getKey();
			for (Entry<File, IStrategoTerm> fileRes : e.getValue().entrySet()) {
				File p = fileRes.getKey();
				IStrategoTerm parseResult = parseResults.get(p).b;
				IStrategoTerm analysisResult = fileRes.getValue();
				Out<IStrategoTerm> result = requireBuild(CompileMetalanguageFiles_Transform.factory, new CompileMetalanguageFiles_Transform.Input(this.context,
						p, context, parseResult, analysisResult));
				compileResults.put(p, result.val);
			}
		}
		
		return compileResults;
	}

}
