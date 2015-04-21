package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.analysis.AnalysisResult;
import org.metaborg.spoofax.core.analysis.IAnalysisService;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.StrategoExecutor;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class CompileMetalanguageFiles_Analyze extends SpoofaxBuilder<CompileMetalanguageFiles_Analyze.Input, AnalysisResult<IStrategoTerm, IStrategoTerm>> {
	private static final TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>> ANALYSIS_LITERAL = new TypeLiteral<IAnalysisService<IStrategoTerm, IStrategoTerm>>(){};

	public static SpoofaxBuilderFactory<Input, AnalysisResult<IStrategoTerm, IStrategoTerm>, CompileMetalanguageFiles_Analyze> factory = new SpoofaxBuilderFactory<Input, AnalysisResult<IStrategoTerm, IStrategoTerm>, CompileMetalanguageFiles_Analyze>() {
		private static final long serialVersionUID = 4436143308769039647L;

		@Override
		public CompileMetalanguageFiles_Analyze makeBuilder(Input input) { return new CompileMetalanguageFiles_Analyze(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 37855003667874400L;

		public final IContext langContext;
		public final Collection<ParseResult<IStrategoTerm>> parseResults;
		public Input(SpoofaxContext context, IContext langContext, Collection<ParseResult<IStrategoTerm>> parseResults) {
			super(context);
			this.langContext = langContext;
			this.parseResults = parseResults instanceof Serializable ? parseResults : Lists.newArrayList(parseResults);
		}
	}
	
	public CompileMetalanguageFiles_Analyze(Input input) {
		super(input);
	}

	@Override
	protected String description() {
		if (input.parseResults.size() == 1) {
			return "Analyze metalanguage file " + input.parseResults.iterator().next().source().getName().getPath();
		}
		else
			return "Analyze " + input.parseResults.size() + " metalanguage files";
	}
	
	@Override
	protected Path persistentPath() {
		if (input.parseResults.size() == 1) {
			FileObject fo = input.parseResults.iterator().next().source();
			Path file = new AbsolutePath(fo.getName().getPath());
			RelativePath rel = FileCommands.getRelativePath(context.baseDir, file);
			String relname = rel.getRelativePath().replace(File.separatorChar, '_');
			return context.depPath("meta/analyze." + relname + ".dep");
		}
		else
			return context.depPath("meta/analyze." + input.parseResults.hashCode() + ".dep");
	}

	
	@Override
	public AnalysisResult<IStrategoTerm, IStrategoTerm> build() throws Exception {
		Injector injector = StrategoExecutor.guiceInjector();
		IAnalysisService<IStrategoTerm, IStrategoTerm> analysisService = injector.getInstance(Key.get(ANALYSIS_LITERAL));
		
		for (ParseResult<IStrategoTerm> pres : input.parseResults)
			require(new AbsolutePath(pres.source().getName().getPath()));
		
		AnalysisResult<IStrategoTerm, IStrategoTerm> result = analysisService.analyze(input.parseResults, input.langContext);
		return result;
	}
}
