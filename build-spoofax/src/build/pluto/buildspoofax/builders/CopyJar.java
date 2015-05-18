package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.sugarj.common.FileCommands;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.stamp.LastModifiedStamper;

public class CopyJar extends SpoofaxBuilder<CopyJar.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, CopyJar> factory = SpoofaxBuilderFactory.of(CopyJar.class, Input.class);
	

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 8710048518971598430L;
		public final File externaljar;

		public Input(SpoofaxContext context, File externaljar) {
			super(context);
			this.externaljar = externaljar;
		}
	}
	
	public CopyJar(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Copy external Jar";
	}
	
	@Override
	public File persistentPath(Input input) {
		if (input.externaljar != null) {
			Path rel = FileCommands.getRelativePath(context.baseDir, input.externaljar);
			String relname = rel.toString().replace(File.separatorChar, '_');
			return context.depPath("copyJar." + relname + ".dep");
		}
		return context.depPath("copyJar.dep");
	}

	@Override
	public None build(Input input) throws IOException {
		if (input.externaljar != null) {
			File target = context.basePath("${include}/" + input.externaljar.getName());
			require(input.externaljar, LastModifiedStamper.instance);
			FileCommands.copyFile(input.externaljar, target, StandardCopyOption.COPY_ATTRIBUTES);
			provide(target);
		}
		return None.val;
	}
}
