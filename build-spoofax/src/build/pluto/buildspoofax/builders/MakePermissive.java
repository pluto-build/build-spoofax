package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.util.file.FileUtils;
import org.strategoxt.permissivegrammars.make_permissive;

import build.pluto.BuildUnit.State;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.buildspoofax.util.LoggingFilteringIOAgent;
import build.pluto.output.None;

public class MakePermissive extends SpoofaxBuilder<MakePermissive.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, MakePermissive> factory = SpoofaxBuilderFactory.of(MakePermissive.class, Input.class);
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 4381601872931676757L;

		public final String sdfModule;
		public final String sdfArgs;
		
		public Input(SpoofaxContext context, String sdfModule, String sdfArgs) {
			super(context);
            this.sdfModule = sdfModule;
            this.sdfArgs = sdfArgs;
		}
	}
	
	public MakePermissive(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Make grammar permissive for error-recovery parsing.";
	}
	
	@Override
	public File persistentPath(Input input) {
		return context.depPath("makePermissive." + context.settings.sdfName() + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		requireBuild(CopySdf.factory, new CopySdf.Input(context, input.sdfModule));
		requireBuild(PackSdf.factory, new PackSdf.Input(context, input.sdfModule, input.sdfArgs));
		
		File inputPath = FileUtils.toFile(context.settings.getSdfCompiledDefFile(input.sdfModule));
		File outputPath = FileUtils.toFile(context.settings.getSdfCompiledPermissiveDefFile(input.sdfModule));
		
		require(inputPath);
		ExecutionResult er = StrategoExecutor.runStrategoCLI(StrategoExecutor.permissiveGrammarsContext(), 
				make_permissive.getMainStrategy(), "make-permissive", new LoggingFilteringIOAgent(Pattern.quote("[ make-permissive | info ]") + ".*"),
				"-i", inputPath,
				"-o", outputPath,
				"--optimize", "on"
				);
		provide(outputPath);
		setState(State.finished(er.success));
		return None.val;
	}
}
