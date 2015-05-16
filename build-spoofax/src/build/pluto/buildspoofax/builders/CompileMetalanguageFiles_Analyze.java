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
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.FileCommands;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.Out;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class CompileMetalanguageFiles_Analyze extends SpoofaxBuilder<CompileMetalanguageFiles_Analyze.Input, Out<HashMap<File, IStrategoTerm>>> {
	private static final TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>> ANALYSIS_LITERAL = new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>() {
	};

	public static SpoofaxBuilderFactory<Input, Out<HashMap<File, IStrategoTerm>>, CompileMetalanguageFiles_Analyze> factory = CompileMetalanguageFiles_Analyze::new;

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 37855003667874400L;

		public final IContext langContext;
		public final Map<File, IStrategoTerm> parseResults;

		public Input(SpoofaxContext context, IContext langContext, Map<File, IStrategoTerm> parseResults) {
			super(context);
			this.langContext = langContext;
			this.parseResults = parseResults;
		}

		public String langName() {
			return langContext.language().name();
		}
	}

	public CompileMetalanguageFiles_Analyze(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		if (input.parseResults.size() == 1)
			return "Analyze " + input.langName() + " file "
					+ FileCommands.getRelativePath(context.baseDir, input.parseResults.keySet().iterator().next()).toString();

		return "Analyze " + input.parseResults.size() + " " + input.langName() + " files";
	}

	@Override
	protected File persistentPath(Input input) {
		if (input.parseResults.size() == 1) {
			Path rel = FileCommands.getRelativePath(context.baseDir, input.parseResults.keySet().iterator().next());
			String relname = rel.toString().replace(File.separatorChar, '_');
			return context.depPath("meta/analyze." + input.langName() + "." + relname + ".dep");
		}

		return context.depPath("meta/analyze." + input.langName() + "." + input.parseResults.hashCode() + ".dep");
	}

	@Override
	public Out<HashMap<File, IStrategoTerm>> build(Input input) throws Exception {
		Injector injector = context.guiceInjector();
		IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService = injector.getInstance(Key.get(ANALYSIS_LITERAL));
		IResourceService resourceService = context.getResourceService();

		Map<FileObject, File> fileMap = new HashMap<>();
		List<ParseResult<IStrategoTerm>> analysisInput = new ArrayList<>();
		for (Entry<File, IStrategoTerm> e : input.parseResults.entrySet()) {
			FileObject source = resourceService.resolve(e.getKey());
			fileMap.put(source, e.getKey());
			ParseResult<IStrategoTerm> pres = new ParseResult<>(e.getValue(), source, Collections.emptyList(), -1, input.langContext.language(), null);
			analysisInput.add(pres);
		}

		AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult = analysisService.analyze(analysisInput, input.langContext);

		HashMap<File, IStrategoTerm> result = new HashMap<>();
		for (AnalysisFileResult<IStrategoTerm, IStrategoTerm> res : analysisResult.fileResults)
			result.put(fileMap.get(res.source), res.result);

		return Out.of(result);
	}
}
