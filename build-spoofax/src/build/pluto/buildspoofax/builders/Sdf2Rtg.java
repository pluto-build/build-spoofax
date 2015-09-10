package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.util.file.FileUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.tools.main_sdf2rtg_0_0;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxBuilderFactoryFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.buildspoofax.builders.aux.ParseSdfDefinition;
import build.pluto.buildspoofax.stampers.Sdf2RtgStamper;
import build.pluto.buildspoofax.util.LoggingFilteringIOAgent;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;

import com.google.common.base.Joiner;

public class Sdf2Rtg extends SpoofaxBuilder<Sdf2Rtg.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, Sdf2Rtg> factory = SpoofaxBuilderFactoryFactory.of(Sdf2Rtg.class, Input.class);
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -4487049822305558202L;
		
		public final String sdfModule;
		
		public Input(SpoofaxContext context, String sdfModule) {
			super(context);
			this.sdfModule = sdfModule;
		}
	}
	
	public Sdf2Rtg(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Extract constructor signatures from grammar";
	}
	
	@Override
	protected File persistentPath(Input input) {
		return context.depPath("sdf2Rtg." + input.sdfModule + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/include/TemplateLang.def'.
		BuildRequest<PackSdf.Input, None, PackSdf, ?> packSdf = new BuildRequest<>(PackSdf.factory, new PackSdf.Input(context, input.sdfModule, Joiner.on(' ').join(context.settings.sdfArgs())));
		requireBuild(packSdf);
		
		File inputPath = FileUtils.toFile(context.settings.getSdfCompiledDefFile(input.sdfModule));
		File outputPath = FileUtils.toFile(context.settings.getRtgFile(input.sdfModule));

		if (SpoofaxContext.BETTER_STAMPERS) {
			BuildRequest<ParseSdfDefinition.Input, OutputPersisted<IStrategoTerm>, ParseSdfDefinition, ?> parseSdfDefinition = new BuildRequest<>(
					ParseSdfDefinition.factory, new ParseSdfDefinition.Input(context, inputPath, new BuildRequest<?, ?, ?, ?>[] { packSdf }));
			require(inputPath, new Sdf2RtgStamper(parseSdfDefinition));
		}
		else
			require(inputPath);
		
        // TODO: avoid redundant call to sdf2table
        // TODO: set nativepath to the native bundle, so that sdf2table can be found
		ExecutionResult er = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
				main_sdf2rtg_0_0.instance, "sdf2rtg", new LoggingFilteringIOAgent(Pattern.quote("Invoking native tool") + ".*"),
				"-i", inputPath,
				"-m", input.sdfModule,
				"-o", outputPath,
				"--ignore-missing-cons" /*,
				"-Xnativepath", context.basePath("${nativepath}/")*/);
		provide(outputPath);
		setState(State.finished(er.success));
		
		return None.val;
	}

}
