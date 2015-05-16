package build.pluto.buildspoofax.builders;

import java.io.File;
import java.nio.file.Path;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.syntax.ISyntaxService;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.common.FileCommands;

import build.pluto.buildspoofax.SpoofaxBuilder;
import build.pluto.buildspoofax.SpoofaxBuilderFactory;
import build.pluto.buildspoofax.SpoofaxContext;
import build.pluto.buildspoofax.SpoofaxInput;
import build.pluto.output.Out;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class CompileMetalanguageFiles_Parse extends SpoofaxBuilder<CompileMetalanguageFiles_Parse.Input, Out<IStrategoTerm>> {
	private static final TypeLiteral<ISyntaxService<IStrategoTerm>> SYNTAX_LITERAL = new TypeLiteral<ISyntaxService<IStrategoTerm>>(){};

	public static SpoofaxBuilderFactory<Input, Out<IStrategoTerm>, CompileMetalanguageFiles_Parse> factory = CompileMetalanguageFiles_Parse::new;
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 37855003667874400L;

		public final File file;
		public final ILanguage lang;

		public Input(SpoofaxContext context, File file, ILanguage lang) {
			super(context);
			this.file = file;
			this.lang = lang;
		}
	}
	
	public CompileMetalanguageFiles_Parse(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Parse " + input.lang.name() + " file " + FileCommands.getRelativePath(context.baseDir, input.file).toString();
	}
	
	@Override
	protected File persistentPath(Input input) {
		Path rel = FileCommands.getRelativePath(context.baseDir, input.file);
		String relname = rel.toString().replace(File.separatorChar, '_');
		return context.depPath("meta/parse." + input.lang.name() + "." + relname + ".dep");
	}

	
	@Override
	public Out<IStrategoTerm> build(Input input) throws Exception {
		Injector injector = context.guiceInjector();
		ISyntaxService<IStrategoTerm> syntaxService = injector.getInstance(Key.get(SYNTAX_LITERAL));
		IResourceService resourceSerivce = context.getResourceService();
		
		require(input.file);
		String source = FileCommands.readFileAsString(input.file);
		FileObject fo = resourceSerivce.resolve(input.file);
		return Out.of(syntaxService.parse(source, fo, input.lang).result);
	}
}
