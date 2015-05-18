package build.pluto.buildspoofax.builders;

import java.io.File;
import java.nio.file.Path;
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

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.Out;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class CompileMetalanguageFiles_Transform extends SpoofaxBuilder<CompileMetalanguageFiles_Transform.Input, Out<IStrategoTerm>> {
	private static final TypeLiteral<ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm>> TRANSFORM_LITERAL = new TypeLiteral<ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm>>(){};

	public static SpoofaxBuilderFactory<Input, Out<IStrategoTerm>, CompileMetalanguageFiles_Transform> factory = SpoofaxBuilderFactory.of(
			CompileMetalanguageFiles_Transform.class, Input.class);
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 37855003667874400L;

		public final File file;
		public final IContext langContext;
		public final IStrategoTerm parseResult;
		public final IStrategoTerm analysisResult;

		public Input(SpoofaxContext context, File file, IContext langContext, IStrategoTerm parseResult, IStrategoTerm analysisResult) {
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
	protected String description(Input input) {
		return "Transform " + input.langName() + " file " + FileCommands.getRelativePath(context.baseDir, input.file).toString();
	}
	
	@Override
	protected File persistentPath(Input input) {
		Path rel = FileCommands.getRelativePath(context.baseDir, input.file);
		String relname = rel.toString().replace(File.separatorChar, '_');
		return context.depPath("meta/transform." + relname + ".dep");
	}

	
	@Override
	public Out<IStrategoTerm> build(Input input) throws Exception {
		Injector injector = context.guiceInjector();
		IResourceService resourceService = context.getResourceService();
		ITransformer<IStrategoTerm, IStrategoTerm, IStrategoTerm> transformer = injector.getInstance(Key.get(TRANSFORM_LITERAL));
		
		FileObject source = resourceService.resolve(input.file);
		ParseResult<IStrategoTerm> parseResult = new ParseResult<>(input.parseResult, source, Collections.emptyList(), -1, input.langContext.language(), null);
		AnalysisFileResult<IStrategoTerm, IStrategoTerm> transformInput = new AnalysisFileResult<IStrategoTerm, IStrategoTerm>(input.analysisResult, source, Collections.emptyList(), parseResult);
		
		TransformResult<AnalysisFileResult<IStrategoTerm, IStrategoTerm>, IStrategoTerm> result = transformer.transform(transformInput, input.langContext, new CompileGoal());
		return Out.of(result.result);
	}
}
