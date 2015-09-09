package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.nativebundle.NativeBundle;
import org.metaborg.util.file.FileUtils;
import org.sugarj.common.FileCommands;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.None;
import build.pluto.stamp.FileExistsStamper;
import build.pluto.stamp.LastModifiedStamper;

import com.google.common.base.Joiner;

public class MetaSdf2Table extends SpoofaxBuilder<MetaSdf2Table.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, MetaSdf2Table> factory = SpoofaxBuilderFactory.of(MetaSdf2Table.class, Input.class);

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -3179663405417276186L;
		
		public final String metaModule;

		public Input(SpoofaxContext context, String metaModule) {
			super(context);
			this.metaModule = metaModule;
		}
	}
	
	public MetaSdf2Table(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Compile metagrammar for concrete object syntax";
	}
	
	@Override
	protected File persistentPath(Input input) {
		return context.depPath("metaSdf2Table." + input.metaModule + ".dep");
	}

	@Override
	public None build(Input input) throws IOException {
		File metamodule = FileUtils.toFile(context.settings.getSdfMainFile(input.metaModule));
		require(metamodule, SpoofaxContext.BETTER_STAMPERS ? FileExistsStamper.instance : LastModifiedStamper.instance);
		boolean metasdfmoduleAvailable = FileCommands.exists(metamodule);
		
		if (metasdfmoduleAvailable) {
	        final FileObject strategoMixPath = context.resourceService.resolve(NativeBundle.getStrategoMix().toString());
	        final File strategoMixFile = context.resourceService.localFile(strategoMixPath);
		    provide(strategoMixFile);
			
			String sdfArgs = "-Idef " + strategoMixFile + " " + Joiner.on(' ').join(context.settings.sdfArgs());
			requireBuild(Sdf2Table.factory, new Sdf2Table.Input(context, input.metaModule, sdfArgs));
		}
		
		return None.val;
	}

}
