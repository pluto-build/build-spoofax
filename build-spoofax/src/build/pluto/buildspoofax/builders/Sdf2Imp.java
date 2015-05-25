package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.strategoxt.imp.generator.sdf2imp_jvm_0_0;

import build.pluto.BuildUnit.State;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.buildspoofax.util.LoggingFilteringIOAgent;
import build.pluto.output.None;

public class Sdf2Imp extends SpoofaxBuilder<Sdf2Imp.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, Sdf2Imp> factory = SpoofaxBuilderFactory.of(Sdf2Imp.class, Input.class);

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 5390265710389276659L;
		public final String esvmodule;
		public final String sdfmodule;
		public final String buildSdfImports;

		public Input(SpoofaxContext context, String esvmodule, String sdfmodule, String buildSdfImports) {
			super(context);
			this.esvmodule = esvmodule;
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
		}
	}

	public Sdf2Imp(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Generate language plug-in";
	}

	@Override
	protected File persistentPath(Input input) {
		return context.depPath("sdf2Imp." + input.esvmodule + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		requireBuild(Sdf2Rtg.factory, new Sdf2Rtg.Input(context, input.sdfmodule, input.buildSdfImports));

		File inputPath = new File(context.basePath("editor"), input.esvmodule + ".main.esv");

		LoggingFilteringIOAgent agent = new LoggingFilteringIOAgent(".*");
		agent.setWorkingDir(inputPath.getParentFile().getAbsolutePath());

		require(inputPath);
		ExecutionResult er = StrategoExecutor.runStratego(StrategoExecutor.generatorContext(), sdf2imp_jvm_0_0.instance, "sdf2imp", agent, StrategoExecutor
				.generatorContext().getFactory().makeString(inputPath.getPath()));

		registerUsedPaths(er.errLog);

		setState(State.finished(er.success));
		return None.val;
	}

	private void registerUsedPaths(String log) {
		String defPrefix = "Found accompanying .def file: ";
		String reqPrefix = "found file ";
		String genPrefix = "Generating ";

		Set<File> require = new HashSet<>();
		Set<File> provide = new HashSet<>();

		for (String s : log.split("\\n")) {
			if (s.startsWith(reqPrefix)) {
				String file = s.substring(reqPrefix.length());
				require.add(context.basePath(file));
			} else if (s.startsWith(genPrefix)) {
				String file = s.substring(genPrefix.length());
				provide.add(context.basePath(file));
			} else if (s.startsWith(defPrefix)) {
				String file = s.substring(defPrefix.length());
				require.add(context.basePath(file));
			}
		}

		require.removeAll(provide);
		for (File p : require)
			require(p);
		for (File p : provide)
			provide(p);
	}

}
