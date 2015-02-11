package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.permissivegrammars.make_permissive;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.CompilationUnit.State;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class MakePermissive extends Builder<SpoofaxBuildContext, MakePermissive.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, MakePermissive> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, MakePermissive>() {
		@Override
		public MakePermissive makeBuilder(SpoofaxBuildContext context) { return new MakePermissive(context); }
	};
	
	public static class Input {
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(String sdfmodule, String buildSdfImports) {
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	public MakePermissive(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	public Path persistentPath(Input input) {
		return context.basePath("${include}/build.makePermissive." + input.sdfmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		Log.log.beginInlineTask("Make grammar permissive for error-recovery parsing.", Log.CORE); 
		
		CompilationUnit packSdf = context.packSdf.require(new PackSdf.Input(input.sdfmodule, input.buildSdfImports), new SimpleMode());
		result.addModuleDependency(packSdf);
		
		if (context.props.isDefined("externaldef"))
			copySdf(result, input);
		
		RelativePath inputPath = context.basePath("${include.rel}/" + input.sdfmodule + ".def");
		RelativePath outputPath = context.basePath("${include.rel}/" + input.sdfmodule + "-Permissive.def");
		
		result.addSourceArtifact(inputPath);
		ExecutionResult er = StrategoExecutor.runStrategoCLI(context.permissiveGrammarsContext(), 
				make_permissive.getMainStrategy(), "make-permissive", new LoggingFilteringIOAgent(Pattern.quote("[ make-permissive | info ]") + ".*"),
				"-i", inputPath,
				"-o", outputPath,
				"--optimize", "on"
				);
		result.addGeneratedFile(outputPath);
		result.setState(State.finished(er.success));
		
		Log.log.endTask();
	}

	private void copySdf(SimpleCompilationUnit result, Input input) throws IOException {
		// XXX need to `preservelastmodified`?
		Path source = new AbsolutePath(context.props.get("externaldef"));
		Path target = context.basePath("${include}/" + input.sdfmodule + ".def");
		result.addExternalFileDependency(source);
		FileCommands.copyFile(source, target);
		result.addGeneratedFile(target);
	}

}
