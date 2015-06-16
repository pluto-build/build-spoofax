package build.pluto.buildspoofax.builders;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

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
import build.pluto.buildspoofax.builders.aux.DiscoverSpoofaxLanguage.DiscoverSpoofaxLanguageRequest;
import build.pluto.output.Out;
import build.pluto.output.OutputPersisted;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class CompileMetalanguageFiles_Parse extends SpoofaxBuilder<CompileMetalanguageFiles_Parse.Input, OutputPersisted<IStrategoTerm>> {
	private static final TypeLiteral<ISyntaxService<IStrategoTerm>> SYNTAX_LITERAL = new TypeLiteral<ISyntaxService<IStrategoTerm>>(){};

	public static SpoofaxBuilderFactory<Input, OutputPersisted<IStrategoTerm>, CompileMetalanguageFiles_Parse> factory = SpoofaxBuilderFactory.of(
			CompileMetalanguageFiles_Parse.class, Input.class);
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 37855003667874400L;

		public final File file;
		public final String langname;
		public final DiscoverSpoofaxLanguageRequest langDiscoverReq;

		public Input(SpoofaxContext context, File file, String langname, DiscoverSpoofaxLanguageRequest langDiscoverReq) {
			super(context);
			Objects.requireNonNull(langname);
			this.file = file;
			this.langname = langname;
			this.langDiscoverReq = langDiscoverReq;
		}
	}
	
	public CompileMetalanguageFiles_Parse(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Parse " + input.langname + " file " + FileCommands.getRelativePath(context.baseDir, input.file).toString();
	}
	
	@Override
	protected File persistentPath(Input input) {
		Path rel = FileCommands.getRelativePath(context.baseDir, input.file);
		String relname = rel.toString().replace(File.separatorChar, '_');
		return context.depPath("meta/parse." + input.langname + "." + relname + ".dep");
	}

	
	@Override
	public OutputPersisted<IStrategoTerm> build(Input input) throws Exception {
		Injector injector = context.guiceInjector();
		ISyntaxService<IStrategoTerm> syntaxService = injector.getInstance(Key.get(SYNTAX_LITERAL));
		IResourceService resourceSerivce = context.getResourceService();
		
		require(input.file);
		String source = FileCommands.readFileAsString(input.file);
		FileObject fo = resourceSerivce.resolve(input.file);
		Out<ILanguage> lang = requireBuild(input.langDiscoverReq);
		IStrategoTerm result = syntaxService.parse(source, fo, lang.val()).result;
		return OutputPersisted.of(result);
	}
}
