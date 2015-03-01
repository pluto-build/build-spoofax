package org.metaborg.spoofax.build.cleardep;

import java.io.Serializable;

import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class SpoofaxContext implements Serializable{
	private static final long serialVersionUID = -1973461199459693455L;
	
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
	
	public boolean isBuildStrategoEnabled(CompilationUnit result) {
		RelativePath strategoPath = basePath("${trans}/${strmodule}.str");
		result.addExternalFileDependency(strategoPath);
		boolean buildStrategoEnabled = FileCommands.exists(strategoPath);
		return buildStrategoEnabled;
	}
	
	public boolean isJavaJarEnabled(CompilationUnit result) {
		RelativePath mainPath = basePath("${src-gen}/org/strategoxt/imp/editors/template/strategies/Main.java");
		result.addExternalFileDependency(mainPath);
		return FileCommands.exists(mainPath);
	}
}
