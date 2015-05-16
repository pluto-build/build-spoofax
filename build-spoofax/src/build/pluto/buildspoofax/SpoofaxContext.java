package build.pluto.buildspoofax;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.metaborg.spoofax.core.resource.IResourceService;
import org.sugarj.common.FileCommands;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import build.pluto.builder.Builder;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class SpoofaxContext implements Serializable{
	private static final long serialVersionUID = -1973461199459693455L;
	
	public final static boolean BETTER_STAMPERS = true;
	
	public final File baseDir;
	public final Properties props;
	
	public SpoofaxContext(File baseDir, Properties props) {
		this.baseDir = baseDir;
		this.props = props;
	}
	
	public File basePath(String relative) {
		return new File(baseDir, props.substitute(relative));
	}
	
	public File depDir() {
		return new File(baseDir, props.substitute("${include}/build"));
	}
	
	public File depPath(String relative) {
		return new File(baseDir, props.substitute("${include}/build/" + relative));
	}
	
	public boolean isBuildStrategoEnabled(Builder<?, ?> result) {
		File strategoPath = basePath("${trans}/${strmodule}.str");
		result.require(strategoPath, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance : LastModifiedStamper.instance);
		boolean buildStrategoEnabled = FileCommands.exists(strategoPath);
		return buildStrategoEnabled;
	}
	
	public boolean isJavaJarEnabled(Builder<?, ?> result) {
		File mainPath = basePath("${src-gen}/org/strategoxt/imp/editors/template/strategies/Main.java");
		result.require(mainPath, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance : LastModifiedStamper.instance);
		return FileCommands.exists(mainPath);
	}
	
	
	
	
	
	public static Properties makeSpoofaxProperties(File baseDir) {
		Properties props = new Properties();
		props.put("trans", "trans");
		props.put("src-gen", "editor/java");
		props.put("syntax", "src-gen/syntax");
		props.put("include", "include");
		props.put("lib", "lib");
		props.put("build", "target/classes");
		props.put("dist", "bin/dist");
		props.put("pp", "src-gen/pp");
		props.put("signatures", "src-gen/signatures");
		props.put("completions", "src-gen/completions");
		props.put("sdf-src-gen", "src-gen");
		props.put("lib-gen", "include");

//		props.put("externaljarx", new PluginClasspathProvider().getAntPropertyValue(null));
		
		String lang;
		File[] sdfImports;
		File antBuildXML = new File(baseDir, "build.main.xml");
		try {
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(antBuildXML);
			Element project = doc.getDocumentElement();
			lang = project.getAttribute("name");
			
			Node kid = project.getFirstChild();
			sdfImports = null;
			while (kid != null) {
				if ("property".equals(kid.getNodeName()) && kid.hasAttributes()) {
					Node name = kid.getAttributes().getNamedItem("name");
					if (name != null && "build.sdf.imports".equals(name.getNodeValue())) {
						Node value = kid.getAttributes().getNamedItem("value");
						if (value != null) {
							String[] imports = value.getNodeValue().split("[\\s]*" + Pattern.quote("-Idef") + "[\\s]+");
							List<File> paths = new ArrayList<>();
							for (String imp : imports)
								if (!imp.isEmpty()) {
									String subst = props.substitute(imp);
									if (FileCommands.acceptableAsAbsolute(subst))
										paths.add(new File(subst));
									else
										paths.add(new File(baseDir, subst));
								}
							sdfImports = paths.toArray(new File[paths.size()]);
							break;
						}
					}
				}
				kid = kid.getNextSibling();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		
		props.put("basedir", baseDir.getAbsolutePath());
		props.put("sdfmodule", lang);
		props.put("metasdfmodule", "Stratego-" + lang);
		props.put("esvmodule", lang);
		props.put("strmodule", lang.toLowerCase());
		props.put("ppmodule", lang + "-pp");
		props.put("sigmodule", lang + "-sig");
		
		if (sdfImports != null) {
			StringBuilder importString = new StringBuilder();
			for (File imp : sdfImports)
				importString.append("-Idef " + props.substitute(imp.getAbsolutePath()));
			props.put("build.sdf.imports", importString.toString());
		}

		return props;
	}
	
	public static SpoofaxContext makeContext(File projectPath) {
		Properties props = makeSpoofaxProperties(projectPath);
		return new SpoofaxContext(projectPath, props);
	}

	private static Injector guiceInjector;
	private static IResourceService resourceService;
	public synchronized Injector guiceInjector() {
		if (guiceInjector != null)
			return guiceInjector;
		guiceInjector = Guice.createInjector(new SpoofaxPlutoModule(baseDir));
		return guiceInjector;
	}
	public synchronized IResourceService getResourceService() {
		if (resourceService != null)
			return resourceService;
		resourceService = guiceInjector().getInstance(IResourceService.class);
		return resourceService;
	}

}
