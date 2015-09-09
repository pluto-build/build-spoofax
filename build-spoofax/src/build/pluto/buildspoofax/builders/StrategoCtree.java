package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;

import org.metaborg.util.file.FileUtils;
import org.sugarj.common.util.ArrayUtils;

import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.LastModifiedStamper;
import build.pluto.stamp.Stamper;

public class StrategoCtree extends SpoofaxBuilder<StrategoCtree.Input, OutputPersisted<File>> {

	public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, StrategoCtree> factory = SpoofaxBuilderFactory.of(StrategoCtree.class, Input.class);

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 6323245405121428720L;

		public final String sdfmodule;
		public final String buildSdfImports;
		public final String strmodule;
		public final File externaljar;
		public final String externaljarflags;
		public final File externalDef;

		public final BuildRequest<?, ?, ?, ?>[] requiredUnits;

		public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports, String strmodule, File externaljar, String externaljarflags,
				File externalDef, BuildRequest<?, ?, ?, ?>[] requiredUnits) {
			super(context);
			this.sdfmodule = sdfmodule;
			this.buildSdfImports = buildSdfImports;
			this.strmodule = strmodule;
			this.externaljar = externaljar;
			this.externaljarflags = externaljarflags;
			this.externalDef = externalDef;
			this.requiredUnits = requiredUnits;
		}
	}

	public StrategoCtree(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Prepare Stratego code";
	}

	@Override
	public File persistentPath(Input input) {
		return context.depPath("strategoCtree." + input.sdfmodule + "." + input.strmodule + ".dep");
	}

	@Override
	public Stamper defaultStamper() {
		return LastModifiedStamper.instance;
	}

	@Override
	public OutputPersisted<File> build(Input input) throws IOException {
		BuildRequest<Rtg2Sig.Input, None, Rtg2Sig, ?> rtg2Sig = new BuildRequest<>(Rtg2Sig.factory, new Rtg2Sig.Input(context, input.sdfmodule));

		if (!context.isBuildStrategoEnabled(this)) {
		    final String strategoModule = context.settings.strategoName();
			throw new IllegalArgumentException(String.format("Main stratego file '%s' not found", strategoModule));
		}

		requireBuild(CopyJar.factory, new CopyJar.Input(context, input.externaljar));

		File inputPath = FileUtils.toFile(context.settings.getStrMainFile());
		File outputPath = FileUtils.toFile(context.settings.getStrCompiledCtreeFile());
		File cacheDir = FileUtils.toFile(context.settings.getCacheDirectory());

		// TODO: get libraries from stratego arguments
		// TODO: get source paths from source path service
		File[] directoryIncludes = new File[] { context.baseDir, FileUtils.toFile(context.settings.getTransDirectory()), 
		    FileUtils.toFile(context.settings.getLibDirectory()), FileUtils.toFile(context.settings.getIconsDirectory()), 
		    input.externalDef };
		requireBuild(
				StrategoJavaCompiler.factory,
				new StrategoJavaCompiler.Input(context, inputPath, outputPath, "trans", true, true, directoryIncludes, new String[] {
				"stratego-lib", "stratego-sglr", "stratego-gpp", "stratego-xtc", "stratego-aterm", "stratego-sdf", "strc" }, cacheDir,
				ArrayUtils.arrayAdd("-F", input.externaljarflags.split("[\\s]+")), ArrayUtils.arrayAdd(rtg2Sig, input.requiredUnits)));

		return OutputPersisted.of(outputPath);
	}
}
