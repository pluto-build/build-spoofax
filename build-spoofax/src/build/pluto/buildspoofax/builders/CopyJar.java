package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.stamp.LastModifiedStamper;

public class CopyJar extends SpoofaxBuilder<CopyJar.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, CopyJar> factory = new SpoofaxBuilderFactory<Input, None, CopyJar>() {
		private static final long serialVersionUID = -8387363389037442076L;

		@Override
		public CopyJar makeBuilder(Input input) { return new CopyJar(input); }
	};
	

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 8710048518971598430L;
		public final Path externaljar;
		public Input(SpoofaxContext context, Path externaljar) {
			super(context);
			this.externaljar = externaljar;
		}
	}
	
	public CopyJar(Input input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Copy external Jar";
	}
	
	@Override
	public Path persistentPath() {
		if (input.externaljar != null) {
			RelativePath rel = FileCommands.getRelativePath(context.baseDir, input.externaljar);
			String relname = rel.getRelativePath().replace(File.separatorChar, '_');
			return context.depPath("copyJar." + relname + ".dep");
		}
		return context.depPath("copyJar.dep");
	}

	@Override
	public None build() throws IOException {
		if (input.externaljar != null) {
			Path target = context.basePath("${include}/" + FileCommands.dropDirectory(input.externaljar));
			require(input.externaljar, LastModifiedStamper.instance);
			FileCommands.copyFile(input.externaljar, target, StandardCopyOption.COPY_ATTRIBUTES);
			provide(target);
		}
		return None.val;
	}
}