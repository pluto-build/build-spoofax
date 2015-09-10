package build.pluto.buildspoofax.builders;


import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.util.file.FileUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.tools.main_pp_pp_table_0_0;
import org.strategoxt.tools.main_ppgen_0_0;
import org.sugarj.common.FileCommands;

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
import build.pluto.buildspoofax.stampers.PPGenStamper;
import build.pluto.buildspoofax.util.LoggingFilteringIOAgent;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;

import com.google.common.base.Joiner;

public class PPGen extends SpoofaxBuilder<PPGen.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, PPGen> factory = SpoofaxBuilderFactoryFactory.of(PPGen.class, PPGen.Input.class);
	
	public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -6752720592940603183L;
        
        public final String sdfModule;

        public Input(SpoofaxContext context, String sdfModule) {
            super(context);
            this.sdfModule = sdfModule;
        }
	}
	
	public PPGen(Input context) {
		super(context);
	}
	
	@Override
	protected String description(Input input) {
		return "Generate pretty-print table from grammar";
	}
	
	@Override
	protected File persistentPath(Input input) {
		return input.context.depPath("ppGen." + input.sdfModule + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		if (!context.isBuildStrategoEnabled(this))
			return None.val;
		
		
		
		BuildRequest<PackSdf.Input,None,PackSdf,?> packSdf = new BuildRequest<>(PackSdf.factory, new PackSdf.Input(context, input.sdfModule, Joiner.on(' ').join(context.settings.sdfArgs())));
		requireBuild(packSdf);

		File inputPath = FileUtils.toFile(context.settings.getSdfCompiledDefFile(input.sdfModule));
		File ppOutputPath = FileUtils.toFile(context.settings.getGenPpCompiledFile(input.sdfModule));
		File afOutputPath = FileUtils.toFile(context.settings.getGenPpAfCompiledFile(input.sdfModule));
		
		if (SpoofaxContext.BETTER_STAMPERS) {
			BuildRequest<ParseSdfDefinition.Input, OutputPersisted<IStrategoTerm>, ParseSdfDefinition, ?> parseSdfDefinition = new BuildRequest<>(
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
