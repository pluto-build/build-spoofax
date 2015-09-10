package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.sugarj.common.FileCommands;
import org.sugarj.common.StringCommands;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxBuilderFactoryFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.StrategoExecutor.ExecutionResult;
import build.pluto.buildspoofax.util.LoggingFilteringIOAgent;
import build.pluto.output.None;

public class StrategoJavaCompiler extends SpoofaxBuilder<StrategoJavaCompiler.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, StrategoJavaCompiler> factory = SpoofaxBuilderFactoryFactory.of(StrategoJavaCompiler.class, Input.class);

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -5234502421638344690L;

		public final File inputPath;
		public final File outputPath;
		public final String packageName;
		public final boolean library;
		public final boolean clean;
		public final File[] directoryIncludes;
		public final String[] libraryIncludes;
		public final File cacheDir;
		public final String[] additionalArgs;
		public final BuildRequest<?, ?, ?, ?>[] requiredUnits;

		public Input(SpoofaxContext context, File inputPath, File outputPath, String packageName, boolean library, boolean clean,
				File[] directoryIncludes, String[] libraryIncludes, File cacheDir, String[] additionalArgs, BuildRequest<?, ?, ?, ?>[] requiredUnits) {
			super(context);
			this.inputPath = inputPath;
			this.outputPath = outputPath;
			this.packageName = packageName;
			this.library = library;
			this.clean = clean;
			this.directoryIncludes = directoryIncludes;
			this.libraryIncludes = libraryIncludes;
			this.cacheDir = cacheDir;
			this.additionalArgs = additionalArgs;
			this.requiredUnits = requiredUnits;
		}
	}

	public StrategoJavaCompiler(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Compile Stratego code";
	}

	@Override
	public File persistentPath(Input input) {
		Path rel = FileCommands.getRelativePath(context.baseDir, input.inputPath);
		String relname = rel.toString().replace(File.separatorChar, '_');
		return context.depPath("strategoJavaCompiler." + relname + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		requireBuild(input.requiredUnits);

		require(input.inputPath);

		File rtree = FileCommands.replaceExtension(input.outputPath, "rtree");
		File strdep = FileCommands.addExtension(input.outputPath, "dep");

		FileCommands.delete(rtree);

		StringBuilder directoryIncludes = new StringBuilder();
		for (File dir : input.directoryIncludes)
			if (dir != null)
				directoryIncludes.append("-I ").append(dir).append(" ");
		StringBuilder libraryIncludes = new StringBuilder();
		for (String lib : input.libraryIncludes)
			if (lib != null && lib.isEmpty())
				directoryIncludes.append("-la ").append(lib).append(" ");

		ExecutionResult er = StrategoExecutor.runStrategoCLI(
				StrategoExecutor.strjContext(),
				org.strategoxt.strj.main_0_0.instance,
				"strj",
				new LoggingFilteringIOAgent(Pattern.quote("[ strj | info ]") + ".*", Pattern.quote("[ strj | error ] Compilation failed") + ".*", Pattern
						.quote("[ strj | warning ] Nullary constructor") + ".*"), "-i", input.inputPath, "-o", input.outputPath,
				input.packageName != null ? "-p " + input.packageName : "", input.library ? "--library" : "", input.clean ? "--clean" : "", directoryIncludes,
				libraryIncludes, input.cacheDir != null ? "--cache-dir " + input.cacheDir : "", StringCommands.printListSeparated(input.additionalArgs, " "));
		FileCommands.delete(rtree);

		provide(input.outputPath);
		provide(rtree);
		provide(strdep);

		if (FileCommands.exists(strdep))
			registerUsedPaths(strdep);

		setState(State.finished(er.success));

		return None.val;
	}

	private void registerUsedPaths(File strdep) throws IOException {
		String contents = FileCommands.readFileAsString(strdep);
		String[] lines = contents.split("[\\s\\\\]+");
		for (int i = 1; i < lines.length; i++) { // skip first line, which lists
													// the generated ctree file
			String line = lines[i];
			File p = new File(line);
			// TODO: there can be dependencies outside of the project, then this does not work any more?
			Path prel = FileCommands.getRelativePath(context.baseDir, p);
			require(prel != null ? prel.toFile() : p);
		}
	}
}
