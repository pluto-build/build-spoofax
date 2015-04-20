package build.pluto.buildspoofax.builders.aux;

import java.util.Iterator;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.stamp.LastModifiedStamper;

import com.google.inject.Injector;

public class DiscoverSpoofaxLanguage extends SpoofaxBuilder<DiscoverSpoofaxLanguage.Input, ILanguage> {

	public static SpoofaxBuilderFactory<Input, ILanguage, DiscoverSpoofaxLanguage> factory = new SpoofaxBuilderFactory<Input, ILanguage, DiscoverSpoofaxLanguage>() {
		private static final long serialVersionUID = -8387363389037442076L;

		@Override
		public DiscoverSpoofaxLanguage makeBuilder(Input input) { return new DiscoverSpoofaxLanguage(input); }
	};
	

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 12331766781256062L;

		public final Class<?> someClassFromLanguage;
		public Input(SpoofaxContext context, Class<?> someClassFromLanguage) {
			super(context);
			this.someClassFromLanguage = someClassFromLanguage;
		}
	}
	
	public DiscoverSpoofaxLanguage(Input input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Discover Spoofax language for " + input.someClassFromLanguage.getName();
	}
	
	@Override
	public Path persistentPath() {
		return context.depPath("discover." + input.someClassFromLanguage.getName() + ".dep");
	}

	@Override
	public ILanguage build() throws Exception {
		Injector injector = StrategoExecutor.guiceInjector();
		IResourceService resourceSerivce = injector.getInstance(IResourceService.class);
		ILanguageDiscoveryService discoverySerivce = injector.getInstance(ILanguageDiscoveryService.class);
		
		Path jar = FileCommands.getRessourcePath(input.someClassFromLanguage);
		Path dir;
		if (jar.getFile().isDirectory())
			dir = jar;
		else {
			dir = context.depPath("discover." + input.someClassFromLanguage.getName());
			requireBuild(UnpackJarFile.factory, new UnpackJarFile.Input(context, jar, dir));
		}
		
		for (Path p : FileCommands.listFilesRecursive(dir))
			require(p, LastModifiedStamper.instance);

		FileObject fo = resourceSerivce.resolve(dir.getAbsolutePath());
		Iterable<ILanguage> langs = discoverySerivce.discover(fo);
		if (langs == null || !langs.iterator().hasNext())
			throw new IllegalStateException("Failed to discover language for " + input.someClassFromLanguage);
		Iterator<ILanguage> it = langs.iterator();
		ILanguage lang = it.next();
		if (it.hasNext())
			throw new IllegalStateException("Discovered multiple languages for " + input.someClassFromLanguage);
		
		return lang;
	}
}
