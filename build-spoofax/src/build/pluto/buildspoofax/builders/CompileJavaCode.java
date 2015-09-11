package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.metaborg.util.file.FileUtils;
import org.strategoxt.strj.strj;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.buildjava.JavaBulkBuilder;
import build.pluto.buildjava.JavaInput;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxBuilderFactoryFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;

import com.google.common.collect.Lists;

public class CompileJavaCode extends SpoofaxBuilder<CompileJavaCode.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, CompileJavaCode> factory = SpoofaxBuilderFactoryFactory.of(CompileJavaCode.class, Input.class);

	public static class Input extends SpoofaxInput {
	    private static final long serialVersionUID = 2844209784723078635L;
	    
	    public final BuildRequest<?, ?, ?, ?>[] dependencies;
	    
        public Input(SpoofaxContext context, BuildRequest<?, ?, ?, ?>[] dependencies) {
            super(context);
            
            this.dependencies = dependencies;
        }        
	}
	
	public CompileJavaCode(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Compile Java code for Spoofax";
	}

	@Override
	public File persistentPath(Input input) {
		return context.depPath("compileJavaCode.dep");
	}

	@Override
	public None build(Input input) throws IOException {
		File targetDir = FileUtils.toFile(context.settings.getClassesDirectory());
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

		// TODO: this used to get src-dirs property before
		String srcDirs = FileUtils.toPath(context.settings.getStrJavaDirectory());
		List<File> sourcePath = new ArrayList<>();
		List<File> sourceFiles = new ArrayList<>();
		for (String dir : srcDirs.split("[\\s]+")) {
			File p;
			if (FileCommands.acceptableAsAbsolute(dir))
				p = new File(dir);
			else
				p = context.basePath(dir);

			sourcePath.add(p);

			for (Path sourceFile : FileCommands.listFilesRecursive(p.toPath(), new SuffixFileFilter("java"))) {
				sourceFiles.add(sourceFile.toFile());
				require(sourceFile.toFile());
			}
		}

		List<File> classPath = new ArrayList<>();
		for(String classpathItem : context.javaClasspath) {
		    classPath.add(new File(classpathItem));
		}
		classPath.add(FileCommands.getRessourcePath(strj.class).toFile());

		if (context.settings.externalJar() != null) {
		    classPath.add(new File(context.settings.externalJar()));
		}
		if (context.isJavaJarEnabled(this)) {
			classPath.add(FileUtils.toFile(context.settings.getStrCompiledJavaJarFile()));
		}
		
        requireBuild(JavaBulkBuilder.factory, new JavaInput(sourceFiles, targetDir, sourcePath, classPath,
            additionalArgs.toArray(new String[additionalArgs.size()]), Lists.newArrayList(input.dependencies)));

		return None.val;
	}
}
