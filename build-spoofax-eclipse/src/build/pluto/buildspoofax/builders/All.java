package build.pluto.buildspoofax.builders;

import java.io.IOException;

import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.output.None;


public class All extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, All> factory = new SpoofaxBuilderFactory<SpoofaxInput, None, All>() {
		private static final long serialVersionUID = 1747202833519981639L;

		@Override
		public All makeBuilder(SpoofaxInput input) { return new All(input); }
	};
	

	public All(SpoofaxInput input) {
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
