package build.pluto.buildspoofax.builders;

import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.StrategoExecutor;
import build.pluto.buildspoofax.util.FileExtensionFilter;
import build.pluto.output.None;

import com.google.inject.Injector;

public class ForceOnSave extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, ForceOnSave> factory = new SpoofaxBuilderFactory<SpoofaxInput, None, ForceOnSave>() {
		private static final long serialVersionUID = 4436143308769039647L;

		@Override
		public ForceOnSave makeBuilder(SpoofaxInput input) { return new ForceOnSave(input); }
	};
	
	public ForceOnSave(SpoofaxInput input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Force on-save handlers for SDF3, NaBL, TS, etc.";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("forceOnSave.dep");
	}

	@Override
	public None build() throws Exception {
		// XXX really need to delete old sdf3 files? Or is it sufficient to remove them from `paths` below?
		List<RelativePath> oldSdf3Paths = FileCommands.listFilesRecursive(context.basePath("src-gen"), new FileExtensionFilter("sdf3"));
		for (Path p : oldSdf3Paths)
			FileCommands.delete(p);
		
		foo();
		
		List<RelativePath> paths = FileCommands.listFilesRecursive(
				context.baseDir, 
				new FileExtensionFilter("tmpl", "sdf3", "nab", "ts"));
		
		for (RelativePath p : paths)
			requireBuild(ForceOnSaveFile.factory, new ForceOnSaveFile.Input(context, p));
		
		return None.val;
	}

	private void foo() throws Exception {
		Injector injector = StrategoExecutor.guiceInjector();
		IResourceService resourceSerivce = injector.getInstance(IResourceService.class);
		ILanguageDiscoveryService discoverySerivce = injector.getInstance(ILanguageDiscoveryService.class);
		
		Path p = context.basePath("${include}");
		FileObject dir = resourceSerivce.resolve(p.getAbsolutePath());
		
		Iterable<ILanguage> langs = discoverySerivce.discover(dir);
		ILanguage lang = langs.iterator().next();
		System.out.println(lang);
	}

}
