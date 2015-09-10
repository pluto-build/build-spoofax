package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxProjectConstants;
import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.util.file.FileUtils;
import org.sugarj.common.util.ArrayUtils;

import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxBuilderFactoryFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.LastModifiedStamper;
import build.pluto.stamp.Stamper;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class StrategoCtree extends SpoofaxBuilder<StrategoCtree.Input, OutputPersisted<File>> {

    public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, StrategoCtree> factory =
        SpoofaxBuilderFactoryFactory.of(StrategoCtree.class, Input.class);

    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = 6323245405121428720L;

        public final String sdfmodule;
        public final String buildSdfImports;
        public final String strmodule;
        public final File externaljar;
        public final String externaljarflags;
        public final File externalDef;

        public final BuildRequest<?, ?, ?, ?>[] requiredUnits;

        public Input(SpoofaxContext context, String sdfmodule, String buildSdfImports, String strmodule,
            File externaljar, String externaljarflags, File externalDef, BuildRequest<?, ?, ?, ?>[] requiredUnits) {
            super(context);
            this.sdfmodule = sdfmodule;
            this.buildSdfImports = buildSdfImports;
            this.strmodule = strmodule;
            this.externaljar = externaljar;
            this.externaljarflags = externaljarflags;
            this.externalDef = externalDef;
            this.requiredUnits = requiredUnits;
        }
    }

    public StrategoCtree(Input input) {
        super(input);
    }

    @Override protected String description(Input input) {
        return "Prepare Stratego code";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("strategoCtree." + input.sdfmodule + "." + input.strmodule + ".dep");
    }

    @Override public Stamper defaultStamper() {
        return LastModifiedStamper.instance;
    }

    @Override public OutputPersisted<File> build(Input input) throws IOException {
        BuildRequest<Rtg2Sig.Input, None, Rtg2Sig, ?> rtg2Sig =
            new BuildRequest<>(Rtg2Sig.factory, new Rtg2Sig.Input(context, input.sdfmodule));

        if(!context.isBuildStrategoEnabled(this)) {
            final String strategoModule = context.settings.strategoName();
            throw new IllegalArgumentException(String.format("Main stratego file '%s' not found", strategoModule));
        }

        requireBuild(CopyJar.factory, new CopyJar.Input(context, input.externaljar));

        final File inputPath = FileUtils.toFile(context.settings.getStrMainFile());
        final File outputPath;
        final File depPath;
        if(context.settings.format() == Format.ctree) {
            outputPath = FileUtils.toFile(context.settings.getStrCompiledCtreeFile());
            depPath = outputPath;
        } else {
            outputPath = FileUtils.toFile(context.settings.getStrJavaMainFile());
            depPath = FileUtils.toFile(context.settings.getStrJavaTransDirectory());
        }
        final File cacheDir = FileUtils.toFile(context.settings.getCacheDirectory());

        final Collection<String> extraArgs = Lists.newLinkedList();
        if(context.settings.format() == Format.ctree) {
            extraArgs.add("-F");
        } else {
            extraArgs.add("-la java-front");
            if(context.isJavaJarEnabled(this)) {
                extraArgs.add("-la " + context.settings.strategiesPackageName());
            }
        }
        if(input.externaljarflags != null) {
            Collections.addAll(extraArgs, input.externaljarflags.split("[\\s]+"));
        }

        final Iterable<FileObject> paths =
            context.languagePathService.sourceAndIncludePaths(context.project,
                SpoofaxProjectConstants.LANG_STRATEGO_NAME);
        final Collection<File> includeDirs = Lists.newLinkedList();
        for(FileObject path : paths) {
            File file = context.resourceService.localFile(path);
            includeDirs.add(file);
        }

        // TODO: get libraries from stratego arguments
        requireBuild(StrategoJavaCompiler.factory, new StrategoJavaCompiler.Input(context, inputPath, outputPath, depPath,
            "trans", true, true, Iterables.toArray(includeDirs, File.class), new String[] { "stratego-lib",
                "stratego-sglr", "stratego-gpp", "stratego-xtc", "stratego-aterm", "stratego-sdf", "strc" }, cacheDir,
            extraArgs.toArray(new String[0]), ArrayUtils.arrayAdd(rtg2Sig, input.requiredUnits)));

        return OutputPersisted.of(outputPath);
    }
}
