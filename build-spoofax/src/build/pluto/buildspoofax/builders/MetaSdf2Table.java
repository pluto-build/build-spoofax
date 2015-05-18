package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;

import org.sugarj.common.FileCommands;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

public class MetaSdf2Table extends SpoofaxBuilder<MetaSdf2Table.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, MetaSdf2Table> factory = SpoofaxBuilderFactory.of(MetaSdf2Table.class, Input.class);

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -3179663405417276186L;
		
		public final String metasdfmodule;
		public final String buildSdfImports;
		public final File externaldef;

		public Input(SpoofaxContext context, String metasdfmodule, String buildSdfImports, File externaldef) {
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
	protected String description(Input input) {
		return "Compile metagrammar for concrete object syntax";
	}
	
	@Override
	protected File persistentPath(Input input) {
		return context.depPath("metaSdf2Table." + input.metasdfmodule + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		File metamodule = context.basePath("${syntax}/${metasdfmodule}.sdf");
		require(metamodule, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance : LastModifiedStamper.instance);
		boolean metasdfmoduleAvailable = FileCommands.exists(metamodule);
		
		if (metasdfmoduleAvailable) {
			requireBuild(CopyUtils.factory, input);
			File strategoMixDef = context.basePath("utils/StrategoMix.def");
			
			String sdfImports = "-Idef " + strategoMixDef + " " + input.buildSdfImports;
			requireBuild(Sdf2Table.factory, new Sdf2Table.Input(context, input.metasdfmodule, sdfImports, input.externaldef));
		}
		
		return None.val;
	}

}
