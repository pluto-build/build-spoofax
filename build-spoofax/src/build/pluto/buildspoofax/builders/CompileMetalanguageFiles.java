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
import java.util.stream.Collectors;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.metaborg.spoofax.core.context.ContextException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ResourceExtensionFacet;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.FileCommands;
import org.sugarj.common.util.Pair;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.builders.aux.DiscoverSpoofaxLanguage;
import build.pluto.buildspoofax.builders.aux.DiscoverSpoofaxLanguage.DiscoverSpoofaxLanguageRequest;
import build.pluto.buildspoofax.util.PatternFileFilter;
import build.pluto.output.None;
import build.pluto.output.Out;
import build.pluto.output.OutputHashStamper;
import build.pluto.output.OutputPersisted;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class CompileMetalanguageFiles extends SpoofaxBuilder<CompileMetalanguageFiles.Input, None> {

	public final static String[] DEFAULT_LANGUAGES = {"TemplateLang", "NameBindingLanguage", "TypeSystemLanguage"};
	
	public static SpoofaxBuilderFactory<Input, None, CompileMetalanguageFiles> factory = SpoofaxBuilderFactory.of(CompileMetalanguageFiles.class, Input.class);
	
	public static class Input extends SpoofaxInput {

		private static final long serialVersionUID = 8233923229608629326L;
		
		public final String[] languageNames;
		
		public Input(SpoofaxContext context) {
			this(context, DEFAULT_LANGUAGES);
		}
		
		public Input(SpoofaxContext context, String[] languageNames) {
			super(context);
			this.languageNames = languageNames;
		}
	}
	
	public CompileMetalanguageFiles(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Compile metalanguage files";
	}
	
	@Override
	protected File persistentPath(Input input) {
		return context.depPath("metalang.compile.dep");
	}

	@Override
	public None build(Input input) throws Exception {
		// XXX really need to delete old sdf3 files? Or is it sufficient to remove them from `paths` below?
		List<Path> oldSdf3Paths = FileCommands.listFilesRecursive(context.basePath("src-gen").toPath(), new SuffixFileFilter("sdf3"));
		for (Path p : oldSdf3Paths)
			FileCommands.delete(p);
		
		Map<ILanguage, DiscoverSpoofaxLanguageRequest> metalangs = loadMetalanguages(input.languageNames);
		Map<String, Entry<ILanguage, DiscoverSpoofaxLanguageRequest>> metalangsByExtension = getMetalanguageExtensions(metalangs);
		
		File include = context.basePath("${include}");
		List<Path> paths = FileCommands.listFilesRecursive(
				context.baseDir.toPath(),
				new AndFileFilter(Lists.newArrayList(
						new NotFileFilter(DirectoryFileFilter.INSTANCE),
						new NotFileFilter(new PatternFileFilter(true, Pattern.quote(include.getAbsolutePath()) + ".*")),
						new SuffixFileFilter(metalangsByExtension.keySet().toArray(new String[0])))));
		List<File> files = paths.stream().map(x -> x.toFile()).collect(Collectors.toList());
		
		Multimap<Entry<ILanguage, DiscoverSpoofaxLanguageRequest>, Pair<File, IStrategoTerm>> parseResults = parseFiles(metalangsByExtension, files);
		Map<Entry<ILanguage, DiscoverSpoofaxLanguageRequest>, Map<File, Pair<IStrategoTerm, IStrategoTerm>>> analysisResults = analyzeFiles(parseResults);
		@SuppressWarnings("unused")
		Map<File, IStrategoTerm> compilerResults = transformFiles(analysisResults);
		
		return None.val;
	}

	private Map<ILanguage, DiscoverSpoofaxLanguageRequest> loadMetalanguages(String[] languageNames) throws IOException {
		Map<ILanguage, DiscoverSpoofaxLanguageRequest> langs = new HashMap<>();
		
		for (String langName : languageNames) {
			DiscoverSpoofaxLanguageRequest langReq = new DiscoverSpoofaxLanguageRequest(DiscoverSpoofaxLanguage.factory, new DiscoverSpoofaxLanguage.Input(context, langName));
			Out<ILanguage> lang = requireBuild(langReq);
			langs.put(lang.val(), langReq);
		}
		
		return langs;
	}
	
	private Map<String, Entry<ILanguage, DiscoverSpoofaxLanguageRequest>> getMetalanguageExtensions(Map<ILanguage, DiscoverSpoofaxLanguageRequest> metalangs) {
		Map<String, Entry<ILanguage, DiscoverSpoofaxLanguageRequest>> extensions = new HashMap<>(metalangs.size());
		
		for (Entry<ILanguage, DiscoverSpoofaxLanguageRequest> lang : metalangs.entrySet()) {
			ResourceExtensionFacet facet = lang.getKey().facet(ResourceExtensionFacet.class);
			if (facet != null)
				for (String ext : facet.extensions())
					extensions.put(ext, lang);
		}
		
		return extensions;
	}
	
	private Multimap<Entry<ILanguage, DiscoverSpoofaxLanguageRequest>, Pair<File, IStrategoTerm>> parseFiles(Map<String, Entry<ILanguage, DiscoverSpoofaxLanguageRequest>> metalangsByExtension, List<File> paths) throws IOException {
		Multimap<Entry<ILanguage, DiscoverSpoofaxLanguageRequest>, Pair<File, IStrategoTerm>> parseResults = ArrayListMultimap.create();
		for (File p : paths) {
			Entry<ILanguage, DiscoverSpoofaxLanguageRequest> lang = metalangsByExtension.get(FileCommands.getExtension(p));
			CompileMetalanguageFiles_Parse.Input parseInput = new CompileMetalanguageFiles_Parse.Input(context, p, lang.getKey().name(), lang.getValue());
			OutputPersisted<IStrategoTerm> parseResult = requireBuild(CompileMetalanguageFiles_Parse.factory, parseInput, OutputHashStamper.instance());
			parseResults.put(lang, new Pair<>(p, parseResult.val));
		}
		return parseResults;
	}

	private Map<Entry<ILanguage, DiscoverSpoofaxLanguageRequest>, Map<File, Pair<IStrategoTerm, IStrategoTerm>>> analyzeFiles(Multimap<Entry<ILanguage, DiscoverSpoofaxLanguageRequest>, Pair<File, IStrategoTerm>> parseResults) throws ContextException, IOException {
        // TODO better separation of analysis tasks possible? AnalysisMode:Single/Multi
		Map<Entry<ILanguage, DiscoverSpoofaxLanguageRequest>, Map<File, Pair<IStrategoTerm, IStrategoTerm>>> analysisResults = Maps.newHashMapWithExpectedSize(parseResults.keySet().size());
		for (Entry<Entry<ILanguage, DiscoverSpoofaxLanguageRequest>, Collection<Pair<File, IStrategoTerm>>> e : parseResults.asMap().entrySet()) {
			Entry<ILanguage, DiscoverSpoofaxLanguageRequest> lang = e.getKey();
			CompileMetalanguageFiles_Analyze.Input analzeInput = new CompileMetalanguageFiles_Analyze.Input(context, lang.getKey().name(), lang.getValue(), Pair.asMap(e.getValue()));
			OutputPersisted<HashMap<File, Pair<IStrategoTerm, IStrategoTerm>>> analysisResult = requireBuild(CompileMetalanguageFiles_Analyze.factory, analzeInput, OutputHashStamper.instance());
			analysisResults.put(lang, analysisResult.val);
        }
        
        return analysisResults;
	}

	private Map<File, IStrategoTerm> transformFiles(Map<Entry<ILanguage, DiscoverSpoofaxLanguageRequest>, Map<File, Pair<IStrategoTerm, IStrategoTerm>>> analysisResults) throws IOException {

		Map<File, IStrategoTerm> compileResults = new HashMap<>();
		
		for (Entry<Entry<ILanguage, DiscoverSpoofaxLanguageRequest>, Map<File, Pair<IStrategoTerm, IStrategoTerm>>> e : analysisResults.entrySet()) {
			Entry<ILanguage, DiscoverSpoofaxLanguageRequest> lang = e.getKey();
			for (Entry<File, Pair<IStrategoTerm, IStrategoTerm>> fileRes : e.getValue().entrySet()) {
				File p = fileRes.getKey();
				IStrategoTerm parseResult = fileRes.getValue().a;
				IStrategoTerm analysisResult = fileRes.getValue().b;
				CompileMetalanguageFiles_Transform.Input transformInput = new CompileMetalanguageFiles_Transform.Input(this.context, p, lang.getKey().name(), lang.getValue(), parseResult, analysisResult);
				Out<IStrategoTerm> result = requireBuild(CompileMetalanguageFiles_Transform.factory, transformInput, OutputHashStamper.instance());
				compileResults.put(p, result.val());
			}
		}
		
		return compileResults;
	}

}
