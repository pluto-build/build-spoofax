package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.sugarj.common.FileCommands;
import org.sugarj.common.StringCommands;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;

public class StrategoAster extends SpoofaxBuilder<StrategoAster.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, StrategoAster> factory = new SpoofaxBuilderFactory<Input, None, StrategoAster>() {
		private static final long serialVersionUID = -1290903435504555665L;

		@Override
		public StrategoAster makeBuilder(Input input) { return new StrategoAster(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -4593910056510380042L;
		
		public final String strmodule;
		public Input(SpoofaxContext context, String strmodule) {
			super(context);
			this.strmodule = strmodule;
		}
	}
	
	public StrategoAster(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Compile attribute grammar to Stratego";
	}
	
	@Override
	public File persistentPath(Input input) {
		return context.depPath("strategoAster." + input.strmodule + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		List<Path> asterInputList = FileCommands.listFilesRecursive(context.baseDir.toPath(), new SuffixFileFilter("astr"));
		for (Path p : asterInputList)
			require(p.toFile());
		
		String asterInput = StringCommands.printListSeparated(asterInputList, " ");
		File outputPath = context.basePath("${trans}/" + input.strmodule + ".rtree");
		
		// TODO Aster compiler not available
//		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.asterContext, 
//				org.strategoxt.aster.Main.instance, "aster", new LoggingFilteringIOAgent(), 
//				"-i", asterInput);

		provide(outputPath);
//		setState(State.finished(er.success));
		
		return None.val;
	}
}
