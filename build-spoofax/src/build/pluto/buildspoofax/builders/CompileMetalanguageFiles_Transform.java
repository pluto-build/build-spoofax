package build.pluto.buildspoofax.builders;

import java.io.File;
import java.util.Collections;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.analysis.AnalysisFileResult;
import org.metaborg.spoofax.core.context.IContext;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.metaborg.spoofax.core.transform.CompileGoal;
import org.metaborg.spoofax.core.transform.ITransformer;
import org.metaborg.spoofax.core.transform.TransformResult;
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

public class CompileMetalanguageFiles_Transform extends SpoofaxBuilder<CompileMetalanguageFiles_Transform.Input, IStrategoTerm> {
	private static final TypeLiteral<ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm>> TRANSFORM_LITERAL = new TypeLiteral<ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm>>(){};

	public static SpoofaxBuilderFactory<Input, IStrategoTerm, CompileMetalanguageFiles_Transform> factory = new SpoofaxBuilderFactory<Input, IStrategoTerm, CompileMetalanguageFiles_Transform>() {
		private static final long serialVersionUID = 4436143308769039647L;

		@Override
		public CompileMetalanguageFiles_Transform makeBuilder(Input input) { return new CompileMetalanguageFiles_Transform(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 37855003667874400L;

		public final Path file;
		public final IContext langContext;
		public final IStrategoTerm parseResult;
		public final IStrategoTerm analysisResult;
		public Input(SpoofaxContext context, Path file, IContext langContext, IStrategoTerm parseResult, IStrategoTerm analysisResult) {
			super(context);
			this.file = file;
			this.langContext = langContext;
			this.parseResult = parseResult;
			this.analysisResult = analysisResult;
		}
		
		public String langName() {
			return langContext.language().name();
		}
	}
	
	public CompileMetalanguageFiles_Transform(Input input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Transform " + input.langName() + " file " + FileCommands.getRelativePath(context.baseDir, input.file).getRelativePath();
	}
	
	@Override
	protected Path persistentPath() {
		RelativePath rel = FileCommands.getRelativePath(context.baseDir, input.file);
		String relname = rel.getRelativePath().replace(File.separatorChar, '_');
		return context.depPath("meta/transform." + relname + ".dep");
	}

	
	@Override
	public IStrategoTerm build() throws Exception {
		Injector injector = StrategoExecutor.guiceInjector();
		IResourceService resourceService = StrategoExecutor.getResourceService();
		ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformer = injector.getInstance(Key.get(TRANSFORM_LITERAL));
		
		FileObject source = resourceService.resolve(input.file.getFile());
		ParseResult<IStrategoTerm> parseResult = new ParseResult<>(input.parseResult, source, Collections.emptyList(), -1, input.langContext.language(), null);
		AnalysisFileResult<IStrategoTerm, IStrategoTerm> transformInput = new AnalysisFileResult<IStrategoTerm, IStrategoTerm>(input.analysisResult, source, Collections.emptyList(), parseResult);
		
		TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm> result = transformer.transform(transformInput, input.langContext, new CompileGoal());
		return result.result;
	}
}
