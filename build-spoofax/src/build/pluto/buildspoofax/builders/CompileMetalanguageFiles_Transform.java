package build.pluto.buildspoofax.builders;

import java.io.File;

import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.transform.CompileGoal;
import org.metaborg.spoofax.core.transform.ITransformer;
import org.metaborg.spoofax.core.transform.TransformResult;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.StrategoExecutor;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class CompileMetalanguageFiles_Transform extends SpoofaxBuilder<CompileMetalanguageFiles_Transform.Input, TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm>> {
	private static final TypeLiteral<ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm>> TRANSFORM_LITERAL = new TypeLiteral<ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm>>(){};

	public static SpoofaxBuilderFactory<Input, TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm>, CompileMetalanguageFiles_Transform> factory = new SpoofaxBuilderFactory<Input, TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm>, CompileMetalanguageFiles_Transform>() {
		private static final long serialVersionUID = 4436143308769039647L;

		@Override
		public CompileMetalanguageFiles_Transform makeBuilder(Input input) { return new CompileMetalanguageFiles_Transform(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 37855003667874400L;

		public final IContext langContext;
		public final AnalysisFileResult<IStrategoTerm, IStrategoTerm> analysisResult;
		public Input(SpoofaxContext context, IContext langContext, AnalysisFileResult<IStrategoTerm, IStrategoTerm> analysisResult) {
			super(context);
			this.langContext = langContext;
			this.analysisResult = analysisResult;
		}
	}
	
	public CompileMetalanguageFiles_Transform(Input input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Transform metalanguage file " + input.analysisResult.source();
	}
	
	@Override
	protected Path persistentPath() {
		RelativePath rel = FileCommands.getRelativePath(context.baseDir, new AbsolutePath(input.analysisResult.source().getName().getPath()));
		String relname = rel.getRelativePath().replace(File.separatorChar, '_');
		return context.depPath("meta/transform." + relname + ".dep");
	}

	
	@Override
	public TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm> build() throws Exception {
		Injector injector = StrategoExecutor.guiceInjector();
		ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformer = injector.getInstance(Key.get(TRANSFORM_LITERAL));
		
		require(new AbsolutePath(input.analysisResult.source().getName().getPath()));
		
		TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm> result = transformer.transform(input.analysisResult, input.langContext, new CompileGoal());
		return result;
	}
}
