package build.pluto.buildspoofax.builders;

import java.io.IOException;
import java.util.ArrayList;
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
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.context.ContextException;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ResourceExtensionFacet;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.transform.TransformResult;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

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
		
		List<ParseResult<IStrategoTerm>> parseResults = parseFiles(metalangsByExtension, paths);
		Map<IContext, AnalysisResult<IStrategoTerm, IStrategoTerm>> analysisResults = analyzeFiles(parseResults);
		@SuppressWarnings("unused")
		List<TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm>> compilerResults = transformFiles(analysisResults);
		
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
	
	private List<ParseResult<IStrategoTerm>> parseFiles(Map<String, ILanguage> metalangsByExtension, List<RelativePath> paths) throws IOException {
		List<ParseResult<IStrategoTerm>> parseResults = new ArrayList<>();
		for (RelativePath p : paths) {
			ILanguage lang = metalangsByExtension.get(FileCommands.getExtension(p));
			ParseResult<IStrategoTerm> parseResult = 
					requireBuild(CompileMetalanguageFiles_Parse.factory, new CompileMetalanguageFiles_Parse.Input(context, p, lang));
			parseResults.add(parseResult);
		}
		return parseResults;
	}

	private Map<IContext, AnalysisResult<IStrategoTerm, IStrategoTerm>> analyzeFiles(List<ParseResult<IStrategoTerm>> parseResults) throws ContextException, IOException {
		IContextService contextService = StrategoExecutor.guiceInjector().getInstance(IContextService.class);

		Multimap<IContext, ParseResult<IStrategoTerm>> parseResultsByContext = ArrayListMultimap.create();
        for(ParseResult<IStrategoTerm> parseResult : parseResults) {
            IContext context = contextService.get(parseResult.source(), parseResult.language);
            parseResultsByContext.put(context, parseResult);
        }
        
        // TODO better separation of analysis tasks possible? AnalysisMode:Single/Multi
        Map<IContext, AnalysisResult<IStrategoTerm, IStrategoTerm>> analysisResults = Maps.newHashMapWithExpectedSize(parseResultsByContext.keySet().size());
        for(Entry<IContext, Collection<ParseResult<IStrategoTerm>>> entry : parseResultsByContext.asMap().entrySet()) {
            AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult =
            		requireBuild(CompileMetalanguageFiles_Analyze.factory, new CompileMetalanguageFiles_Analyze.Input(context, entry.getKey(), entry.getValue()));
            analysisResults.put(entry.getKey(), analysisResult);
        }
        
        return analysisResults;
	}

	private List<TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm>> 
		transformFiles(Map<IContext, AnalysisResult<IStrategoTerm, IStrategoTerm>> analysisResults) throws IOException {

		List<TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm>> compileResults = new ArrayList<>();
		
		for (Entry<IContext, AnalysisResult<IStrategoTerm, IStrategoTerm>> e : analysisResults.entrySet()) {
			IContext context = e.getKey();
			for (AnalysisFileResult<IStrategoTerm, IStrategoTerm> fileRes : e.getValue().fileResults) {
				TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm> result = 
						requireBuild(CompileMetalanguageFiles_Transform.factory, new CompileMetalanguageFiles_Transform.Input(this.context, context, fileRes));
				compileResults.add(result);
			}
		}
		
		return compileResults;
	}

}
