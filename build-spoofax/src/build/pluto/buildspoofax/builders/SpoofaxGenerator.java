package build.pluto.buildspoofax.builders;

import java.io.File;

import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.spoofax.meta.core.MetaBuildInput;
import org.metaborg.spoofax.meta.core.SpoofaxMetaBuilder;
import org.metaborg.util.file.FileAccess;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;

import com.google.inject.Injector;

public class SpoofaxGenerator extends SpoofaxBuilder<SpoofaxInput, None> {
    public static SpoofaxBuilderFactory<SpoofaxInput, None, SpoofaxGenerator> factory = SpoofaxBuilderFactory.of(
        SpoofaxGenerator.class, SpoofaxInput.class);

    public SpoofaxGenerator(SpoofaxInput input) {
        super(input);
    }

    @Override protected String description(SpoofaxInput input) {
        return "Generating Spoofax project files";
    }

    @Override protected File persistentPath(SpoofaxInput input) {
        return context.depPath("generator.dep");
    }

    @Override protected None build(SpoofaxInput input) throws Throwable {
        final Injector injector = context.injector;
        final IProject project = injector.getInstance(IProjectService.class).get(context.settings.location());
        final SpoofaxMetaBuilder metaBuilder = injector.getInstance(SpoofaxMetaBuilder.class);
        final FileAccess access = new FileAccess();
        metaBuilder.generateSources(new MetaBuildInput(project, context.settings), access);
        processFileAccess(access);
        return None.val;
    }
}
