package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.List;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.sugarj.cleardep.BuildUnit.State;
import org.sugarj.cleardep.output.None;
import org.sugarj.common.FileCommands;
import org.sugarj.common.StringCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

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
	protected String description() {
		return "Compile attribute grammar to Stratego";
	}
	
	@Override
	public Path persistentPath() {
		return context.depPath("strategoAster." + input.strmodule + ".dep");
	}

	@Override
	public None build() throws IOException {
		List<RelativePath> asterInputList = FileCommands.listFilesRecursive(context.baseDir, new FileExtensionFilter("astr"));
		for (RelativePath p : asterInputList)
			require(p);
		
		String asterInput = StringCommands.printListSeparated(asterInputList, " ");
		RelativePath outputPath = context.basePath("${trans}/" + input.strmodule + ".rtree");
		
		// TODO Aster compiler not available
//		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.asterContext, 
//				org.strategoxt.aster.Main.instance, "aster", new LoggingFilteringIOAgent(), 
//				"-i", asterInput);

		provide(outputPath);
//		setState(State.finished(er.success));
		
		return None.val;
	}
}
