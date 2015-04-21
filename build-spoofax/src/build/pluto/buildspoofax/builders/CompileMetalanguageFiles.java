package build.pluto.buildspoofax.builders;

import java.io.IOException;
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
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;
import org.sugarj.common.util.Pair;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.builders.aux.DiscoverSpoofaxLanguage;
import build.pluto.buildspoofax.util.PatternFileFilter;
import build.pluto.output.None;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class CompileMetalanguageFiles extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, CompileMetalanguageFiles> factory = new SpoofaxBuilderFactory<SpoofaxInput, None, CompileMetalanguageFiles>() {
		private static final long serialVersionUID = 4436143308769039647L;

		@Override
		public CompileMetalanguageFiles makeBuilder(SpoofaxInput input) { return new CompileMetalanguageFiles(input); }
	};
	
	public CompileMetalanguageFiles(SpoofaxInput input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Compile metalanguage files";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("metalang.compile.dep");
	}

	@Override
	public None build() throws Exception {
		// XXX really need to delete old sdf3 files? Or is it sufficient to remove them from `paths` below?
		List<RelativePath> oldSdf3Paths = FileCommands.listFilesRecursive(context.basePath("src-gen"), new SuffixFileFilter("sdf3"));
		for (Path p : oldSdf3Paths)
			FileCommands.delete(p);
		
		List<ILanguage> metalangs = loadMetalanguages();
		Map<String, ILanguage> metalangsByExtension = getMetalanguageExtensions(metalangs);
		
		Path include = context.basePath("${include}");
		List<RelativePath> paths = FileCommands.listFilesRecursive(
				context.baseDir,
				new AndFileFilter(Lists.newArrayList(
						new NotFileFilter(DirectoryFileFilter.INSTANCE),
						new NotFileFilter(new PatternFileFilter(true, Pattern.quote(include.getAbsolutePath()) + ".*")),
						new SuffixFileFilter(metalangsByExtension.keySet().toArray(new String[0])))));
		
		Map<Path, Pair<ILanguage, IStrategoTerm>> parseResults = parseFiles(metalangsByExtension, paths);
		Map<IContext, Map<Path, IStrategoTerm>> analysisResults = analyzeFiles(parseResults);
		@SuppressWarnings("unused")
		Map<Path, IStrategoTerm> compilerResults = transformFiles(parseResults, analysisResults);
		
		return None.val;
	}

	private List<ILanguage> loadMetalanguages() throws IOException {
		Class<?> sdf3Class = org.strategoxt.imp.editors.template.strategies.InteropRegisterer.class;
		ILanguage sdf3 = requireBuild(DiscoverSpoofaxLanguage.factory, new DiscoverSpoofaxLanguage.Input(context, sdf3Class));
		
		Class<?> nablClass = org.metaborg.meta.lang.nabl.strategies.InteropRegisterer.class;
		ILanguage nabl = requireBuild(DiscoverSpoofaxLanguage.factory, new DiscoverSpoofaxLanguage.Input(context, nablClass));
		
		Class<?> tsClass = org.metaborg.meta.lang.ts.strategies.InteropRegisterer.class;
		ILanguage ts = requireBuild(DiscoverSpoofaxLanguage.factory, new DiscoverSpoofaxLanguage.Input(context, tsClass));
		
		return Lists.newArrayList(sdf3, nabl, ts);
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
	
	private Map<Path, Pair<ILanguage, IStrategoTerm>> parseFiles(Map<String, ILanguage> metalangsByExtension, List<RelativePath> paths) throws IOException {
		Map<Path, Pair<ILanguage, IStrategoTerm>> parseResults = new HashMap<>();
		for (RelativePath p : paths) {
			ILanguage lang = metalangsByExtension.get(FileCommands.getExtension(p));
			IStrategoTerm parseResult = requireBuild(CompileMetalanguageFiles_Parse.factory, new CompileMetalanguageFiles_Parse.Input(context, p, lang));
			parseResults.put(p, new Pair<>(lang, parseResult));
		}
		return parseResults;
	}

	private Map<IContext, Map<Path, IStrategoTerm>> analyzeFiles(Map<Path, Pair<ILanguage, IStrategoTerm>> parseResults) throws ContextException, IOException {
		IResourceService resourceService = StrategoExecutor.getResourceService();
		IContextService contextService = StrategoExecutor.guiceInjector().getInstance(IContextService.class);

		Multimap<IContext, Pair<Path, IStrategoTerm>> parseResultsByContext = ArrayListMultimap.create();
        for(Entry<Path, Pair<ILanguage, IStrategoTerm>> e : parseResults.entrySet()) {
        	FileObject source = resourceService.resolve(e.getKey().getFile());
            IContext context = contextService.get(source, e.getValue().a);
            parseResultsByContext.put(context, new Pair<>(e.getKey(), e.getValue().b));
        }
        
        // TODO better separation of analysis tasks possible? AnalysisMode:Single/Multi
        Map<IContext, Map<Path, IStrategoTerm>> analysisResults = Maps.newHashMapWithExpectedSize(parseResultsByContext.keySet().size());
        for(Entry<IContext, Collection<Pair<Path, IStrategoTerm>>> e : parseResultsByContext.asMap().entrySet()) {
            Map<Path, IStrategoTerm> analysisResult =
            		requireBuild(CompileMetalanguageFiles_Analyze.factory, new CompileMetalanguageFiles_Analyze.Input(context, e.getKey(), Pair.asMap(e.getValue())));
            analysisResults.put(e.getKey(), analysisResult);
        }
        
        return analysisResults;
	}

	private Map<Path, IStrategoTerm> transformFiles(
			Map<Path, Pair<ILanguage, IStrategoTerm>> parseResults, 
			Map<IContext, Map<Path, IStrategoTerm>> analysisResults) throws IOException {

		Map<Path, IStrategoTerm> compileResults = new HashMap<>();
		
		for (Entry<IContext, Map<Path, IStrategoTerm>> e : analysisResults.entrySet()) {
			IContext context = e.getKey();
			for (Entry<Path, IStrategoTerm> fileRes : e.getValue().entrySet()) {
				Path p = fileRes.getKey();
				IStrategoTerm parseResult = parseResults.get(p).b;
				IStrategoTerm analysisResult = fileRes.getValue();
				IStrategoTerm result = requireBuild(CompileMetalanguageFiles_Transform.factory, new CompileMetalanguageFiles_Transform.Input(this.context, p, context, parseResult, analysisResult));
				compileResults.put(p, result);
			}
		}
		
		return compileResults;
	}

}
