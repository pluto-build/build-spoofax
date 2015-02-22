package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;

import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.build.EmptyBuildInput;

import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;


public class All extends SpoofaxBuilder<SpoofaxInput> {

	public static SpoofaxBuilderFactory<SpoofaxInput, All> factory = new SpoofaxBuilderFactory<SpoofaxInput, All>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1747202833519981639L;

		@Override
		public All makeBuilder(SpoofaxInput input, BuildManager manager) { return new All(input, manager); }
	};
	

	public All(SpoofaxInput input, BuildManager manager) {
		super(input, factory, manager);
	}

	@Override
	protected String taskDescription() {
		return "Build Spoofax project";
	}
	
	protected Path persistentPath() {
		return context.depPath("all.dep");
	}
	
	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result) throws IOException {
		RelativePath ppInput = context.basePath("${lib}/EditorService-pretty.pp");
		RelativePath ppTermOutput = context.basePath("${include}/EditorService-pretty.pp.af");
		require(PPPack.factory, new PPPack.Input(context, ppInput, ppTermOutput), new SimpleMode());
		
		require(SpoofaxDefaultCtree.factory, input, new SimpleMode());
	}

}
