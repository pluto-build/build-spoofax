package build.pluto.buildspoofax;

import java.io.File;
import java.io.Serializable;

import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.util.file.FileUtils;
import org.sugarj.common.FileCommands;

import build.pluto.builder.Builder;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

import com.google.inject.Injector;

public class SpoofaxContext implements Serializable {
    private static final long serialVersionUID = -1973461199459693455L;

    public final static boolean BETTER_STAMPERS = true;

    public transient final SpoofaxProjectSettings settings;
    public final File baseDir;
    public final File depDir;

    public transient final Injector injector;
    public transient final IResourceService resourceService;


    public SpoofaxContext(Injector injector, SpoofaxProjectSettings settings) {
        this.settings = settings;
        this.baseDir = FileUtils.toFile(settings.location());
        this.depDir = FileUtils.toFile(settings.getBuildDirectory());

        this.injector = injector;
        this.resourceService = injector.getInstance(IResourceService.class);
    }


    public File basePath(String relative) {
        return new File(baseDir, relative);
    }
    
    public File depPath(String relative) {
        return new File(depDir, relative);
    }
    

    public boolean isBuildStrategoEnabled(Builder<?, ?> result) {
        final File strategoPath = FileUtils.toFile(settings.getStrMainFile());
        result.require(strategoPath, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance
            : LastModifiedStamper.instance);
        boolean buildStrategoEnabled = FileCommands.exists(strategoPath);
        return buildStrategoEnabled;
    }

    public boolean isJavaJarEnabled(Builder<?, ?> result) {
        final File mainFile = FileUtils.toFile(settings.getStrJavaStrategiesMainFile());
        result.require(mainFile, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance
            : LastModifiedStamper.instance);
        return FileCommands.exists(mainFile);
    }
}
