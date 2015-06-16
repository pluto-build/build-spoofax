package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.strategoxt.tools.main_pack_sdf_0_0;
import org.sugarj.common.FileCommands;

import build.pluto.BuildUnit.State;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.buildspoofax.util.LoggingFilteringIOAgent;
import build.pluto.output.None;
import build.pluto.stamp.LastModifiedStamper;

public class PackSdf extends SpoofaxBuilder<PackSdf.Input, None> {
	
	public static SpoofaxBuilderFactory<Input, None, PackSdf> factory = SpoofaxBuilderFactory.of(PackSdf.class, Input.class);

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 2058684747897720328L;
		
		public final String sdfmodule;
		public final String buildSdfImports;
		public Input(SpoofaxContext context) {
			super(context);
			this.sdfmodule = context.props.get("sdfmodule");
			this.buildSdfImports = context.props.get("build.sdf.imports");
		}
		public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports) {
			super(context);
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}
	
	public PackSdf(Input input) {
		super(input);
	}
	
	@Override
	protected String description(Input input) {
		return "Pack SDF modules";
	}
	
	@Override
	protected File persistentPath(Input input) {
		return context.depPath("packSdf." + input.sdfmodule + ".dep");
	}
	
	@Override
	public None build(Input input) throws IOException {
		// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/src-gen/syntax/TemplateLang.sdf'.
		requireBuild(CompileMetalanguageFiles.factory, new CompileMetalanguageFiles.Input(context));
		
		copySdf2();
		
		File inputPath = context.basePath("${syntax}/" + input.sdfmodule + ".sdf");
		File outputPath = context.basePath("${include}/" + input.sdfmodule + ".def");
		String utilsInclude = FileCommands.exists(context.basePath("${utils}")) ? context.props.substitute("-I ${utils}") : "";
		
		require(inputPath);
		
		ExecutionResult er = StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), 
				main_pack_sdf_0_0.instance, "pack-sdf", new LoggingFilteringIOAgent(Pattern.quote("  including ") + ".*"),
				"-i", inputPath,
				"-o", outputPath,
				FileCommands.exists(context.basePath("${syntax}")) ? "-I " + context.basePath("${syntax}") : "",
				FileCommands.exists(context.basePath("${lib}")) ? "-I " + context.basePath("${lib}") : "",
				utilsInclude,
				input.buildSdfImports);
		
		provide(outputPath);
		for (File required : extractRequiredPaths(er.errLog))
			require(required);
		
		setState(State.finished(er.success));
		
		return None.val;
	}

	private List<File> extractRequiredPaths(String log) {
		final String prefix = "  including ";
		final String infix = " from ";
		
		List<File> paths = new ArrayList<>();
		for (String s : log.split("\\n")) {
			if (s.startsWith(prefix)) {
				String module = s.substring(prefix.length());
				int infixIndex = module.indexOf(infix);
				if (infixIndex < 0 && FileCommands.acceptableAsAbsolute(module)) {
					paths.add(new File(s.substring(prefix.length())));
				}
				else if (infixIndex >= 0) {
					String def = module.substring(infixIndex + infix.length());
					if (FileCommands.acceptable(def))
						paths.add(new File(def));
				}
			}
		}
		return paths;
	}

	private void copySdf2() {
		List<Path> srcSdfFiles = FileCommands.listFilesRecursive(context.basePath("syntax").toPath(), new SuffixFileFilter("sdf"));
		for (Path p : srcSdfFiles) {
			require(p.toFile(), LastModifiedStamper.instance);
			File target = FileCommands.copyFile(context.basePath("syntax"), context.basePath("${syntax}"), p.toFile(), StandardCopyOption.COPY_ATTRIBUTES);
			provide(target);
		}		
	}

}
