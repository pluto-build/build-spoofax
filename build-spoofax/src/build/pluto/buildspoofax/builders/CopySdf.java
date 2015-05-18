package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

import org.sugarj.common.FileCommands;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.stamp.LastModifiedStamper;

public class CopySdf extends SpoofaxBuilder<CopySdf.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, CopySdf> factory = SpoofaxBuilderFactory.of(CopySdf.class, Input.class);
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 6298820503718314523L;

		public final String sdfmodule;
		public final File externaldef;

		public Input(SpoofaxContext context, String sdfmodule, File externaldef) {
			super(context);
			this.sdfmodule = sdfmodule;
			this.externaldef = externaldef;
		}
	}
	
	public CopySdf(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Copy external grammar definition.";
	}
	
	@Override
	public File persistentPath(Input input) {
		if (input.externaldef != null)
			return context.depPath("copySdf." + input.externaldef + "." + input.sdfmodule + ".dep");
		return context.depPath("copySdf." + input.sdfmodule + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		if (input.externaldef != null) {
			File target = context.basePath("${include}/" + input.sdfmodule + ".def");
			require(input.externaldef, LastModifiedStamper.instance);
			FileCommands.copyFile(input.externaldef, target, StandardCopyOption.COPY_ATTRIBUTES);
			provide(target);
		}
		return None.val;
	}
}
