package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.metaborg.util.file.FileUtils;
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
		File metamodule = FileUtils.toFile(context.settings.getMetaSdfMainFile());
		require(metamodule, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance : LastModifiedStamper.instance);
		boolean metasdfmoduleAvailable = FileCommands.exists(metamodule);
		
		if (metasdfmoduleAvailable) {
		    InputStream stream = this.getClass().getResourceAsStream("StrategoMix.def");
		    File strategoMixDef = new File(context.basePath("utils"), "StrategoMix.def");
		    FileCommands.createFile(strategoMixDef);
		    IOUtils.copy(stream, new FileOutputStream(strategoMixDef));
		    provide(strategoMixDef);
			
			String sdfImports = "-Idef " + strategoMixDef + " " + input.buildSdfImports;
			requireBuild(Sdf2Table.factory, new Sdf2Table.Input(context, input.metasdfmodule, sdfImports, input.externaldef));
		}
		
		return None.val;
	}

}
