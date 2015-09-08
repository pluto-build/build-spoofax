package build.pluto.buildspoofax;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;

import build.pluto.buildspoofax.builders.PPPack;
import build.pluto.buildspoofax.builders.SpoofaxDefaultCtree;
import build.pluto.buildspoofax.builders.SpoofaxGenerator;
import build.pluto.output.None;

import com.google.inject.Injector;

public class Main extends SpoofaxBuilder<Main.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, Main> factory = SpoofaxBuilderFactory.of(Main.class, Input.class);
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 8115987062955840937L;
		
		public Input(Injector injector, SpoofaxProjectSettings settings) {
		    super(new SpoofaxContext(injector, settings));
		}
	}
	
	public Main(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return null;
	}
	
	@Override
	protected File persistentPath(Input input) {
		return context.depPath("all.dep");
	}
	
	@Override
	public None build(Input input) throws IOException {
	    requireBuild(SpoofaxGenerator.factory, input);
	    
		File ppInput = context.basePath("${lib}/EditorService-pretty.pp");
		File ppTermOutput = context.basePath("${include}/EditorService-pretty.pp.af");
		requireBuild(PPPack.factory, new PPPack.Input(context, ppInput, ppTermOutput, true));
		
		requireBuild(SpoofaxDefaultCtree.factory, input);
		return None.val;
	}

}
