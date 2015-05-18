package build.pluto.buildspoofax.builders;


import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.tools.main_pp_pp_table_0_0;
import org.strategoxt.tools.main_ppgen_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.buildspoofax.builders.aux.ParseSdfDefinition;
import build.pluto.buildspoofax.stampers.PPGenStamper;
import build.pluto.buildspoofax.util.LoggingFilteringIOAgent;
import build.pluto.output.None;
import build.pluto.output.Out;

public class PPGen extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, PPGen> factory = SpoofaxBuilderFactory.of(PPGen.class, SpoofaxInput.class);
	
	public PPGen(SpoofaxInput context) {
		super(context);
	}
	
	@Override
	protected String description(SpoofaxInput input) {
		return "Generate pretty-print table from grammar";
	}
	
	@Override
	protected File persistentPath(SpoofaxInput input) {
		return input.context.depPath("ppGen.dep");
	}

	@Override
	public None build(SpoofaxInput input) throws IOException {
		SpoofaxContext context = input.context;
		if (!context.isBuildStrategoEnabled(this))
			return None.val;
		
		BuildRequest<PackSdf.Input,None,PackSdf,?> packSdf = new BuildRequest<>(PackSdf.factory, new PackSdf.Input(context));
		requireBuild(packSdf);

		File inputPath = context.basePath("${include}/${sdfmodule}.def");
		File ppOutputPath = context.basePath("${include}/${sdfmodule}.generated.pp");
		File afOutputPath = context.basePath("${include}/${sdfmodule}.generated.pp.af");
		
		if (SpoofaxContext.BETTER_STAMPERS) {
			BuildRequest<ParseSdfDefinition.Input, Out<IStrategoTerm>, ParseSdfDefinition, ?> parseSdfDefinition = new BuildRequest<>(
					ParseSdfDefinition.factory, new ParseSdfDefinition.Input(context, inputPath, new BuildRequest<?, ?, ?, ?>[] { packSdf }));
			require(inputPath, new PPGenStamper(parseSdfDefinition));
		}
		else
			require(inputPath);
		
		ExecutionResult er1 = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
				main_ppgen_0_0.instance, "main-ppgen", new LoggingFilteringIOAgent(Pattern.quote("[ main-ppgen | warning ]") + ".*"),
				"-i", inputPath,
				"-t",
				"-b",
				"-o", afOutputPath);
		provide(afOutputPath);
		
		// requires(afOutputPath);
		ExecutionResult er2 = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
				main_pp_pp_table_0_0.instance, "main-pp-pp-table", new LoggingFilteringIOAgent(),
				"-i", afOutputPath,
				"-o", ppOutputPath);
		provide(ppOutputPath);
		
		if (!FileCommands.exists(afOutputPath)) {
			FileCommands.writeToFile(afOutputPath, "PP-Table([])");
			provide(afOutputPath);
		}
		
		setState(State.finished(er1.success && er2.success));
		
		return None.val;
	}

}
