package build.pluto.buildspoofax;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import build.pluto.buildspoofax.builders.PPPack;
import build.pluto.buildspoofax.builders.SpoofaxDefaultCtree;
import build.pluto.output.None;

public class Main extends SpoofaxBuilder<Main.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, Main> factory = new SpoofaxBuilderFactory<Input, None, Main>() {
		private static final long serialVersionUID = 1747202833519981639L;

		@Override
		public Main makeBuilder(Input input) { return new Main(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 8115987062955840937L;
		
		public final File projectPath;

		public Input(File projectPath) {
			super(SpoofaxContext.makeContext(Objects.requireNonNull(projectPath, "Spoofax builder requires project-path parameter")));
			this.projectPath = projectPath;
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
		File ppInput = context.basePath("${lib}/EditorService-pretty.pp");
		File ppTermOutput = context.basePath("${include}/EditorService-pretty.pp.af");
		requireBuild(PPPack.factory, new PPPack.Input(context, ppInput, ppTermOutput));
		
		requireBuild(SpoofaxDefaultCtree.factory, input);
		return None.val;
	}

}
