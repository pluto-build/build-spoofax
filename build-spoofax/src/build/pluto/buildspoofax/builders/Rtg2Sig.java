package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;

import org.metaborg.util.file.FileUtils;
import org.strategoxt.tools.main_rtg2sig_0_0;

import build.pluto.BuildUnit.State;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxBuilderFactoryFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.buildspoofax.util.LoggingFilteringIOAgent;
import build.pluto.output.None;

public class Rtg2Sig extends SpoofaxBuilder<Rtg2Sig.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, Rtg2Sig> factory = SpoofaxBuilderFactoryFactory.of(Rtg2Sig.class, Input.class);
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -8305692591357842018L;
		
		public final String sdfModule;
		
		public Input(SpoofaxContext context, String sdfModule) {
			super(context);
            this.sdfModule = sdfModule;
		}
	}
	
	public Rtg2Sig(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Generate Stratego signatures for grammar constructors";
	}
	
	@Override
    public File persistentPath(Input input) {
		return context.depPath("rtg2Sig." + input.sdfModule + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		
		if (context.isBuildStrategoEnabled(this)) {
			// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/include/TemplateLang.rtg'.
			requireBuild(Sdf2Rtg.factory, new Sdf2Rtg.Input(context, input.sdfModule));

			File inputPath = FileUtils.toFile(context.settings.getRtgFile(input.sdfModule));
			File outputPath = FileUtils.toFile(context.settings.getStrCompiledSigFile(input.sdfModule));
			
			require(inputPath);
			ExecutionResult er = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
					main_rtg2sig_0_0.instance, "rtg2sig", new LoggingFilteringIOAgent(),
					"-i", inputPath,
					"--module", input.sdfModule,
					"-o", outputPath);
			provide(outputPath);
			setState(State.finished(er.success));
		}
		
		return None.val;
	}

}
