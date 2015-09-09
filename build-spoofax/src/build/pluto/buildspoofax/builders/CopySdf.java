package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

import org.metaborg.util.file.FileUtils;
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

		public final String sdfModule;
		
		public Input(SpoofaxContext context, String sdfModule) {
			super(context);
            this.sdfModule = sdfModule;
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
	    final String externalDef = context.settings.externalDef();
		if (externalDef != null)
			return context.depPath("copySdf." + externalDef + "." + input.sdfModule + ".dep");
		return context.depPath("copySdf." + input.sdfModule + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
	    final String externalDef = context.settings.externalDef();
		if (externalDef != null) {
		    final File externalDefFile = new File(externalDef);
			File target = FileUtils.toFile(context.settings.getOutputDirectory().resolveFile(input.sdfModule + ".def"));
			require(externalDefFile, LastModifiedStamper.instance);
			FileCommands.copyFile(externalDefFile, target, StandardCopyOption.COPY_ATTRIBUTES);
			provide(target);
		}
		return None.val;
	}
}
