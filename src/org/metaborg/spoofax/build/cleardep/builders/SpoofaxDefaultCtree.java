package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.strategoxt.imp.metatooling.building.AntForceRefreshScheduler;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.Log;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class SpoofaxDefaultCtree extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, SpoofaxDefaultCtree> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, SpoofaxDefaultCtree>() {
		@Override
		public SpoofaxDefaultCtree makeBuilder(SpoofaxBuildContext context) { return new SpoofaxDefaultCtree(context); }
	};
	
	public SpoofaxDefaultCtree(SpoofaxBuildContext context) {
		super(context);
	}
	
	@Override
	protected String taskDescription(Void input) {
		return null;
	}

	@Override
	protected Path persistentPath(Void input) {
		return context.depPath("spoofaxDefault.dep");
	}
	
	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Void input) throws IOException {
		checkClassPath();
		
		CompilationUnit forceOnSave = context.forceOnSave.require(null, new SimpleMode());
		result.addModuleDependency(forceOnSave);
		
		forceWorkspaceRefresh();
		
		String sdfmodule = context.props.get("sdfmodule");
		String strmodule = context.props.get("strmodule");
		String esvmodule = context.props.get("esvmodule");
		String metasdfmodule = context.props.get("metasdfmodule");
		String buildSdfImports = context.props.get("build.sdf.imports");
		Path externaldef = context.props.isDefined("externaldef") ? new AbsolutePath(context.props.get("externaldef")) : null;
		Path externaljar = context.props.isDefined("externaljar") ? new AbsolutePath(context.props.get("externaljar")) : null;
		
		CompilationUnit sdf2Table = context.sdf2Table.require(new Sdf2Table.Input(sdfmodule, buildSdfImports, externaldef), new SimpleMode());
		result.addModuleDependency(sdf2Table);
		
		CompilationUnit metaSdf2Table = context.metaSdf2Table.require(new MetaSdf2Table.Input(metasdfmodule, buildSdfImports, externaldef), new SimpleMode());
		result.addModuleDependency(metaSdf2Table);
		
		CompilationUnit ppGen = context.ppGen.require(null, new SimpleMode());
		result.addModuleDependency(ppGen);
		
		RelativePath ppPackInputPath = context.basePath("${syntax}/${sdfmodule}.pp");
		RelativePath ppPackOutputPath = context.basePath("${include}/${sdfmodule}.pp.af");
		CompilationUnit ppPack = context.ppPack.require(new PPPack.Input(ppPackInputPath, ppPackOutputPath, true), new SimpleMode());
		result.addModuleDependency(ppPack);
		
		CompilationUnit sdf2Imp = context.sdf2ImpEclipse.require(new Sdf2ImpEclipse.Input(esvmodule, sdfmodule, buildSdfImports), new SimpleMode());
		result.addModuleDependency(sdf2Imp);
		
		CompilationUnit sdf2Parenthesize = context.sdf2Parenthesize.require(new Sdf2Parenthesize.Input(sdfmodule, buildSdfImports, externaldef), new SimpleMode());
		result.addModuleDependency(sdf2Parenthesize);
		
		CompilationUnit strategoAster = context.strategoAster.require(new StrategoAster.Input(strmodule), new SimpleMode());
		result.addModuleDependency(strategoAster);

		CompilationUnit strategoCtree = context.strategoCtree.require(new StrategoCtree.Input(sdfmodule, buildSdfImports, strmodule, externaljar), new SimpleMode());
		result.addModuleDependency(strategoCtree);
	}

	private void checkClassPath() {
		@SuppressWarnings("unused")
		org.strategoxt.imp.generator.sdf2imp c;
	}

	protected void forceWorkspaceRefresh() {
		try {
			AntForceRefreshScheduler.main(new String[] {context.basePath("${include}").getAbsolutePath()});
		} catch (Exception e) {
			Log.log.logErr(e.getMessage(), Log.CORE);
		}
	}
}
