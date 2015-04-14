package build.pluto.buildspoofax.builders;

import java.io.IOException;

import org.strategoxt.tools.main_rtg2sig_0_0;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.BuildUnit.State;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.buildspoofax.util.LoggingFilteringIOAgent;
import build.pluto.output.None;

public class Rtg2Sig extends SpoofaxBuilder<Rtg2Sig.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, Rtg2Sig> factory = new SpoofaxBuilderFactory<Input, None, Rtg2Sig>() {
		private static final long serialVersionUID = -2453863767591818617L;

		@Override
		public Rtg2Sig makeBuilder(Input input) { return new Rtg2Sig(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -8305692591357842018L;
		
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports) {
			super(context);
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	public Rtg2Sig(Input input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Generate Stratego signatures for grammar constructors";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("rtg2Sig." + input.sdfmodule + ".dep");
	}

	@Override
	public None build() throws IOException {
		
		if (context.isBuildStrategoEnabled(this)) {
			// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/include/TemplateLang.rtg'.
			requireBuild(Sdf2Rtg.factory, new Sdf2Rtg.Input(context, input.sdfmodule, input.buildSdfImports));

			RelativePath inputPath = context.basePath("${include}/" + input.sdfmodule + ".rtg");
			RelativePath outputPath = context.basePath("${include}/" + input.sdfmodule + ".str");
			
			require(inputPath);
			ExecutionResult er = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
					main_rtg2sig_0_0.instance, "rtg2sig", new LoggingFilteringIOAgent(),
					"-i", inputPath,
					"--module", input.sdfmodule,
					"-o", outputPath);
			provide(outputPath);
			setState(State.finished(er.success));
		}
		
		return None.val;
	}

}
