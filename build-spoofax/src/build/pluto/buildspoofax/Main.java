package build.pluto.buildspoofax;

import java.io.IOException;
import java.util.Objects;

import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
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
		
		public final Path projectPath;

		public Input(Path projectPath) {
			super(SpoofaxContext.makeContext(Objects.requireNonNull(projectPath, "Spoofax builder requires project-path parameter")));
			this.projectPath = projectPath;
		}
	}
	
	public Main(Input input) {
		super(input);
	}

	@Override
	protected String description() {
		return null;
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("all.dep");
	}
	
	@Override
	public None build() throws IOException {
		RelativePath ppInput = context.basePath("${lib}/EditorService-pretty.pp");
		RelativePath ppTermOutput = context.basePath("${include}/EditorService-pretty.pp.af");
		requireBuild(PPPack.factory, new PPPack.Input(context, ppInput, ppTermOutput));
		
		requireBuild(SpoofaxDefaultCtree.factory, input);
		return None.val;
	}

}
