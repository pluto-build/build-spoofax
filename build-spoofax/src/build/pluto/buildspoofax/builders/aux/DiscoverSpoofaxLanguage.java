package build.pluto.buildspoofax.builders.aux;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.sugarj.common.FileCommands;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.buildspoofax.util.KryoWrapper;
import build.pluto.output.Out;
import build.pluto.stamp.LastModifiedStamper;

import com.google.inject.Injector;

public class DiscoverSpoofaxLanguage extends SpoofaxBuilder<DiscoverSpoofaxLanguage.Input, Out<KryoWrapper<ILanguage>>> {

	public static SpoofaxBuilderFactory<Input, Out<KryoWrapper<ILanguage>>, DiscoverSpoofaxLanguage> factory = SpoofaxBuilderFactory.of(
			DiscoverSpoofaxLanguage.class,
			Input.class);

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 12331766781256062L;

		public final Class<?> someClassFromLanguage;
		public Input(SpoofaxContext context, Class<?> someClassFromLanguage) {
			super(context);
			this.someClassFromLanguage = someClassFromLanguage;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Input) {
				return super.context.equals(((Input) obj).context) && someClassFromLanguage.equals(((Input) obj).someClassFromLanguage);
			}
			return false;
		}
	}
	
	public DiscoverSpoofaxLanguage(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Discover Spoofax language for " + input.someClassFromLanguage.getName();
	}
	
	@Override
	public File persistentPath(Input input) {
		return context.depPath("discover." + input.someClassFromLanguage.getName() + ".dep");
	}

	@Override
	public Out<KryoWrapper<ILanguage>> build(Input input) throws Exception {
		Injector injector = context.guiceInjector();
		IResourceService resourceSerivce = context.getResourceService();
		ILanguageDiscoveryService discoverySerivce = injector.getInstance(ILanguageDiscoveryService.class);
		
		File jar = FileCommands.getRessourcePath(input.someClassFromLanguage).toFile();
		File dir;
		if (jar.isDirectory())
			dir = jar;
		else {
			dir = context.depPath("discover." + FileCommands.fileName(jar.getName()));
			requireBuild(UnpackJarFile.factory, new UnpackJarFile.Input(context, jar, dir));
		}
		
		for (Path p : FileCommands.listFilesRecursive(dir.toPath()))
			require(p.toFile(), LastModifiedStamper.instance);

		FileObject fo = resourceSerivce.resolve(dir.getAbsolutePath());
		Iterable<ILanguage> langs = discoverySerivce.discover(fo);
		if (langs == null || !langs.iterator().hasNext())
			throw new IllegalStateException("Failed to discover language for " + input.someClassFromLanguage);
		Iterator<ILanguage> it = langs.iterator();
		ILanguage lang = it.next();
		if (it.hasNext())
			throw new IllegalStateException("Discovered multiple languages for " + input.someClassFromLanguage);

		return Out.of(new KryoWrapper<>(lang));
	}
}
