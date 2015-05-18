package build.pluto.buildspoofax.builders;

import java.io.IOException;

import org.sugarj.common.FileCommands;
import org.sugarj.common.path.AbsolutePath;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.stamp.LastModifiedStamper;

public class CopyUtils extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, CopyUtils> factory = new SpoofaxBuilderFactory<SpoofaxInput, None, CopyUtils>() {
		private static final long serialVersionUID = 2088788942202940759L;

		@Override
		public CopyUtils makeBuilder(SpoofaxInput input) { return new CopyUtils(input); }
	};
	
	public CopyUtils(SpoofaxInput input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Copy utilities";
	}
	
	@Override
	public Path persistentPath() {
		return context.depPath("copyUtils.dep");
	}

	@Override
	public None build() throws IOException {
		Path utils = context.basePath("utils");
		FileCommands.createDir(utils);
		
		String base = context.props.getOrFail("eclipse.spoofaximp.jars");
		for (String p : new String[]{"make_permissive.jar", "sdf2imp.jar", "aster.jar", "StrategoMix.def"}) {
			Path from = new AbsolutePath(base + "/" + p);
			Path to = new RelativePath(utils, p);
			require(from, LastModifiedStamper.instance);
			FileCommands.copyFile(from, to);
			provide(to);
		}
		
		Path strategojar = new AbsolutePath(context.props.getOrFail("eclipse.spoofaximp.strategojar"));
		Path strategojarTo = new RelativePath(utils, FileCommands.dropDirectory(strategojar));
		require(strategojar, LastModifiedStamper.instance);
		FileCommands.copyFile(strategojar, strategojarTo);
		provide(strategojarTo);
		
		return None.val;
	}
}
