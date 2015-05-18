package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.buildjava.JavaJar;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.output.Out;

public class SpoofaxDefaultCtree extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, SpoofaxDefaultCtree> factory = SpoofaxBuilderFactory.of(SpoofaxDefaultCtree.class,
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
		String sdfmodule = context.props.getOrFail("sdfmodule");
		String strmodule = context.props.getOrFail("strmodule");
		String esvmodule = context.props.getOrFail("esvmodule");
		String metasdfmodule = context.props.getOrFail("metasdfmodule");
		String buildSdfImports = context.props.getOrElse("build.sdf.imports", "");
		File externaldef = context.props.isDefined("externaldef") ? new File(context.props.get("externaldef")) : null;
		File externaljar = context.props.isDefined("externaljar") ? new File(context.props.get("externaljar")) : null;
		String externaljarflags = context.props.getOrElse("externaljarflags", "");

		checkClassPath();
		
		requireBuild(Sdf2Table.factory, new Sdf2Table.Input(context, sdfmodule, buildSdfImports, externaldef));
		requireBuild(MetaSdf2Table.factory, new MetaSdf2Table.Input(context, metasdfmodule, buildSdfImports, externaldef));
		requireBuild(PPGen.factory, input);
		
		File ppPackInputPath = context.basePath("${syntax}/${sdfmodule}.pp");
		File ppPackOutputPath = context.basePath("${include}/${sdfmodule}.pp.af");
		requireBuild(PPPack.factory, new PPPack.Input(context, ppPackInputPath, ppPackOutputPath, true));
		
		requireBuild(StrategoAster.factory, new StrategoAster.Input(context, strmodule));

		// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/lib/editor-common.generated.str'.
		BuildRequest<Sdf2Imp.Input,None,Sdf2Imp,?> sdf2imp = new BuildRequest<>(Sdf2Imp.factory, new Sdf2Imp.Input(context, esvmodule, sdfmodule, buildSdfImports));
		// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/include/TemplateLang-parenthesize.str'.
		BuildRequest<Sdf2Parenthesize.Input,None,Sdf2Parenthesize,?> sdf2Parenthesize = new BuildRequest<>(Sdf2Parenthesize.factory, new Sdf2Parenthesize.Input(context, sdfmodule, buildSdfImports, externaldef));

		Out<File> ctree = requireBuild(StrategoCtree.factory,
				new StrategoCtree.Input(
						context,
						sdfmodule, 
						buildSdfImports, 
						strmodule, 
						externaljar, 
						externaljarflags, 
						externaldef,
						new BuildRequest<?,?,?,?>[] {sdf2imp, sdf2Parenthesize}));
		
		// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/editor/java/org/strategoxt/imp/editors/template/strategies/InteropRegisterer.class'.
		BuildRequest<SpoofaxInput,None,CompileJavaCode,?> compileJavaCode = new BuildRequest<>(CompileJavaCode.factory, input);
		requireBuild(compileJavaCode);
		
		javaJar(strmodule, compileJavaCode);
		
		return None.val;
	}

	
	private void checkClassPath() {
		@SuppressWarnings("unused")
		org.strategoxt.imp.generator.sdf2imp c;
	}

	private void javaJar(String strmodule, BuildRequest<?,?,?,?> compileJavaCode) throws IOException {
		if (!context.isJavaJarEnabled(this))
			return;
		
		File baseDir = context.basePath("${build}");
		String[] sfiles = context.props.getOrElse("javajar-includes", "org/strategoxt/imp/editors/template/strategies/").split("[\\s]+");
		Map<File, Set<File>> files = new HashMap<>();
		Set<File> relativeFiles = new HashSet<>();
		Set<File> absoluteFiles = new HashSet<>();
		for (int i = 0; i < sfiles.length; i++) {
			if (FileCommands.acceptableAsAbsolute(sfiles[i]))
				absoluteFiles.add(new File(sfiles[i]));
			else
				relativeFiles.add(new File(baseDir, sfiles[i]));
		}
		files.put(baseDir, relativeFiles);
		files.put(new File(""), absoluteFiles);
		
		File jarPath = context.basePath("${include}/" + strmodule + "-java.jar");
		requireBuild(JavaJar.factory, 
				new JavaJar.Input(
						JavaJar.Mode.CreateOrUpdate,
						jarPath,
						null,
						files, 
						new BuildRequest<?,?,?,?>[] {compileJavaCode}));
	}
}

