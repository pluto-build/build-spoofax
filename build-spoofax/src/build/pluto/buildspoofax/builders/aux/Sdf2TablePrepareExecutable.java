package build.pluto.buildspoofax.builders.aux;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.metaborg.spoofax.nativebundle.NativeBundle;
import org.sugarj.common.Exec;
import org.sugarj.common.FileCommands;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.util.ExecutableCommandStrategy;
import build.pluto.stamp.LastModifiedStamper;

import com.google.common.base.Objects;

public class Sdf2TablePrepareExecutable extends SpoofaxBuilder<SpoofaxInput, Sdf2TablePrepareExecutable.Output> {

    public static SpoofaxBuilderFactory<SpoofaxInput, Output, Sdf2TablePrepareExecutable> factory =
        SpoofaxBuilderFactory.of(Sdf2TablePrepareExecutable.class, SpoofaxInput.class);

    public static class Output implements build.pluto.output.Output {
        private static final long serialVersionUID = -6018464107000421068L;

        public final ExecutableCommandStrategy sdf2table;
        public final ExecutableCommandStrategy implodePT;

        public Output(ExecutableCommandStrategy sdf2table, ExecutableCommandStrategy implodePT) {
            super();
            this.sdf2table = sdf2table;
            this.implodePT = implodePT;
        }

        @Override public boolean equals(Object obj) {
            if(!(obj instanceof Output))
                return false;

            Output other = (Output) obj;
            return Objects.equal(implodePT, other.implodePT) && Objects.equal(sdf2table, other.sdf2table);
        }

        @Override public int hashCode() {
            return Objects.hashCode(implodePT, sdf2table);
        }
    }

    public Sdf2TablePrepareExecutable(SpoofaxInput input) {
        super(input);
    }

    @Override protected String description(SpoofaxInput input) {
        return "Prepare sdf2table exeuctable";
    }

    @Override protected File persistentPath(SpoofaxInput input) {
        return context.depPath("sdf2Table.executable.dep");
    }

    @Override public Output build(SpoofaxInput input) throws IOException {
        final URI implodePTUri = NativeBundle.getImplodePT();
        final URI sdf2TableUri = NativeBundle.getSdf2Table();

        require(FileCommands.getRessourcePath(NativeBundle.class).toFile(), LastModifiedStamper.instance);
        try(InputStream sdf2tableInput = sdf2TableUri.toURL().openStream();
            InputStream implodePTInput = implodePTUri.toURL().openStream()) {

            File sdf2table = context.basePath("include/build/native/sdf2table");
            FileCommands.createFile(sdf2table);
            try(OutputStream output = new FileOutputStream(sdf2table.getAbsolutePath())) {
                IOUtils.copy(sdf2tableInput, output);
            }

            File implodePT = context.basePath("include/build/native/implodePT");
            FileCommands.createFile(implodePT);
            try(OutputStream output = new FileOutputStream(implodePT.getAbsolutePath())) {
                IOUtils.copy(implodePTInput, output);
            }

            if(SystemUtils.IS_OS_UNIX) {
                Exec.run(context.baseDir, "/bin/sh", "-c", "chmod +x \"" + sdf2table.getAbsolutePath() + "\"");
                Exec.run(context.baseDir, "/bin/sh", "-c", "chmod +x \"" + implodePT.getAbsolutePath() + "\"");
            } else {
                throw new UnsupportedOperationException("Only Unix systems at the moment.");
            }

            provide(sdf2table, LastModifiedStamper.instance);
            provide(implodePT, LastModifiedStamper.instance);

            return new Output(ExecutableCommandStrategy.getInstance("sdf2table", sdf2table),
                ExecutableCommandStrategy.getInstance("implodePT", implodePT));
        }
    }


}
