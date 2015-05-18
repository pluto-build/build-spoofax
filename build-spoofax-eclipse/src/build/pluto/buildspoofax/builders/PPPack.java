package build.pluto.buildspoofax.builders;


import java.io.File;
import java.io.IOException;

import org.strategoxt.tools.main_parse_pp_table_0_0;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.BuildUnit.State;
import build.pluto.buildspoofax.LoggingFilteringIOAgent;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.output.None;

public class PPPack extends SpoofaxBuilder<PPPack.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, PPPack> factory = new SpoofaxBuilderFactory<Input, None, PPPack>() {
		private static final long serialVersionUID = 7367043797398412114L;

		@Override
		public PPPack makeBuilder(Input input) { return new PPPack(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -5786344696509159033L;

		public final RelativePath ppInput;
		public final RelativePath ppTermOutput;
		/** If true, produce empty table in case `ppInput` does not exist. */
		public final boolean fallback;
		public Input(SpoofaxContext context, RelativePath ppInput, RelativePath ppTermOutput) {
			this(context, ppInput, ppTermOutput, false);
		}
		public Input(SpoofaxContext context, RelativePath ppInput, RelativePath ppTermOutput, boolean fallback) {
			super(context);
			this.ppInput = ppInput;
			this.ppTermOutput = ppTermOutput;
			this.fallback = fallback;
		}
	}
	
	public PPPack(Input input) {
		super(input);
	}
	
	@Override
	protected String description() {
		return "Compress pretty-print table";
	}
	
	@Override
	protected Path persistentPath() {
		RelativePath rel = FileCommands.getRelativePath(context.baseDir, input.ppTermOutput);
		String relname = rel.getRelativePath().replace(File.separatorChar, '_');
		return context.depPath("ppPack." + relname + ".dep");
	}

	@Override
	public None build() throws IOException {
		if (!context.isBuildStrategoEnabled(this))
			return None.val;
		
		requireBuild(PackSdf.factory, new PackSdf.Input(context));
		
		require(input.ppInput);
		if (input.fallback && !FileCommands.exists(input.ppInput)) {
			FileCommands.writeToFile(input.ppTermOutput, "PP-Table([])");
			provide(input.ppTermOutput);
		}
		else {
			ExecutionResult er = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
					main_parse_pp_table_0_0.instance, "parse-pp-table", new LoggingFilteringIOAgent(),
						"-i", input.ppInput,
						"-o", input.ppTermOutput);
			provide(input.ppTermOutput);
			setState(State.finished(er.success));
		}
		
		return None.val;
	}
}
