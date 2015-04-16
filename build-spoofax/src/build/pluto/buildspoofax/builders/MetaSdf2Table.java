package build.pluto.buildspoofax.builders;

import java.io.IOException;

import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

public class MetaSdf2Table extends SpoofaxBuilder<MetaSdf2Table.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, MetaSdf2Table> factory = new SpoofaxBuilderFactory<Input, None, MetaSdf2Table>() {
		private static final long serialVersionUID = 5848449529745147614L;

		@Override
		public MetaSdf2Table makeBuilder(Input input) { return new MetaSdf2Table(input); }
	};

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -3179663405417276186L;
		
		public final String metasdfmodule;
		public final String buildSdfImports;
		public final Path externaldef;
		public Input(SpoofaxContext context, String metasdfmodule, String buildSdfImports, Path externaldef) {
			super(context);
			this.metasdfmodule = metasdfmodule;
			this.buildSdfImports = buildSdfImports;
			this.externaldef = externaldef;
		}
	}
	
	public MetaSdf2Table(Input input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Compile metagrammar for concrete object syntax";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("metaSdf2Table." + input.metasdfmodule + ".dep");
	}

	@Override
	public None build() throws IOException {
		RelativePath metamodule = context.basePath("${syntax}/${metasdfmodule}.sdf");
		require(metamodule, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance : LastModifiedStamper.instance);
		boolean metasdfmoduleAvailable = FileCommands.exists(metamodule);
		
		if (metasdfmoduleAvailable) {
			requireBuild(CopyUtils.factory, input);
			Path strategoMixDef = context.basePath("utils/StrategoMix.def");
			
			String sdfImports = "-Idef " + strategoMixDef + " " + input.buildSdfImports;
			requireBuild(Sdf2Table.factory, new Sdf2Table.Input(context, input.metasdfmodule, sdfImports, input.externaldef));
		}
		
		return None.val;
	}

}
