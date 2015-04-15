package build.pluto.buildspoofax.builders;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.strategoxt.imp.nativebundle.Dummy;
import org.sugarj.common.Exec;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.stamp.LastModifiedStamper;

public class Sdf2TablePrepareExecutable extends SpoofaxBuilder<SpoofaxInput, Path> {


	public static SpoofaxBuilderFactory<SpoofaxInput, Path, Sdf2TablePrepareExecutable> factory = new SpoofaxBuilderFactory<SpoofaxInput, Path, Sdf2TablePrepareExecutable>() {
		private static final long serialVersionUID = -5551917492018980172L;

		@Override
		public Sdf2TablePrepareExecutable makeBuilder(SpoofaxInput input) { return new Sdf2TablePrepareExecutable(input); }
	};
	

	public Sdf2TablePrepareExecutable(SpoofaxInput input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Prepare sdf2table exeuctable";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("sdf2Table.executable.dep");
	}

	@Override
	public Path build() throws IOException {
		InputStream input = Dummy.class.getClassLoader().getResourceAsStream("native/macosx/sdf2table");
		// TODO require jar file containing "Dummy"
		
		Path exe = context.basePath("./include/build/native/sdf2table");
		FileCommands.createFile(exe);
		IOUtils.copy(input, new FileOutputStream(exe.getAbsolutePath()));
		if (SystemUtils.IS_OS_UNIX)
			Exec.run(context.baseDir, "/bin/sh", "-c", "chmod +x \"" + exe.getAbsolutePath() + "\"");
		
		provide(exe, LastModifiedStamper.instance);
		return exe;
	}

}
