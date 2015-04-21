package build.pluto.buildspoofax.builders;

import java.io.File;
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
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.StrategoExecutor;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class CompileMetalanguageFiles_Analyze extends SpoofaxBuilder<CompileMetalanguageFiles_Analyze.Input, HashMap<Path, IStrategoTerm>> {
	private static final TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>> ANALYSIS_LITERAL = new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>(){};

	public static SpoofaxBuilderFactory<Input, HashMap<Path, IStrategoTerm>, CompileMetalanguageFiles_Analyze> factory = new SpoofaxBuilderFactory<Input, HashMap<Path, IStrategoTerm>, CompileMetalanguageFiles_Analyze>() {
		private static final long serialVersionUID = 4436143308769039647L;

		@Override
		public CompileMetalanguageFiles_Analyze makeBuilder(Input input) { return new CompileMetalanguageFiles_Analyze(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 37855003667874400L;

		public final IContext langContext;
		public final Map<Path, IStrategoTerm> parseResults;
		public Input(SpoofaxContext context, IContext langContext, Map<Path, IStrategoTerm> parseResults) {
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
	protected String description() {
	  if (input.parseResults.size() == 1)
	    return "Analyze " + input.langName() + " file " + FileCommands.getRelativePath(context.baseDir, input.parseResults.keySet().iterator().next()).getRelativePath();
		
	  return "Analyze " + input.parseResults.size() + " " + input.langName() + " files";
	}
	
	@Override
	protected Path persistentPath() {
	  if (input.parseResults.size() == 1) {
	    RelativePath rel = FileCommands.getRelativePath(context.baseDir, input.parseResults.keySet().iterator().next());
	    String relname = rel.getRelativePath().replace(File.separatorChar, '_');
	    return context.depPath("meta/analyze." + input.langName() + "." + relname + ".dep");
	  }
	  
		return context.depPath("meta/analyze." + input.langName() + "." + input.parseResults.hashCode() + ".dep");
	}

	
	@Override
	public HashMap<Path, IStrategoTerm> build() throws Exception {
		Injector injector = StrategoExecutor.guiceInjector();
		IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService = injector.getInstance(Key.get(ANALYSIS_LITERAL));
		IResourceService resourceService = StrategoExecutor.getResourceService();
		
		Map<FileObject, Path> fileMap = new HashMap<>();
		List<ParseResult<IStrategoTerm>> analysisInput = new ArrayList<>();
		for (Entry<Path, IStrategoTerm> e : input.parseResults.entrySet()) {
		  FileObject source = resourceService.resolve(e.getKey().getFile());
		  fileMap.put(source, e.getKey());
		  ParseResult<IStrategoTerm> pres = new ParseResult<>(e.getValue(), source, Collections.emptyList(), -1, input.langContext.language(), null);
		  analysisInput.add(pres);
		}
		
		AnalysisResult<IStrategoTerm, IStrategoTerm> analysisResult = analysisService.analyze(analysisInput, input.langContext);
		
		HashMap<Path, IStrategoTerm> result = new HashMap<>();
		for (AnalysisFileResult<IStrategoTerm, IStrategoTerm> res : analysisResult.fileResults)
		  result.put(fileMap.get(res.source()), res.result);
		
		return result;
	}
}
