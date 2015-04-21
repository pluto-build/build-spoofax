package build.pluto.buildspoofax.builders;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.SpoofaxContext;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class CompileMetalanguageFiles_Parse extends SpoofaxBuilder<CompileMetalanguageFiles_Parse.Input, IStrategoTerm> {
	private static final TypeLiteral<ISyntaxService<IStrategoTerm>> SYNTAX_LITERAL = new TypeLiteral<ISyntaxService<IStrategoTerm>>(){};

	public static SpoofaxBuilderFactory<Input, IStrategoTerm, CompileMetalanguageFiles_Parse> factory = new SpoofaxBuilderFactory<Input, IStrategoTerm, CompileMetalanguageFiles_Parse>() {
		private static final long serialVersionUID = 4436143308769039647L;

		@Override
		public CompileMetalanguageFiles_Parse makeBuilder(Input input) { return new CompileMetalanguageFiles_Parse(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 37855003667874400L;

		public final Path file;
		public final ILanguage lang;
		public Input(SpoofaxContext context, Path file, ILanguage lang) {
			super(context);
			this.file = file;
			this.lang = lang;
		}
	}
	
	public CompileMetalanguageFiles_Parse(Input input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Parse " + input.lang.name() + " file " + FileCommands.getRelativePath(context.baseDir, input.file).getRelativePath();
	}
	
	@Override
	protected Path persistentPath() {
		RelativePath rel = FileCommands.getRelativePath(context.baseDir, input.file);
		String relname = rel.getRelativePath().replace(File.separatorChar, '_');
		return context.depPath("meta/parse." + input.lang.name() + "." + relname + ".dep");
	}

	
	@Override
	public IStrategoTerm build() throws Exception {
		Injector injector = context.guiceInjector();
		ISyntaxService<IStrategoTerm> syntaxService = injector.getInstance(Key.get(SYNTAX_LITERAL));
		IResourceService resourceSerivce = context.getResourceService();
		
		require(input.file);
		String source = FileCommands.readFileAsString(input.file);
		FileObject fo = resourceSerivce.resolve(input.file.getFile());
		return syntaxService.parse(source, fo, input.lang).result;
	}
}
