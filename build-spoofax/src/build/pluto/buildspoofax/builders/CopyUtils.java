package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.strategoxt.imp.generator.sdf2imp;
import org.strategoxt.lang.Context;
import org.sugarj.common.FileCommands;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.stamp.LastModifiedStamper;

public class CopyUtils extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, CopyUtils> factory = SpoofaxBuilderFactory.of(CopyUtils.class, SpoofaxInput.class);
	
	public CopyUtils(SpoofaxInput input) {
		super(input);
	}

	@Override
	protected String description(SpoofaxInput input) {
		return "Copy utilities";
	}
	
	@Override
	public File persistentPath(SpoofaxInput input) {
		return context.depPath("copyUtils.dep");
	}

	@Override
	public None build(SpoofaxInput input) throws IOException {
		File utils = context.basePath("utils");
		FileCommands.createDir(utils.toPath());
		
		require(FileCommands.getRessourcePath(sdf2imp.class).toFile(), LastModifiedStamper.instance);
		ClassLoader loader = sdf2imp.class.getClassLoader();
		
		for (String p : new String[]{"make_permissive.jar", "sdf2imp.jar", "aster.jar", "StrategoMix.def"}) {
			InputStream from = loader.getResourceAsStream("dist/" + p);
			File to = new File(utils, p);
			FileCommands.createFile(to);
			IOUtils.copy(from, new FileOutputStream(to));
			provide(to, LastModifiedStamper.instance);
		}

		require(FileCommands.getRessourcePath(Context.class).toFile(), LastModifiedStamper.instance);
		InputStream strategoJarStream = Context.class.getClassLoader().getResourceAsStream("java/strategoxt.jar");
		File strategojarTo = new File(utils, "strategoxt.jar");
		FileCommands.createFile(strategojarTo);
		IOUtils.copy(strategoJarStream, new FileOutputStream(strategojarTo));
		provide(strategojarTo, LastModifiedStamper.instance);
		
		return None.val;
	}
}
