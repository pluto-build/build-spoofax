package build.pluto.buildspoofax.builders;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.lang.Context;
import org.sugarj.common.FileCommands;
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
		
		require(FileCommands.getRessourcePath(sdf2imp.class), LastModifiedStamper.instance);
		ClassLoader loader = sdf2imp.class.getClassLoader();
		
		for (String p : new String[]{"make_permissive.jar", "sdf2imp.jar", "aster.jar", "StrategoMix.def"}) {
			InputStream from = loader.getResourceAsStream("dist/" + p);
			Path to = new RelativePath(utils, p);
			FileCommands.createFile(to);
			IOUtils.copy(from, new FileOutputStream(to.getFile()));
			provide(to, LastModifiedStamper.instance);
		}

		require(FileCommands.getRessourcePath(Context.class), LastModifiedStamper.instance);
		InputStream strategoJarStream = Context.class.getClassLoader().getResourceAsStream("java/strategoxt.jar");
		Path strategojarTo = new RelativePath(utils, "strategoxt.jar");
		FileCommands.createFile(strategojarTo);
		IOUtils.copy(strategoJarStream, new FileOutputStream(strategojarTo.getFile()));
		provide(strategojarTo, LastModifiedStamper.instance);
		
		return None.val;
	}
}
