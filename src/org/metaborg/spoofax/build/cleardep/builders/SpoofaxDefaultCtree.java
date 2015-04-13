package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.strategoxt.imp.metatooling.building.AntForceRefreshScheduler;
import org.strategoxt.imp.metatooling.loading.AntDescriptorLoader;
import org.sugarj.common.Log;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.builder.BuildRequest;
import build.pluto.buildjava.JavaJar;
import build.pluto.output.None;

public class SpoofaxDefaultCtree extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, SpoofaxDefaultCtree> factory = new SpoofaxBuilderFactory<SpoofaxInput, None, SpoofaxDefaultCtree>() {
		private static final long serialVersionUID = -6945708860855449389L;

		@Override
		public SpoofaxDefaultCtree makeBuilder(SpoofaxInput input) { return new SpoofaxDefaultCtree(input); }
	};
	

	public SpoofaxDefaultCtree(SpoofaxInput input) {
		super(input);
	}
	
	@Override
	protected String description() {
		return "Build Spoofax project";
	}

	@Override
	protected Path persistentPath() {
		return context.depPath("spoofaxDefault.dep");
	}
	
	@Override
	public None build() throws IOException {
		String sdfmodule = context.props.getOrFail("sdfmodule");
		String strmodule = context.props.getOrFail("strmodule");
		String esvmodule = context.props.getOrFail("esvmodule");
		String metasdfmodule = context.props.getOrFail("metasdfmodule");
		String buildSdfImports = context.props.getOrElse("build.sdf.imports", "");
		Path externaldef = context.props.isDefined("externaldef") ? new AbsolutePath(context.props.get("externaldef")) : null;
		Path externaljar = context.props.isDefined("externaljar") ? new AbsolutePath(context.props.get("externaljar")) : null;
		String externaljarflags = context.props.getOrElse("externaljarflags", "");

		try {
			checkClassPath();
			
			Path parseTable = requireBuild(Sdf2Table.factory, new Sdf2Table.Input(context, sdfmodule, buildSdfImports, externaldef));
			requireBuild(MetaSdf2Table.factory, new MetaSdf2Table.Input(context, metasdfmodule, buildSdfImports, externaldef));
			requireBuild(PPGen.factory, input);
			
			RelativePath ppPackInputPath = context.basePath("${syntax}/${sdfmodule}.pp");
			RelativePath ppPackOutputPath = context.basePath("${include}/${sdfmodule}.pp.af");
			requireBuild(PPPack.factory, new PPPack.Input(context, ppPackInputPath, ppPackOutputPath, true));
			
			requireBuild(StrategoAster.factory, new StrategoAster.Input(context, strmodule));
	
			// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/lib/editor-common.generated.str'.

			BuildRequest<?,?,?,?> sdf2Imp = new BuildRequest<>(Sdf2ImpEclipse.factory, new Sdf2ImpEclipse.Input(context, esvmodule, sdfmodule, buildSdfImports));
			// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/include/TemplateLang-parenthesize.str'.
			BuildRequest<?,?,?,?> sdf2Parenthesize = new BuildRequest<>(Sdf2Parenthesize.factory, new Sdf2Parenthesize.Input(context, sdfmodule, buildSdfImports, externaldef));
	
			Path ctree = requireBuild(StrategoCtree.factory,
					new StrategoCtree.Input(
							context,
							sdfmodule, 
							buildSdfImports, 
							strmodule, 
							externaljar, 
							externaljarflags, 
							externaldef,
							new BuildRequest<?,?,?,?>[] {sdf2Imp, sdf2Parenthesize}));
			
			// This dependency was discovered by cleardep, due to an implicit dependency on 'org.strategoxt.imp.editors.template/editor/java/org/strategoxt/imp/editors/template/strategies/InteropRegisterer.class'.
			BuildRequest<?,?,?,?> compileJavaCode = new BuildRequest<>(CompileJavaCode.factory, input);
			requireBuild(compileJavaCode);
			
			javaJar(strmodule, compileJavaCode);
			
			sdf2impEclipseReload(parseTable, ctree);
			
			return None.val;
			
		} finally {
			forceWorkspaceRefresh();
		}
	}

	
	private void checkClassPath() {
		@SuppressWarnings("unused")
		org.strategoxt.imp.generator.sdf2imp c;
	}

	private void javaJar(String strmodule, BuildRequest<?,?,?,?> compileJavaCode) throws IOException {
		if (!context.isJavaJarEnabled(this))
			return;
		
		Path baseDir = context.basePath("${build}");
		String[] sfiles = context.props.getOrElse("javajar-includes", "org/strategoxt/imp/editors/template/strategies/").split("[\\s]+");
		Path[] files = new Path[sfiles.length];
		for (int i = 0; i < sfiles.length; i++)
			if (AbsolutePath.acceptable(sfiles[i]))
				files[i] = new AbsolutePath(sfiles[i]);
			else
				files[i] = new RelativePath(baseDir, sfiles[i]);
		
		Path jarPath = context.basePath("${include}/" + strmodule + "-java.jar");
		requireBuild(JavaJar.factory, 
				new JavaJar.Input(
						JavaJar.Mode.CreateOrUpdate,
						jarPath,
						null,
						files, 
						new BuildRequest<?,?,?,?>[] {compileJavaCode}));
	}

	private void sdf2impEclipseReload(Path parseTable, Path ctree) {
		RelativePath packedEsv = context.basePath("${include}/${esvmodule}.packed.esv");
		require(packedEsv);
		require(parseTable);
		require(ctree);
		AntDescriptorLoader.main(new String[]{packedEsv.getAbsolutePath()});
		Log.log.log("Reloaded Spoofax plug-in", Log.CORE);
	}

	protected void forceWorkspaceRefresh() {
		try {
			AntForceRefreshScheduler.main(new String[] {context.baseDir.getAbsolutePath()});
		} catch (Exception e) {
			Log.log.logErr(e.getMessage(), Log.CORE);
		}
	}
}

