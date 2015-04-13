package build.pluto.buildspoofax.builders;

import java.io.IOException;
import java.util.regex.Pattern;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.tools.main_sdf2parenthesize_0_0;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.LoggingFilteringIOAgent;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.buildspoofax.builders.aux.ParseSdfDefinition;
import build.pluto.buildspoofax.stampers.Sdf2ParenthesizeStamper;
import build.pluto.output.None;

public class Sdf2Parenthesize extends SpoofaxBuilder<Sdf2Parenthesize.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, Sdf2Parenthesize> factory = new SpoofaxBuilderFactory<Input, None, Sdf2Parenthesize>() {
		private static final long serialVersionUID = -7165488913542364367L;

		@Override
		public Sdf2Parenthesize makeBuilder(Input input) { return new Sdf2Parenthesize(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 6177130857266733408L;
		
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
	
	public Sdf2Parenthesize(Input input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Extract parenthesis structure from grammar";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("sdf2Parenthesize." + input.sdfmodule + ".dep");
	}

	@Override
	public None build() throws IOException {
		requireBuild(CopySdf.factory, new CopySdf.Input(context, input.sdfmodule, input.externaldef));
		BuildRequest<?, ?, ?, ?> packSdf = new BuildRequest<>(PackSdf.factory, new PackSdf.Input(context, input.sdfmodule, input.buildSdfImports));
		requireBuild(packSdf);
		
		RelativePath inputPath = context.basePath("${include}/" + input.sdfmodule + ".def");
		RelativePath outputPath = context.basePath("${include}/" + input.sdfmodule + "-parenthesize.str");
		String outputmodule = "include/" + input.sdfmodule + "-parenthesize";

		if (SpoofaxContext.BETTER_STAMPERS) {
			BuildRequest<?, IStrategoTerm, ?, ?> parseSdfDefinition = new BuildRequest<>(ParseSdfDefinition.factory, new ParseSdfDefinition.Input(context, inputPath, new BuildRequest<?,?,?,?>[]{packSdf}));
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
