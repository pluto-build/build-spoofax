package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.metaborg.spoofax.core.project.settings.Format;
import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.util.file.FileUtils;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.buildjava.JavaJar;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxBuilderFactoryFactory;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;

import com.google.common.base.Joiner;

public class SpoofaxDefaultCtree extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, SpoofaxDefaultCtree> factory = SpoofaxBuilderFactoryFactory.of(SpoofaxDefaultCtree.class,
			SpoofaxInput.class);
	

	public SpoofaxDefaultCtree(SpoofaxInput input) {
		super(input);
	}
	
	@Override
	protected String description(SpoofaxInput input) {
		return "Build Spoofax project";
	}

	@Override
	protected File persistentPath(SpoofaxInput input) {
		return context.depPath("spoofaxDefault.dep");
	}
	
	@Override
	public None build(SpoofaxInput input) throws IOException {
	    requireBuild(CompileSpoofaxPrograms.factory, new CompileSpoofaxPrograms.Input(context));
	    
	    final SpoofaxProjectSettings settings = context.settings;
	    
		String sdfModule = settings.sdfName();
		String strModule = settings.strategoName();
		String metaSdfModule = settings.metaSdfName();
		String sdfArgs = Joiner.on(' ').join(settings.sdfArgs());
		File externalDef = settings.externalDef() != null ? new File(settings.externalDef()) : null;
		File externalJar = settings.externalJar() != null ? new File(settings.externalJar()) : null;
		String externalJarFlags = settings.externalJarFlags();
		
		requireBuild(Sdf2Table.factory, new Sdf2Table.Input(context, sdfModule, sdfArgs));
		requireBuild(MetaSdf2Table.factory, new MetaSdf2Table.Input(context, metaSdfModule));
		requireBuild(PPGen.factory, new PPGen.Input(context, sdfModule));
		
		File ppPackInputPath = FileUtils.toFile(settings.getPpFile(sdfModule));
		File ppPackOutputPath = FileUtils.toFile(settings.getPpAfCompiledFile(sdfModule));
		requireBuild(PPPack.factory, new PPPack.Input(context, ppPackInputPath, ppPackOutputPath, true));
		
		// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/include/TemplateLang-parenthesize.str'.
		BuildRequest<Sdf2Parenthesize.Input,None,Sdf2Parenthesize,?> sdf2Parenthesize = new BuildRequest<>(Sdf2Parenthesize.factory, new Sdf2Parenthesize.Input(context, sdfModule));

		requireBuild(StrategoCtree.factory,
				new StrategoCtree.Input(
						context,
						sdfModule, 
						sdfArgs, 
						strModule, 
						externalJar, 
						externalJarFlags, 
						externalDef,
						new BuildRequest<?,?,?,?>[] {sdf2Parenthesize}));
		
		// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/editor/java/org/strategoxt/imp/editors/template/strategies/InteropRegisterer.class'.
		BuildRequest<SpoofaxInput,None,CompileJavaCode,?> compileJavaCode = new BuildRequest<>(CompileJavaCode.factory, input);
		requireBuild(compileJavaCode);
		
        if(context.isJavaJarEnabled(this)) {
            final File buildDir = FileUtils.toFile(context.settings.getClassesDirectory());
            // TODO: get javajar-includes from project settings?
            // String[] paths = context.props.getOrElse("javajar-includes",
            // context.settings.packageStrategiesPath()).split("[\\s]+");
            final String[] paths = new String[] { context.settings.packageStrategiesPath() };
            final File output = FileUtils.toFile(context.settings.getStrCompiledJavaJarFile());
            jar(buildDir, paths, output, new BuildRequest<?, ?, ?, ?>[] { compileJavaCode });
        }
        
        if(context.settings.format() == Format.jar) {
            final File buildDir = FileUtils.toFile(context.settings.getClassesDirectory());
            final String[] paths = new String[] { context.settings.getStrJavaTransDirectory().getName().getPath() };
            final File output = FileUtils.toFile(context.settings.getStrCompiledJarFile());
            jar(buildDir, paths, output, new BuildRequest<?, ?, ?, ?>[] { compileJavaCode });
        }
		
		return None.val;
	}

	private void jar(File buildDir, String[] paths, File output, BuildRequest<?,?,?,?>[] requirements) throws IOException {
		Map<File, Set<File>> files = new HashMap<>();
		Set<File> relativeFiles = new HashSet<>();
		Set<File> absoluteFiles = new HashSet<>();
		for (int i = 0; i < paths.length; i++) {
			if (FileCommands.acceptableAsAbsolute(paths[i]))
				absoluteFiles.add(new File(paths[i]));
			else
				relativeFiles.add(new File(buildDir, paths[i]));
		}
		files.put(buildDir, relativeFiles);
		files.put(new File(""), absoluteFiles);
		
        requireBuild(JavaJar.factory, new JavaJar.Input(JavaJar.Mode.CreateOrUpdate, output, null, files, requirements));
	}
}

