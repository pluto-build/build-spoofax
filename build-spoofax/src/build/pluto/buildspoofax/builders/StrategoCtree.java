package build.pluto.buildspoofax.builders;

import java.io.IOException;

import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;
import org.sugarj.common.util.ArrayUtils;

import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.stamp.LastModifiedStamper;
import build.pluto.stamp.Stamper;

public class StrategoCtree extends SpoofaxBuilder<StrategoCtree.Input, Path> {

	public static SpoofaxBuilderFactory<Input, Path, StrategoCtree> factory = new SpoofaxBuilderFactory<Input, Path, StrategoCtree>() {
		private static final long serialVersionUID = -8635408307377750115L;

		@Override
		public StrategoCtree makeBuilder(Input input) { return new StrategoCtree(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 6323245405121428720L;
		
		public final String sdfmodule;
		public final String buildSdfImports;
		public final String strmodule;
		public final Path externaljar;
		public final String externaljarflags;
		public final Path externalDef;

		public final BuildRequest<?,?,?,?>[] requiredUnits;
		public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports, String strmodule, Path externaljar, String externaljarflags, Path externalDef, BuildRequest<?,?,?,?>[] requiredUnits) {
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
	protected String description() {
		return "Prepare Stratego code";
	}
	
	@Override
	public Path persistentPath() {
		return context.depPath("strategoCtree." + input.sdfmodule + "." + input.strmodule + ".dep");
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public Path build() throws IOException {
		BuildRequest<Rtg2Sig.Input,None,Rtg2Sig,?> rtg2Sig = new BuildRequest<>(Rtg2Sig.factory, new Rtg2Sig.Input(context, input.sdfmodule, input.buildSdfImports));
		
		if (!context.isBuildStrategoEnabled(this))
			throw new IllegalArgumentException(context.props.substitute("Main stratego file '${strmodule}.str' not found."));
		
		requireBuild(CopyJar.factory, new CopyJar.Input(context, input.externaljar));
		
		RelativePath inputPath = context.basePath("${trans}/" + input.strmodule + ".str");
		RelativePath outputPath = context.basePath("${include}/" + input.strmodule + ".ctree");
		requireBuild(StrategoJavaCompiler.factory,
				new StrategoJavaCompiler.Input(
						context,
						inputPath, 
						outputPath, 
						"trans", 
						null, 
						true, 
						true,
						new Path[]{context.baseDir, context.basePath("${trans}"), context.basePath("${lib}"), context.basePath("${include}"), input.externalDef},
						new String[]{"stratego-lib", "stratego-sglr", "stratego-gpp", "stratego-xtc", "stratego-aterm", "stratego-sdf", "strc"},
						context.basePath(".cache"),
						ArrayUtils.arrayAdd("-F", input.externaljarflags.split("[\\s]+")),
						ArrayUtils.arrayAdd(rtg2Sig, input.requiredUnits)));
		
		return outputPath;
	}
}
