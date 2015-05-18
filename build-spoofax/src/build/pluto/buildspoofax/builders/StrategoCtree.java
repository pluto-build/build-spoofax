package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;

import org.sugarj.common.util.ArrayUtils;

import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.output.Out;
import build.pluto.stamp.LastModifiedStamper;
import build.pluto.stamp.Stamper;

public class StrategoCtree extends SpoofaxBuilder<StrategoCtree.Input, Out<File>> {

	public static SpoofaxBuilderFactory<Input, Out<File>, StrategoCtree> factory = SpoofaxBuilderFactory.of(StrategoCtree.class, Input.class);

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
	public Out<File> build(Input input) throws IOException {
		BuildRequest<Rtg2Sig.Input, None, Rtg2Sig, ?> rtg2Sig = new BuildRequest<>(Rtg2Sig.factory, new Rtg2Sig.Input(context, input.sdfmodule,
				input.buildSdfImports));

		if (!context.isBuildStrategoEnabled(this))
			throw new IllegalArgumentException(context.props.substitute("Main stratego file '${strmodule}.str' not found."));

		requireBuild(CopyJar.factory, new CopyJar.Input(context, input.externaljar));

		File inputPath = context.basePath("${trans}/" + input.strmodule + ".str");
		File outputPath = context.basePath("${include}/" + input.strmodule + ".ctree");

		File[] directoryIncludes = new File[] { context.baseDir, context.basePath("${trans}"), context.basePath("${lib}"), context.basePath("${include}"),
				input.externalDef };
		requireBuild(
				StrategoJavaCompiler.factory,
				new StrategoJavaCompiler.Input(context, inputPath, outputPath, "trans", null, true, true, directoryIncludes, new String[] {
				"stratego-lib", "stratego-sglr", "stratego-gpp", "stratego-xtc", "stratego-aterm", "stratego-sdf", "strc" }, context.basePath(".cache"),
				ArrayUtils.arrayAdd("-F", input.externaljarflags.split("[\\s]+")), ArrayUtils.arrayAdd(rtg2Sig, input.requiredUnits)));

		return Out.of(outputPath);
	}
}
