package build.pluto.buildspoofax.builders;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.context.IContextService;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.FileCommands;
import org.sugarj.common.util.Pair;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.builders.aux.DiscoverSpoofaxLanguage.DiscoverSpoofaxLanguageRequest;
import build.pluto.output.OutputPersisted;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class CompileMetalanguageFiles_Analyze extends SpoofaxBuilder<CompileMetalanguageFiles_Analyze.Input, OutputPersisted<HashMap<File, Pair<IStrategoTerm, IStrategoTerm>>>> {
	private static final TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>> ANALYSIS_LITERAL = new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>() {
	};

	public static SpoofaxBuilderFactory<Input, OutputPersisted<HashMap<File, Pair<IStrategoTerm, IStrategoTerm>>>, CompileMetalanguageFiles_Analyze> factory = SpoofaxBuilderFactory.of(
			CompileMetalanguageFiles_Analyze.class, Input.class);

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 37855003667874400L;

		private final String langName;
		public final DiscoverSpoofaxLanguageRequest langDiscoverReq;
		public final Map<File, IStrategoTerm> parseResults;

		public Input(SpoofaxContext context, String langName, DiscoverSpoofaxLanguageRequest langDiscoverReq, Map<File, IStrategoTerm> parseResults) {
			super(context);
			this.langName = langName;
			this.langDiscoverReq = langDiscoverReq;
			this.parseResults = parseResults;
		}
	}

	public CompileMetalanguageFiles_Analyze(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		if (input.parseResults.size() == 1)
			return "Analyze " + input.langName + " file "
					+ FileCommands.getRelativePath(context.baseDir, input.parseResults.keySet().iterator().next()).toString();

		return "Analyze " + input.parseResults.size() + " " + input.langName + " files";
	}

	@Override
	protected File persistentPath(Input input) {
		if (input.parseResults.size() == 1) {
			Path rel = FileCommands.getRelativePath(context.baseDir, input.parseResults.keySet().iterator().next());
			String relname = rel.toString().replace(File.separatorChar, '_');
			return context.depPath("meta/analyze." + input.langName + "." + relname + ".dep");
		}

		return context.depPath("meta/analyze." + input.langName + "." + input.parseResults.hashCode() + ".dep");
	}

	@Override
	public OutputPersisted<HashMap<File, Pair<IStrategoTerm, IStrategoTerm>>> build(Input input) throws Exception {
		Injector injector = context.guiceInjector();
		IContextService contextService = injector.getInstance(IContextService.class);
		
		IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService = injector.getInstance(Key.get(ANALYSIS_LITERAL));
		IResourceService resourceService = context.getResourceService();
		ILanguage lang = requireBuild(input.langDiscoverReq).val();
		IContext langContext = contextService.get(resourceService.resolve(input.parseResults.keySet().iterator().next()), lang);
		
		Map<FileObject, File> fileMap = new HashMap<>();
		List<ParseResult<IStrategoTerm>> analysisInput = new ArrayList<>();
		for (Entry<File, IStrategoTerm> e : input.parseResults.entrySet()) {
			FileObject source = resourceService.resolve(e.getKey());
			fileMap.put(source, e.getKey());
			ParseResult<IStrategoTerm> pres = new ParseResult<>(e.getValue(), source, Collections.emptyList(), -1, lang, null);
			analysisInput.add(pres);
		}

		AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult = analysisService.analyze(analysisInput, langContext);

		HashMap<File, Pair<IStrategoTerm, IStrategoTerm>> result = new HashMap<>();
		for (AnalysisFileResult<IStrategoTerm, IStrategoTerm> res : analysisResult.fileResults) {
			if (res.result != null)
				result.put(fileMap.get(res.source), Pair.create(res.previous.result, res.result));
		}

		return OutputPersisted.of(result);
	}
}
