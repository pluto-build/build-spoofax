package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;

import build.pluto.BuildUnit.State;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.buildspoofax.builders.aux.Sdf2TablePrepareExecutable;
import build.pluto.output.OutputPersisted;

public class Sdf2Table extends SpoofaxBuilder<Sdf2Table.Input, OutputPersisted<File>> {


	public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, Sdf2Table> factory = SpoofaxBuilderFactory.of(Sdf2Table.class, Input.class);
	

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -2379365089609792204L;
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
	
	public Sdf2Table(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Compile grammar to parse table";
	}
	
	@Override
	protected File persistentPath(Input input) {
		return context.depPath("sdf2Table." + input.sdfmodule + ".dep");
	}

	@Override
	public OutputPersisted<File> build(Input input) throws IOException {
		requireBuild(MakePermissive.factory, new MakePermissive.Input(context, input.sdfmodule, input.buildSdfImports, input.externaldef));
		Sdf2TablePrepareExecutable.Output commands = requireBuild(Sdf2TablePrepareExecutable.factory, input);
		
		File inputPath = context.basePath("${include}/" + input.sdfmodule + "-Permissive.def");
		File outputPath = context.basePath("${include}/" + input.sdfmodule + ".tbl");

		require(inputPath);
		ExecutionResult er = commands.sdf2table.run( 
				"-t",
				"-i", inputPath.getAbsolutePath(),
				"-m", input.sdfmodule,
				"-o", outputPath.getAbsolutePath());
		
		provide(outputPath);
		setState(State.finished(er.success));
		return OutputPersisted.of(outputPath);
	}

}
