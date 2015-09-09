package build.pluto.buildspoofax;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.util.file.FileUtils;
import org.sugarj.common.FileCommands;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import build.pluto.builder.Builder;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

import com.google.inject.Injector;

public class SpoofaxContext implements Serializable {
    private static final long serialVersionUID = -1973461199459693455L;

    public final static boolean BETTER_STAMPERS = true;

    public final File baseDir;
    public final Properties props;
    public final SpoofaxProjectSettings settings;

    public SpoofaxContext(Injector injector, SpoofaxProjectSettings settings) {
        this.guiceInjector = injector;
        this.baseDir = new File(settings.location().getName().getPath());
        this.props = makeSpoofaxProperties(baseDir);
        this.settings = settings;

    }

    public File basePath(String relative) {
        final String relativeSubst = props.substitute(relative);
        return new File(baseDir, relativeSubst);
    }

    public File depDir() {
        return new File(baseDir, props.substitute("${include}/build"));
    }

    public File depPath(String relative) {
        return new File(baseDir, props.substitute("${include}/build/" + relative));
    }

    public boolean isBuildStrategoEnabled(Builder<?, ?> result) {
        final File strategoPath = FileUtils.toFile(settings.getStrategoMainFile());
        result.require(strategoPath, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance
            : LastModifiedStamper.instance);
        boolean buildStrategoEnabled = FileCommands.exists(strategoPath);
        return buildStrategoEnabled;
    }

    public boolean isJavaJarEnabled(Builder<?, ?> result) {
        final File mainFile = FileUtils.toFile(settings.getStrategoJavaStrategiesMainFile());
        result.require(mainFile, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance
            : LastModifiedStamper.instance);
        return FileCommands.exists(mainFile);
    }


    private static String unquote(String s) {
        if(s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"') {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }


    public static Properties makeSpoofaxProperties(SpoofaxProjectSettings settings) {
        Properties props = new Properties();
        props.put("trans", "trans");
        props.put("src-gen", "editor/java");
        props.put("syntax", "src-gen/syntax");
        props.put("include", "include");
        props.put("lib", "lib");
        props.put("build", "target/classes");

        props.put("sdfmodule", lang);

        if(sdfImports != null) {
            StringBuilder importString = new StringBuilder();
            for(File imp : sdfImports) {

                importString.append("-Idef " + props.substitute(imp.getAbsolutePath()));
            }
            props.put("build.sdf.imports", importString.toString());
        }

        return props;
    }

    private final Injector guiceInjector;

    public Injector injector() {
        return guiceInjector;
    }

    public IResourceService getResourceService() {
        return guiceInjector.getInstance(IResourceService.class);
    }
}
