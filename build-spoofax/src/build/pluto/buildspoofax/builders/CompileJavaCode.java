package build.pluto.buildspoofax.builders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.buildjava.JavaBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.util.FileExtensionFilter;
import build.pluto.output.None;

public class CompileJavaCode extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, CompileJavaCode> factory = new SpoofaxBuilderFactory<SpoofaxInput, None, CompileJavaCode>() {
		private static final long serialVersionUID = 5448125602790119713L;

		@Override
		public CompileJavaCode makeBuilder(SpoofaxInput input) { return new CompileJavaCode(input); }
	};
	
	public CompileJavaCode(SpoofaxInput input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Compile Java code for Spoofax";
	}
	
	@Override
	public Path persistentPath() {
		return context.depPath("compileJavaCode.dep");
	}

	@Override
	public None build() throws IOException {
		requireBuild(CopyUtils.factory, input);
		
		Path targetDir = context.basePath("${build}");
		boolean debug = true;
		String sourceVersion = "1.7";
		String targetVersion = "1.7";
		List<String> additionalArgs = new ArrayList<>();
		if (debug)
			additionalArgs.add("-g");
		additionalArgs.add("-source");
		additionalArgs.add(sourceVersion);
		additionalArgs.add("-target");
		additionalArgs.add(targetVersion);
		
		String srcDirs = context.props.getOrElse("src-dirs", context.props.get("src-gen"));
		List<Path> sourcePath = new ArrayList<>();
		List<Path> sourceFiles = new ArrayList<>();
		for (String dir : srcDirs.split("[\\s]+")) {
			Path p;
			if (AbsolutePath.acceptable(dir))
				p = new AbsolutePath(dir);
			else
				p = context.basePath(dir);
			
			sourcePath.add(p);
			
			// TODO soundly select non-Eclipse files
			for (RelativePath sourceFile : FileCommands.listFilesRecursive(p, new FileExtensionFilter("java"))) {
				if (sourceFile.getRelativePath().contains("ParseController") || sourceFile.getRelativePath().contains("Validator"))
					continue;
				
				String content = FileCommands.readFileAsString(sourceFile);
				if (!content.contains("org.eclipse"))
					sourceFiles.add(sourceFile);
			}
		}
		
		
		List<Path> classPath = new ArrayList<>();
		classPath.add(context.basePath("utils/strategoxt.jar"));
		classPath.add(context.basePath("${src-gen}"));
		if (context.props.isDefined("externaljar"))
			classPath.add(new AbsolutePath(context.props.get("externaljar")));
		if (context.props.isDefined("externaljarx"))
			classPath.add(new AbsolutePath(context.props.get("externaljarx")));
		if (context.isJavaJarEnabled(this))
			classPath.add(context.basePath("${include}/${strmodule}-java.jar"));

		requireBuild(JavaBuilder.factory, 
				new JavaBuilder.Input(
						sourceFiles.toArray(new Path[sourceFiles.size()]),
						targetDir,
						sourcePath.toArray(new Path[sourcePath.size()]), 
						classPath.toArray(new Path[classPath.size()]),
						additionalArgs.toArray(new String[additionalArgs.size()]),
						null,
						false));
		
		return None.val;
	}
}