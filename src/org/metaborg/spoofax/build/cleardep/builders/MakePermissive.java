package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.strategoxt.permissivegrammars.make_permissive;
import org.sugarj.cleardep.BuildUnit;
import org.sugarj.cleardep.BuildUnit.State;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class MakePermissive extends SpoofaxBuilder<MakePermissive.Input> {

	public static SpoofaxBuilderFactory<Input, MakePermissive> factory = new SpoofaxBuilderFactory<Input, MakePermissive>() {
		private static final long serialVersionUID = 657230698706473822L;

		@Override
		public MakePermissive makeBuilder(Input input, BuildManager manager) { return new MakePermissive(input, manager); }
	};
	
	public static class Input extends SpoofaxInput {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4381601872931676757L;
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
	
	public MakePermissive(Input input, BuildManager manager) {
		super(input, factory, manager);
	}

	@Override
	protected String taskDescription() {
		return "Make grammar permissive for error-recovery parsing.";
	}
	
	@Override
	public Path persistentPath() {
		return context.depPath("makePermissive." + input.sdfmodule + ".dep");
	}

	@Override
	public void build(BuildUnit result) throws IOException {
		require(CopySdf.factory, new CopySdf.Input(context, input.sdfmodule, input.externaldef));
		require(PackSdf.factory, new PackSdf.Input(context,input.sdfmodule, input.buildSdfImports));
		
		RelativePath inputPath = context.basePath("${include}/" + input.sdfmodule + ".def");
		RelativePath outputPath = context.basePath("${include}/" + input.sdfmodule + "-Permissive.def");
		
		result.requires(inputPath);
		ExecutionResult er = StrategoExecutor.runStrategoCLI(StrategoExecutor.permissiveGrammarsContext(), 
				make_permissive.getMainStrategy(), "make-permissive", new LoggingFilteringIOAgent(Pattern.quote("[ make-permissive | info ]") + ".*"),
				"-i", inputPath,
				"-o", outputPath,
				"--optimize", "on"
				);
		result.generates(outputPath);
		result.setState(State.finished(er.success));
	}
}
