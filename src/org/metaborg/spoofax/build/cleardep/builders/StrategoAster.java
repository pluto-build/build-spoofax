package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.List;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class StrategoAster extends Builder<SpoofaxBuildContext, StrategoAster.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, StrategoAster> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, StrategoAster>() {
		@Override
		public StrategoAster makeBuilder(SpoofaxBuildContext context) { return new StrategoAster(context); }
	};
	
	public static class Input {
		public final String strmodule;
		public Input(String strmodule) {
			this.strmodule = strmodule;
		}
	}
	
	public StrategoAster(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Input input) {
		return "Compile attribute grammar to stratego.";
	}
	
	@Override
	public Path persistentPath(Input input) {
		return context.depPath("strategoAster." + input.strmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		List<RelativePath> asterInputList = FileCommands.listFilesRecursive(context.baseDir, new FileExtensionFilter("astr"));
		for (RelativePath p : asterInputList)
			result.addSourceArtifact(p);
//		String asterInput = StringCommands.printListSeparated(asterInputList, " ");
//		RelativePath outputPath = context.basePath("${trans}/" + input.strmodule + ".rtree");
		
		// TODO Aster compiler not available
//		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.asterContext, 
//				org.strategoxt.aster.Main.instance, "aster", new LoggingFilteringIOAgent(), 
//				"-i", asterInput);

//		result.addGeneratedFile(outputPath);
//		result.setState(State.finished(er.success));
	}
}
