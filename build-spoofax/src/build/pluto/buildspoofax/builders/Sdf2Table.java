package build.pluto.buildspoofax.builders;

import java.io.IOException;

import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.BuildUnit.State;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;

public class Sdf2Table extends SpoofaxBuilder<Sdf2Table.Input, Path> {


	public static SpoofaxBuilderFactory<Input, Path, Sdf2Table> factory = new SpoofaxBuilderFactory<Input, Path, Sdf2Table>() {
		private static final long serialVersionUID = -5551917492018980172L;

		@Override
		public Sdf2Table makeBuilder(Input input) { return new Sdf2Table(input); }
	};
	

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -2379365089609792204L;
		public final String sdfmodule;
		public final String buildSdfImports;
		public final Path externaldef;
		public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports, Path externaldef) {
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
	protected String description() {
		return "Compile grammar to parse table";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("sdf2Table." + input.sdfmodule + ".dep");
	}

	@Override
	public Path build() throws IOException {
		requireBuild(MakePermissive.factory, new MakePermissive.Input(context, input.sdfmodule, input.buildSdfImports, input.externaldef));
		Path sdf2table = requireBuild(Sdf2TablePrepareExecutable.factory, input);
		
		RelativePath inputPath = context.basePath("${include}/" + input.sdfmodule + "-Permissive.def");
		RelativePath outputPath = context.basePath("${include}/" + input.sdfmodule + ".tbl");

		require(inputPath);
		ExecutionResult er = StrategoExecutor.runSdf2TableCLI(sdf2table, 
				"-t",
				"-i", inputPath,
				"-m", input.sdfmodule,
				"-o", outputPath);
		
		provide(outputPath);
		setState(State.finished(er.success));
		return outputPath;
	}

}
