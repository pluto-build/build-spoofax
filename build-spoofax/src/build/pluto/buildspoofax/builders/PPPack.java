package build.pluto.buildspoofax.builders;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.strategoxt.tools.main_parse_pp_table_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.BuildUnit.State;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.buildspoofax.util.LoggingFilteringIOAgent;
import build.pluto.output.None;

import com.google.common.base.Joiner;

public class PPPack extends SpoofaxBuilder<PPPack.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, PPPack> factory = SpoofaxBuilderFactory.of(PPPack.class, Input.class);
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -5786344696509159033L;

		public final File ppInput;
		public final File ppTermOutput;
		/** If true, produce empty table in case `ppInput` does not exist. */
		public final boolean fallback;

		public Input(SpoofaxContext context, File ppInput, File ppTermOutput) {
			this(context, ppInput, ppTermOutput, false);
		}

		public Input(SpoofaxContext context, File ppInput, File ppTermOutput, boolean fallback) {
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
	protected String description(Input input) {
		return "Compress pretty-print table";
	}
	
	@Override
	protected File persistentPath(Input input) {
		Path rel = FileCommands.getRelativePath(context.baseDir, input.ppTermOutput);
		String relname = rel.toString().replace(File.separatorChar, '_');
		return context.depPath("ppPack." + relname + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		if (!context.isBuildStrategoEnabled(this))
			return None.val;
		
		requireBuild(PackSdf.factory, new PackSdf.Input(context, context.settings.sdfName(), Joiner.on(' ').join(context.settings.sdfArgs())));
		
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
