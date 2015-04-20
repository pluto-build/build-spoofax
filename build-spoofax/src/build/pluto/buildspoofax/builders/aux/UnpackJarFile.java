package build.pluto.buildspoofax.builders.aux;

import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.stamp.LastModifiedStamper;

public class UnpackJarFile extends SpoofaxBuilder<UnpackJarFile.Input, Path> {

	public static SpoofaxBuilderFactory<Input, Path, UnpackJarFile> factory = new SpoofaxBuilderFactory<Input, Path, UnpackJarFile>() {
		private static final long serialVersionUID = -5071622884621295511L;

		@Override
		public UnpackJarFile makeBuilder(Input input) { return new UnpackJarFile(input); }
	};
	

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 12331766781256062L;

		public final Path jarfile;
		public final Path outdir;
		public Input(SpoofaxContext context, Path jarfile, Path outdir) {
			super(context);
			this.jarfile = jarfile;
			this.outdir = outdir;
		}
	}
	
	public UnpackJarFile(Input input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Unpack jarfile " + FileCommands.fileName(input.jarfile);
	}
	
	@Override
	public Path persistentPath() {
		return context.depPath("unpack.jar." + FileCommands.fileName(input.jarfile) + ".dep");
	}

	@Override
	public Path build() throws Exception {
		require(input.jarfile, LastModifiedStamper.instance);
		Path dir = input.outdir != null ? input.outdir : FileCommands.newTempDir();
		FileCommands.unpackJarfile(dir, input.jarfile);
		for (Path p : FileCommands.listFilesRecursive(dir))
			require(p, LastModifiedStamper.instance);
		return dir;
		
	}
}
