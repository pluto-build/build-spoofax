package build.pluto.buildspoofax.builders.aux;

import java.io.File;
import java.nio.file.Path;

import org.sugarj.common.FileCommands;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.Out;
import build.pluto.stamp.LastModifiedStamper;

public class UnpackJarFile extends SpoofaxBuilder<UnpackJarFile.Input, Out<File>> {

	public static SpoofaxBuilderFactory<Input, Out<File>, UnpackJarFile> factory = SpoofaxBuilderFactory.of(UnpackJarFile.class, Input.class);
	

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 12331766781256062L;

		public final File jarfile;
		public final File outdir;

		public Input(SpoofaxContext context, File jarfile, File outdir) {
			super(context);
			this.jarfile = jarfile;
			this.outdir = outdir;
		}
	}
	
	public UnpackJarFile(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Unpack jarfile " + FileCommands.fileName(input.jarfile);
	}
	
	@Override
	public File persistentPath(Input input) {
		return context.depPath("unpack.jar." + FileCommands.fileName(input.jarfile) + ".dep");
	}

	@Override
	public Out<File> build(Input input) throws Exception {
		require(input.jarfile, LastModifiedStamper.instance);
		File dir = input.outdir != null ? input.outdir : FileCommands.newTempDir();
		FileCommands.unpackJarfile(dir, input.jarfile);
		for (Path p : FileCommands.listFilesRecursive(dir.toPath()))
			provide(p.toFile(), LastModifiedStamper.instance);
		return Out.of(dir);
		
	}
}
