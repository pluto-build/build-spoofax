package build.pluto.buildspoofax.builders;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.messages.IMessage;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.metaborg.spoofax.core.syntax.ParseResult;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilder.SpoofaxInput;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.StrategoExecutor;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class CompileMetalanguageFiles_Parse extends SpoofaxBuilder<CompileMetalanguageFiles_Parse.Input, ParseResult<IStrategoTerm>> {
	private static final TypeLiteral<ISyntaxService<IStrategoTerm>> SYNTAX_LITERAL = new TypeLiteral<ISyntaxService<IStrategoTerm>>(){};

	public static SpoofaxBuilderFactory<Input, ParseResult<IStrategoTerm>, CompileMetalanguageFiles_Parse> factory = new SpoofaxBuilderFactory<Input, ParseResult<IStrategoTerm>, CompileMetalanguageFiles_Parse>() {
		private static final long serialVersionUID = 4436143308769039647L;

		@Override
		public CompileMetalanguageFiles_Parse makeBuilder(Input input) { return new CompileMetalanguageFiles_Parse(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 37855003667874400L;

		public final Path file;
		public Input(SpoofaxContext context, Path file) {
			super(context);
			this.file = file;
		}
	}
	
	public CompileMetalanguageFiles_Parse(Input input) {
		super(input);
	}

	@Override
	protected String description() {
		return "Parse metalanguage file " + input.file;
	}
	
	@Override
	protected Path persistentPath() {
		RelativePath rel = FileCommands.getRelativePath(context.baseDir, input.file);
		String relname = rel.getRelativePath().replace(File.separatorChar, '_');
		return context.depPath("meta/parse." + relname + ".dep");
	}

	
	@Override
	public ParseResult<IStrategoTerm> build() throws Exception {
		Injector injector = StrategoExecutor.guiceInjector();
		ISyntaxService<IStrategoTerm> syntaxService = injector.getInstance(Key.get(SYNTAX_LITERAL));
		IResourceService resourceSerivce = injector.getInstance(IResourceService.class);
		ILanguageIdentifierService identifierService  = injector.getInstance(ILanguageIdentifierService.class);
		
		require(input.file);
		String source = FileCommands.readFileAsString(input.file);
		FileObject fo = resourceSerivce.resolve(input.file.getFile());
		ILanguage lang = identifierService.identify(fo);
		return syntaxService.parse(source, fo, lang);
	}
}
