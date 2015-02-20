package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.StandardCopyOption;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;

public class CopySdf extends Builder<SpoofaxBuildContext, CopySdf.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, CopySdf> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, CopySdf>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7896468066897221237L;

		@Override
		public CopySdf makeBuilder(SpoofaxBuildContext context) { return new CopySdf(context); }
	};
	
	public static class Input implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7470907587832108136L;
		public final String sdfmodule;
		public final Path externaldef;
		public Input(String sdfmodule, Path externaldef) {
			this.sdfmodule = sdfmodule;
			this.externaldef = externaldef;
		}
	}
	
	private CopySdf(SpoofaxBuildContext context) {
		super(context, factory);
	}

	@Override
	protected String taskDescription(Input input) {
		return "Copy external grammar definition.";
	}
	
	@Override
	public Path persistentPath(Input input) {
		if (input.externaldef != null)
			return context.depPath("copySdf." + input.externaldef + "." + input.sdfmodule + ".dep");
		return context.depPath("copySdf." + input.sdfmodule + ".dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		if (input.externaldef != null) {
			Path target = context.basePath("${include}/" + input.sdfmodule + ".def");
			result.addExternalFileDependency(input.externaldef);
			FileCommands.copyFile(input.externaldef, target, StandardCopyOption.COPY_ATTRIBUTES);
			result.addGeneratedFile(target);
		}
	}
}
