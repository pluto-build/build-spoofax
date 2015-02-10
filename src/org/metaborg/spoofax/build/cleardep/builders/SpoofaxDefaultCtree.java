package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.Log;

public class SpoofaxDefaultCtree extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, SpoofaxDefaultCtree> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, SpoofaxDefaultCtree>() {
		@Override
		public SpoofaxDefaultCtree makeBuilder(SpoofaxBuildContext context) { return new SpoofaxDefaultCtree(context); }
	};
	
	public SpoofaxDefaultCtree(SpoofaxBuildContext context) {
		super(context);
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
		
		CompilationUnit forceOnSave = context.forceOnSave.require(null, context.basePath("${include}/build.forceOnSave.dep"), new SimpleMode());
		result.addModuleDependency(forceOnSave);
	}
	
	private void checkClassPath() {
		try {
			Class<?> cl = this.getClass().getClassLoader().loadClass("org.strategoxt.imp.generator.sdf2imp");
			Log.log.log("Found class " + cl, Log.DETAIL);
		} catch (ClassNotFoundException e) {
			String msg = "Could not load the Spoofax plugin loading class `org.strategoxt.imp.generator.sdf2imp`.\n" +
			             "Make sure it is on the class path.\n";
			Log.log.logErr(msg, Log.CORE);
			throw new RuntimeException(e);
		}
	}

}
