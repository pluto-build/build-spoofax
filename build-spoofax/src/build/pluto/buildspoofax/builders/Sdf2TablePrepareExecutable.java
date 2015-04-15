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

public class Sdf2TablePrepareExecutable extends
		SpoofaxBuilder<SpoofaxInput, Path> {

	public static SpoofaxBuilderFactory<SpoofaxInput, Path, Sdf2TablePrepareExecutable> factory = new SpoofaxBuilderFactory<SpoofaxInput, Path, Sdf2TablePrepareExecutable>() {
		private static final long serialVersionUID = -5551917492018980172L;

		@Override
		public Sdf2TablePrepareExecutable makeBuilder(SpoofaxInput input) {
			return new Sdf2TablePrepareExecutable(input);
		}
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
		String subdir;
		if(SystemUtils.IS_OS_LINUX)
			subdir = "linux";
		else if(SystemUtils.IS_OS_WINDOWS)
			subdir = "cygwin";
		else if(SystemUtils.IS_OS_MAC)
			subdir = "macosx";
		else
			throw new RuntimeException("Unsupported operating system");
			
		InputStream sdf2tableInput = Dummy.class.getClassLoader().getResourceAsStream("native/"+subdir+"/sdf2table");
		InputStream implodePTInput = Dummy.class.getClassLoader().getResourceAsStream("native/"+subdir+"/implodePT");
		// TODO require jar file containing "Dummy"
		
		Path sdf2table = context.basePath("include/build/native/sdf2table");
		FileCommands.createFile(sdf2table);
		IOUtils.copy(sdf2tableInput, new FileOutputStream(sdf2table.getAbsolutePath()));
		
		Path implodePT = context.basePath("include/build/native/implodePT");
		FileCommands.createFile(implodePT);
		IOUtils.copy(implodePTInput, new FileOutputStream(implodePT.getAbsolutePath()));
		
		if (SystemUtils.IS_OS_UNIX) {
			Exec.run(context.baseDir, "/bin/sh", "-c", "chmod +x \"" + sdf2table.getAbsolutePath() + "\"");
			Exec.run(context.baseDir, "/bin/sh", "-c", "chmod +x \"" + implodePT.getAbsolutePath() + "\"");
		}
		
		provide(sdf2table, LastModifiedStamper.instance);
		provide(implodePT, LastModifiedStamper.instance);
		return sdf2table;
	}
}
