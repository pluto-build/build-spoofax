package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.util.Util;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.cleardep.build.BuildRequirement;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class StrategoCtree extends SpoofaxBuilder<StrategoCtree.Input> {

	public static SpoofaxBuilderFactory<Input, StrategoCtree> factory = new SpoofaxBuilderFactory<Input, StrategoCtree>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8635408307377750115L;

		@Override
		public StrategoCtree makeBuilder(Input input, BuildManager manager) { return new StrategoCtree(input, manager); }
	};
	
	public static class Input extends SpoofaxInput {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6323245405121428720L;
		public final String sdfmodule;
		public final String buildSdfImports;
		public final String strmodule;
		public final Path externaljar;
		public final String externaljarflags;
		public final Path externalDef;

		public final BuildRequirement<?,?,?,?>[] requiredUnits;
		public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports, String strmodule, Path externaljar, String externaljarflags, Path externalDef, BuildRequirement<?,?,?,?>[] requiredUnits) {
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
	
	public StrategoCtree(Input input, BuildManager manager) {
		super(input, factory, manager);
	}

	@Override
	protected String taskDescription() {
		return "Prepare Stratego code";
	}
	
	@Override
	public Path persistentPath() {
		return context.depPath("strategoCtree." + input.sdfmodule + "." + input.strmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result) throws IOException {
		BuildRequirement<?,?,?,?> rtg2Sig = new BuildRequirement<>(Rtg2Sig.factory, new Rtg2Sig.Input(context, input.sdfmodule, input.buildSdfImports), new SimpleMode());
		
		if (!context.isBuildStrategoEnabled(result))
			throw new IllegalArgumentException(context.props.substitute("Main stratego file '${strmodule}.str' not found."));
		
		require(CopyJar.factory, new CopyJar.Input(context, input.externaljar), new SimpleMode());
		
		RelativePath inputPath = context.basePath("${trans}/" + input.strmodule + ".str");
		RelativePath outputPath = context.basePath("${include}/" + input.strmodule + ".ctree");
		require(StrategoJavaCompiler.factory,
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
						Util.arrayAdd("-F", input.externaljarflags.split("[\\s]+")),
						Util.arrayAdd(rtg2Sig, input.requiredUnits)),
				new SimpleMode());
	}
}
