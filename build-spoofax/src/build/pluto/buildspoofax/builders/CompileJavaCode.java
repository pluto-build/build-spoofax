package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.CopyUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.builder.BuilderFactory;
import build.pluto.buildjava.JavaBuilder;
import build.pluto.buildjava.JavaInput;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;

public class CompileJavaCode extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, CompileJavaCode> factory = SpoofaxBuilderFactory.of(CompileJavaCode.class, SpoofaxInput.class);

	public CompileJavaCode(SpoofaxInput input) {
		super(input);
	}

	@Override
	protected String description(SpoofaxInput input) {
		return "Compile Java code for Spoofax";
	}

	@Override
	public File persistentPath(SpoofaxInput input) {
		return context.depPath("compileJavaCode.dep");
	}

	@Override
	public None build(SpoofaxInput input) throws IOException {
		File targetDir = context.basePath("${build}");
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
		List<File> sourcePath = new ArrayList<>();
		List<File> sourceFiles = new ArrayList<>();
		for (String dir : srcDirs.split("[\\s]+")) {
			File p;
			if (FileCommands.acceptableAsAbsolute(dir))
				p = new File(dir);
			else
				p = context.basePath(dir);

			sourcePath.add(p);

			// TODO soundly select non-Eclipse files
			for (Path sourceFile : FileCommands.listFilesRecursive(p.toPath(), new SuffixFileFilter("java"))) {

				if (sourceFile.toString().contains("ParseController") || sourceFile.toString().contains("Validator"))
					continue;


				String content = FileCommands.readFileAsString(sourceFile.toFile());
				if (!content.contains("org.eclipse")) {
					sourceFiles.add(sourceFile.toFile());
					require(sourceFile.toFile());
				}
			}
		}

		List<File> classPath = new ArrayList<>();
		classPath.add(context.basePath("utils/strategoxt.jar"));
		classPath.add(context.basePath("${src-gen}"));
		if (context.props.isDefined("externaljar"))
			classPath.add(new File(context.props.get("externaljar")));
		if (context.props.isDefined("externaljarx"))
			classPath.add(new File(context.props.get("externaljarx")));
		if (context.isJavaJarEnabled(this))
			classPath.add(context.basePath("${include}/${strmodule}-java.jar"));

		for (File sourceFile : sourceFiles) {
			requireBuild(JavaBuilder.request(new JavaInput(sourceFile, targetDir, sourcePath, classPath, additionalArgs.toArray(new String[additionalArgs
					.size()]), null)));
		}

		return None.val;
	}
}
