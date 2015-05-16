package build.pluto.buildspoofax.builders.aux;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.strategoxt.imp.nativebundle.Dummy;
import org.sugarj.common.Exec;
import org.sugarj.common.FileCommands;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.util.ExecutableCommandStrategy;
import build.pluto.stamp.LastModifiedStamper;

import com.google.common.base.Objects;

public class Sdf2TablePrepareExecutable extends SpoofaxBuilder<SpoofaxInput, Sdf2TablePrepareExecutable.Output> {

	public static SpoofaxBuilderFactory<SpoofaxInput, Output, Sdf2TablePrepareExecutable> factory = Sdf2TablePrepareExecutable::new;

	public static class Output implements build.pluto.output.Output {
		private static final long serialVersionUID = -6018464107000421068L;
		
		public final ExecutableCommandStrategy sdf2table;
		public final ExecutableCommandStrategy implodePT;
		public Output(ExecutableCommandStrategy sdf2table, ExecutableCommandStrategy implodePT) {
			super();
			this.sdf2table = sdf2table;
			this.implodePT = implodePT;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Output))
				return false;
			
			Output other = (Output) obj;
			return Objects.equal(implodePT, other.implodePT) && Objects.equal(sdf2table, other.sdf2table);
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(implodePT, sdf2table);
		}
	}
	
	public Sdf2TablePrepareExecutable(SpoofaxInput input) {
		super(input);
	}

	@Override
	protected String description(SpoofaxInput input) {
		return "Prepare sdf2table exeuctable";
	}

	@Override
	protected File persistentPath(SpoofaxInput input) {
		return context.depPath("sdf2Table.executable.dep");
	}

	@Override
	public Output build(SpoofaxInput input) throws IOException {
		String subdir;
		if(SystemUtils.IS_OS_LINUX)
			subdir = "linux";
		else if(SystemUtils.IS_OS_WINDOWS)
			subdir = "cygwin";
		else if(SystemUtils.IS_OS_MAC)
			subdir = "macosx";
		else
			throw new RuntimeException("Unsupported operating system");

		require(FileCommands.getRessourcePath(Dummy.class).toFile(), LastModifiedStamper.instance);
		InputStream sdf2tableInput = Dummy.class.getClassLoader().getResourceAsStream("native/"+subdir+"/sdf2table");
		InputStream implodePTInput = Dummy.class.getClassLoader().getResourceAsStream("native/"+subdir+"/implodePT");
		
		File sdf2table = context.basePath("include/build/native/sdf2table");
		FileCommands.createFile(sdf2table);
		IOUtils.copy(sdf2tableInput, new FileOutputStream(sdf2table.getAbsolutePath()));
		
		File implodePT = context.basePath("include/build/native/implodePT");
		FileCommands.createFile(implodePT);
		IOUtils.copy(implodePTInput, new FileOutputStream(implodePT.getAbsolutePath()));
		
		if (SystemUtils.IS_OS_UNIX) {
			Exec.run(context.baseDir, "/bin/sh", "-c", "chmod +x \"" + sdf2table.getAbsolutePath() + "\"");
			Exec.run(context.baseDir, "/bin/sh", "-c", "chmod +x \"" + implodePT.getAbsolutePath() + "\"");
		}
		
		provide(sdf2table, LastModifiedStamper.instance);
		provide(implodePT, LastModifiedStamper.instance);

		return new Output(
				ExecutableCommandStrategy.getInstance("sdf2table", sdf2table),
				ExecutableCommandStrategy.getInstance("implodePT", implodePT));
	}
	
	
}
