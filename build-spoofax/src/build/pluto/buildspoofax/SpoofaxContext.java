package build.pluto.buildspoofax;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import build.pluto.builder.Builder;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

public class SpoofaxContext implements Serializable{
	private static final long serialVersionUID = -1973461199459693455L;
	
	public final static boolean BETTER_STAMPERS = true;
	
	public final Path baseDir;
	public final Properties props;
	
	public SpoofaxContext(Path baseDir, Properties props) {
		this.baseDir = baseDir;
		this.props = props;
	}
	
	public RelativePath basePath(String relative) { 
		return new RelativePath(baseDir, props.substitute(relative));
	}
	
	public RelativePath depDir() { 
		return new RelativePath(baseDir, props.substitute("${include}/build"));
	}
	
	public RelativePath depPath(String relative) { 
		return new RelativePath(baseDir, props.substitute("${include}/build/" + relative));
	}
	
	public boolean isBuildStrategoEnabled(Builder<?, ?> result) {
		RelativePath strategoPath = basePath("${trans}/${strmodule}.str");
		result.require(strategoPath, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance : LastModifiedStamper.instance);
		boolean buildStrategoEnabled = FileCommands.exists(strategoPath);
		return buildStrategoEnabled;
	}
	
	public boolean isJavaJarEnabled(Builder<?, ?> result) {
		RelativePath mainPath = basePath("${src-gen}/org/strategoxt/imp/editors/template/strategies/Main.java");
		result.require(mainPath, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance : LastModifiedStamper.instance);
		return FileCommands.exists(mainPath);
	}
	
	
	
	
	
	public static Properties makeSpoofaxProperties(Path baseDir) {
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

//		props.put("eclipse.spoofaximp.nativeprefix", new NativePrefixAntPropertyProvider().getAntPropertyValue(null));
//		props.put("eclipse.spoofaximp.strategojar", new StrategoJarAntPropertyProvider().getAntPropertyValue(null));
//		props.put("eclipse.spoofaximp.strategominjar", new StrategoMinJarAntPropertyProvider().getAntPropertyValue(null));
//		props.put("eclipse.spoofaximp.jars", new JarsAntPropertyProvider().getAntPropertyValue(null));
//		props.put("externaljarx", new PluginClasspathProvider().getAntPropertyValue(null));
		
		String lang;
		Path[] sdfImports;
		RelativePath antBuildXML = new RelativePath(baseDir, "build.main.xml");
		try {
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(antBuildXML.getFile());
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
							List<Path> paths = new ArrayList<>();
							for (String imp : imports)
								if (!imp.isEmpty()) {
									String subst = props.substitute(imp);
									if (AbsolutePath.acceptable(subst))
										paths.add(new AbsolutePath(subst));
									else
										paths.add(new RelativePath(baseDir, subst));
								}
							sdfImports = paths.toArray(new Path[paths.size()]);
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
			for (Path imp : sdfImports)
				importString.append("-Idef " + props.substitute(imp.getAbsolutePath()));
			props.put("build.sdf.imports", importString.toString());
		}

		return props;
	}
	
	public static SpoofaxContext makeContext(Path projectPath) {
	    Log.log.setLoggingLevel(Log.ALWAYS);

		Properties props = makeSpoofaxProperties(projectPath);
		return new SpoofaxContext(projectPath, props);
	}
	
}
