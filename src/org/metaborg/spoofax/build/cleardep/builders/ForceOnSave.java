package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.List;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.strategoxt.imp.metatooling.building.AntForceOnSave;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
import org.sugarj.common.StringCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class ForceOnSave extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public ForceOnSave(SpoofaxBuildContext context) {
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
		Log.log.beginInlineTask("Force on-save handlers for NaBL, TS, etc.", Log.CORE); 
		
		// XXX really need to delete old sdf3 files? Or is it sufficient to remove them from `paths` below?
		List<RelativePath> oldSdf3Paths = FileCommands.listFilesRecursive(context.basePath("src-gen"), new FileExtensionFilter("sdf3"));
		for (Path p : oldSdf3Paths)
			FileCommands.delete(p);
		
		List<RelativePath> paths = FileCommands.listFilesRecursive(
				new AbsolutePath("."), 
				new FileExtensionFilter("tmpl", "sdf3", "nab", "ts"));
		String pathString = StringCommands.printListSeparated(paths, ";;;");
		
		for (RelativePath p : paths)
			result.addSourceArtifact(p);
		
		AntForceOnSave.main(new String[]{pathString});
		
		Log.log.endTask();
	}

}
