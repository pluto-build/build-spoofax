package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.tools.main_sdf2parenthesize_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.buildspoofax.builders.aux.ParseSdfDefinition;
import build.pluto.buildspoofax.stampers.Sdf2ParenthesizeStamper;
import build.pluto.buildspoofax.util.LoggingFilteringIOAgent;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;

public class Sdf2Parenthesize extends SpoofaxBuilder<Sdf2Parenthesize.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, Sdf2Parenthesize> factory = SpoofaxBuilderFactory.of(Sdf2Parenthesize.class, Input.class);
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 6177130857266733408L;
		
		public final String sdfmodule;
		public final String buildSdfImports;
		public final File externaldef;

		public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports, File externaldef) {
			super(context);
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
			this.externaldef = externaldef;
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
	protected File persistentPath(Input input) {
		return context.depPath("sdf2Parenthesize." + input.sdfmodule + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		requireBuild(CopySdf.factory, new CopySdf.Input(context, input.sdfmodule, input.externaldef));
		BuildRequest<PackSdf.Input, None, PackSdf, ?> packSdf = new BuildRequest<>(PackSdf.factory, new PackSdf.Input(context, input.sdfmodule, input.buildSdfImports));
		requireBuild(packSdf);
		
		File inputPath = context.basePath("${include}/" + input.sdfmodule + ".def");
		File outputPath = context.basePath("${include}/" + input.sdfmodule + "-parenthesize.str");
		String outputmodule = "include/" + input.sdfmodule + "-parenthesize";

		if (SpoofaxContext.BETTER_STAMPERS) {
			BuildRequest<ParseSdfDefinition.Input, OutputPersisted<IStrategoTerm>, ParseSdfDefinition, ?> parseSdfDefinition = new BuildRequest<>(
					ParseSdfDefinition.factory, new ParseSdfDefinition.Input(context, inputPath, new BuildRequest<?, ?, ?, ?>[] { packSdf }));
			require(inputPath, new Sdf2ParenthesizeStamper(parseSdfDefinition));
		}
		else
			require(inputPath);
		
		// XXX avoid redundant call to sdf2table
		ExecutionResult er = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
				main_sdf2parenthesize_0_0.instance, "sdf2parenthesize", new LoggingFilteringIOAgent(Pattern.quote("[ sdf2parenthesize | info ]") + ".*", Pattern.quote("Invoking native tool") + ".*"),
				"-i", inputPath,
				"-m", input.sdfmodule,
				"--lang", input.sdfmodule,
				"--omod", outputmodule,
				"-o", outputPath,
				"--main-strategy", "io-" + input.sdfmodule + "-parenthesize",
				"--rule-prefix", input.sdfmodule,
				"--sig-module", context.props.get("lib-gen") + "/" + input.sdfmodule);
		
		if (!er.success)
			FileCommands.writeToFile(outputPath, "module include/" + input.sdfmodule + "-parenthesize rules parenthesize-" + input.sdfmodule + " = id");
		
		provide(outputPath);
		
		return None.val;
	}

}
