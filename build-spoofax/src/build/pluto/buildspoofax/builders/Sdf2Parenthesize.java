package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.core.SpoofaxProjectConstants;
import org.metaborg.util.file.FileUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.tools.main_sdf2parenthesize_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxBuilderFactoryFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.buildspoofax.builders.aux.ParseSdfDefinition;
import build.pluto.buildspoofax.stampers.Sdf2ParenthesizeStamper;
import build.pluto.buildspoofax.util.LoggingFilteringIOAgent;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;

import com.google.common.base.Joiner;

public class Sdf2Parenthesize extends SpoofaxBuilder<Sdf2Parenthesize.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, Sdf2Parenthesize> factory = SpoofaxBuilderFactoryFactory.of(Sdf2Parenthesize.class, Input.class);
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 6177130857266733408L;

		public final String sdfModule;
		
		public Input(SpoofaxContext context, String sdfModule) {
			super(context);
            this.sdfModule = sdfModule;
		}
	}
	
	public Sdf2Parenthesize(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Extract parenthesis structure from grammar";
	}
	
	@Override
    public File persistentPath(Input input) {
		return context.depPath("sdf2Parenthesize." + input.sdfModule + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		BuildRequest<PackSdf.Input, None, PackSdf, ?> packSdf = new BuildRequest<>(PackSdf.factory, new PackSdf.Input(context, input.sdfModule, Joiner.on(' ').join(context.settings.sdfArgs())));
		requireBuild(packSdf);
		
		File inputPath = FileUtils.toFile(context.settings.getSdfCompiledDefFile(input.sdfModule));
		File outputPath = FileUtils.toFile(context.settings.getStrCompiledParenthesizerFile(input.sdfModule));
		String outputmodule = "include/" + input.sdfModule + "-parenthesize";

		if (SpoofaxContext.BETTER_STAMPERS) {
			BuildRequest<ParseSdfDefinition.Input, OutputPersisted<IStrategoTerm>, ParseSdfDefinition, ?> parseSdfDefinition = new BuildRequest<>(
					ParseSdfDefinition.factory, new ParseSdfDefinition.Input(context, inputPath, new BuildRequest<?, ?, ?, ?>[] { packSdf }));
			require(inputPath, new Sdf2ParenthesizeStamper(parseSdfDefinition));
		}
		else
			require(inputPath);
		
		// TODO: avoid redundant call to sdf2table
		// TODO: set nativepath to the native bundle, so that sdf2table can be found
		ExecutionResult er = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
				main_sdf2parenthesize_0_0.instance, "sdf2parenthesize", new LoggingFilteringIOAgent(Pattern.quote("[ sdf2parenthesize | info ]") + ".*", Pattern.quote("Invoking native tool") + ".*"),
				"-i", inputPath,
				"-m", input.sdfModule,
				"--lang", input.sdfModule,
				"--omod", outputmodule,
				"-o", outputPath,
				"--main-strategy", "io-" + input.sdfModule + "-parenthesize",
				"--rule-prefix", input.sdfModule,
				"--sig-module", SpoofaxProjectConstants.DIR_INCLUDE + "/" + input.sdfModule);
		
		if (!er.success)
			FileCommands.writeToFile(outputPath, "module include/" + input.sdfModule + "-parenthesize rules parenthesize-" + input.sdfModule + " = id");
		
		provide(outputPath);
		
		return None.val;
	}

}
