package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

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

		public final String sdfmodule;
		public final String buildSdfImports;
		public final File externaldef;

		public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports, File externaldef) {
			super(context);
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
			this.externaldef = externaldef;
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
		return context.depPath("makePermissive." + input.sdfmodule + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		requireBuild(CopySdf.factory, new CopySdf.Input(context, input.sdfmodule, input.externaldef));
		requireBuild(PackSdf.factory, new PackSdf.Input(context,input.sdfmodule, input.buildSdfImports));
		
		File inputPath = context.basePath("${include}/" + input.sdfmodule + ".def");
		File outputPath = context.basePath("${include}/" + input.sdfmodule + "-Permissive.def");
		
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
