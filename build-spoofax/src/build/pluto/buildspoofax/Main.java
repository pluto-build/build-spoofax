package build.pluto.buildspoofax;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.core.project.settings.SpoofaxProjectSettings;
import org.metaborg.util.file.FileUtils;

import build.pluto.buildspoofax.builders.PPPack;
import build.pluto.buildspoofax.builders.SpoofaxDefaultCtree;
import build.pluto.buildspoofax.builders.SpoofaxGenerator;
import build.pluto.output.None;

public class Main extends SpoofaxBuilder<Main.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, Main> factory = SpoofaxBuilderFactoryFactory.of(Main.class, Input.class);
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 8115987062955840937L;
		
		public Input(SpoofaxProjectSettings settings, Iterable<String> javaClasspath) {
		    super(new SpoofaxContext(settings, javaClasspath));
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
    public File persistentPath(Input input) {
		return context.depPath("all.dep");
	}
	
	@Override
	public None build(Input input) throws IOException {
	    requireBuild(SpoofaxGenerator.factory, input);
	    
	    // TODO: this is not generic, get rid of this build step
		File ppInput = FileUtils.toFile(context.settings.getLibDirectory().resolveFile("EditorService-pretty.pp"));
		if(ppInput.exists()) {
    		File ppTermOutput = context.basePath("${include}/EditorService-pretty.pp.af");
    		requireBuild(PPPack.factory, new PPPack.Input(context, ppInput, ppTermOutput, true));
		}
		
		requireBuild(SpoofaxDefaultCtree.factory, input);
		return None.val;
	}

}
