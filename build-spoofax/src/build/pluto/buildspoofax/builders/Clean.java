package build.pluto.buildspoofax.builders;

import static org.sugarj.common.StreamCommands.forEachTry;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.util.PatternFileFilter;
import build.pluto.output.None;

public class Clean extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, Clean> factory = new SpoofaxBuilderFactory<SpoofaxInput, None, Clean>() {
		private static final long serialVersionUID = -1133955108882900676L;

		@Override
		public Clean makeBuilder(SpoofaxInput input) {
			return new Clean(input);
		}
	};

	public Clean(SpoofaxInput input) {
		super(input);
	}

	@Override
	protected String description(SpoofaxInput input) {
		return "Clean";
	}

	@Override
	protected File persistentPath(SpoofaxInput input) {
		return context.depPath("clean.dep");
	}

	@Override
	public None build(SpoofaxInput input) throws IOException {
		String[] paths = { ".cache", "${include}/${sdfmodule}.def", "${include}/${sdfmodule}-parenthesize.str", "${include}/${sdfmodule}-Permissive.def",
				"${include}/${sdfmodule}.generated.pp", "${include}/${sdfmodule}.generated.pp.af", "${include}/${sdfmodule}.packed.esv",
				"${include}/${sdfmodule}.pp.af", "${include}/${sdfmodule}.rtg", "${lib-gen}/${ppmodule}.jar",
				"${lib-gen}/${ppmodule}.rtree",
				"${lib-gen}/${sigmodule}.str",
				// "${lib-gen}/${sigmodule}.ctree",
				"${lib-gen}/${sigmodule}.rtree",
				"${lib-gen}/${sigmodule}.ctree.dep",
				"${include}/${sdfmodule}.str",
				// "${include}/${sdfmodule}.tbl",
				"${include}/${strmodule}.rtree",
				// "${include}/${strmodule}.ctree",
				"${include}/${strmodule}.ctree.dep", "${include}/${strmodule}.jar", "${src-gen}/trans", "${src-gen}/templatelang/pplib",
				"${src-gen}/templatelang/siglib", "${syntax}/${sdfmodule}.generated.esv", "${syntax}/${sdfmodule}.generated.pp",
				"${include}/${metasdfmodule}-Permissive.def", "${include}/${metasdfmodule}.def", "${include}/${metasdfmodule}.tbl", "utils" };

		for (String p : paths) {
			File path = context.basePath(p);
			Log.log.log("Delete " + path, Log.DETAIL);
			FileCommands.delete(path);
			provide(path);
		}

		forEachTry(FileCommands.streamFiles(context.basePath("${build}")), (File p) -> {
			Log.log.log("Delete " + p, Log.DETAIL);
			FileCommands.delete(p);
			provide(p);
		});

		forEachTry(FileCommands.streamFiles(context.basePath("${lib}"), new PatternFileFilter(".*\\.generated\\.str")), (File p) -> {
			Log.log.log("Delete " + p, Log.DETAIL);
			FileCommands.delete(p);
			provide(p);
		});

		File cleanPath = persistentPath(input);
		forEachTry(FileCommands.streamFiles(context.depDir(), new SuffixFileFilter("dep")), (File p) -> {
			if (!p.equals(cleanPath)) {
				Log.log.log("Delete " + p, Log.DETAIL);
				FileCommands.delete(p);
				provide(p);
			}
		});

		return None.val;
	}

	

}
