package build.pluto.buildspoofax.builders.aux;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.Out;
import build.pluto.output.OutputTransient;
import build.pluto.stamp.LastModifiedStamper;

import com.google.inject.Injector;

public class DiscoverSpoofaxLanguage extends SpoofaxBuilder<DiscoverSpoofaxLanguage.Input, Out<ILanguage>> {

	public static class DiscoverSpoofaxLanguageRequest extends BuildRequest<Input, Out<ILanguage>, DiscoverSpoofaxLanguage, SpoofaxBuilderFactory<Input, Out<ILanguage>, DiscoverSpoofaxLanguage>> {
		public DiscoverSpoofaxLanguageRequest(SpoofaxBuilderFactory<Input, Out<ILanguage>, DiscoverSpoofaxLanguage> factory, Input input) {
			super(factory, input);
		}

		private static final long serialVersionUID = -8195304087460359223L;
	}
	
	public static SpoofaxBuilderFactory<Input, Out<ILanguage>, DiscoverSpoofaxLanguage> factory = SpoofaxBuilderFactory.of(
			DiscoverSpoofaxLanguage.class, Input.class);

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 12331766781256062L;

		public final String langName;

		public Input(SpoofaxContext context, String langName) {
			super(context);
			this.langName = langName;
		}
	}

	public DiscoverSpoofaxLanguage(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Discover Spoofax language " + input.langName;
	}

	@Override
	public File persistentPath(Input input) {
		return context.depPath("discover." + input.langName + ".dep");
	}

	@Override
	public Out<ILanguage> build(Input input) throws Exception {
		Injector injector = context.guiceInjector();
		ILanguageDiscoveryService discoverySerivce = injector.getInstance(ILanguageDiscoveryService.class);

		String esv = "/include/" + input.langName + ".packed.esv";
		URL url = DiscoverSpoofaxLanguage.class.getResource(esv);
		File jar = FileCommands.getRessourcePath(url).toFile();
		File dir;
		if (jar.isDirectory())
			dir = jar;
		else {
			dir = context.depPath("discover." + FileCommands.fileName(jar.getName()));
			requireBuild(UnpackJarFile.factory, new UnpackJarFile.Input(context, jar, dir));
		}

		for (Path p : FileCommands.listFilesRecursive(dir.toPath()))
			require(p.toFile(), LastModifiedStamper.instance);

		FileObject fo = VFS.getManager().resolveFile(dir.getAbsolutePath());
		Iterable<ILanguage> langs = discoverySerivce.discover(fo);
		if (langs == null || !langs.iterator().hasNext())
			throw new IllegalStateException("Failed to discover language " + input.langName);
		Iterator<ILanguage> it = langs.iterator();
		ILanguage lang = it.next();
		if (it.hasNext())
			throw new IllegalStateException("Discovered multiple languages named " + input.langName);

		return OutputTransient.of(lang);
	}
}
