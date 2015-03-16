package org.metaborg.spoofax.build.cleardep.builders;

import java.io.File;
import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.FileState;
import org.strategoxt.imp.runtime.services.OnSaveService;
import org.strategoxt.imp.runtime.services.StrategoObserver;
import org.strategoxt.imp.runtime.services.StrategoObserverUpdateJob;
import org.strategoxt.imp.runtime.stratego.EditorIOAgent;
import org.sugarj.cleardep.output.None;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class ForceOnSaveFile extends SpoofaxBuilder<ForceOnSaveFile.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, ForceOnSaveFile> factory = new SpoofaxBuilderFactory<Input, None, ForceOnSaveFile>() {
		private static final long serialVersionUID = 3624331674299289181L;

		@Override
		public ForceOnSaveFile makeBuilder(Input input) { return new ForceOnSaveFile(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = -2546907410371550065L;

		public final Path inputPath;
		public Input(SpoofaxContext context, Path inputPath) {
			super(context);
			this.inputPath = inputPath;
		}
	}
	
	public ForceOnSaveFile(Input input) {
		super(input);
	}
	
	@Override
	protected String taskDescription() {
		return "Force on-save handler for " + input.inputPath;
	}
	
	@Override
	protected Path persistentPath() {
		RelativePath rel = FileCommands.getRelativePath(context.baseDir, input.inputPath);
		String relname = rel.getRelativePath().replace(File.separatorChar, '_');
		return context.depPath("forceOnSaveFile/" + relname + ".dep");
	}

	@Override
	public None build() throws IOException {
		RelativePath p = FileCommands.getRelativePath(context.baseDir, input.inputPath);
		
		require(p);
		callOnSaveService(p);
		switch(FileCommands.getExtension(p)) {
//			case "tmpl": 
//				break;
		case "sdf3":
			RelativePath sdf3 = FileCommands.getRelativePath(context.basePath("syntax"), p);
			if (sdf3 == null)
				break;
			String sdf3RelNoExt = FileCommands.dropExtension(sdf3.getRelativePath());
			
			RelativePath genSdf = FileCommands.replaceExtension(context.basePath("${syntax}/" + sdf3.getRelativePath()), "sdf");
			RelativePath genPP = context.basePath("${pp}/" + sdf3RelNoExt + "-pp.str");
			RelativePath genCompletions = context.basePath("${completions}/" + sdf3RelNoExt + "-esv.esv");
			RelativePath genSignatures = context.basePath("${signatures}/" + sdf3RelNoExt + "-sig.str");
			provide(genSdf);
			provide(genPP);
			provide(genCompletions);
			provide(genSignatures);
			break;
		case "nab":
			RelativePath gen = FileCommands.replaceExtension(p, "str");
			provide(gen);
			break;
		case "ts":
			gen = FileCommands.replaceExtension(p, "generated.str");
			provide(gen);
			break;
		default:
			throw new UnsupportedOperationException("Dependency management not implemented for files with extension " + FileCommands.getExtension(p) + ". File was " + p);
		}
		
		return None.val;
	}
	
	private void callOnSaveService(RelativePath p) {
		try {
			FileState fileState = FileState.getFile(new org.eclipse.core.runtime.Path(p.getAbsolutePath()), null);
			if (fileState == null) {
				Log.log.logErr("Could not call on-save handler: File state could not be retrieved for file " + p, Log.CORE);
				return;
			}
			StrategoObserver observer = fileState.getDescriptor().createService(StrategoObserver.class, fileState.getParseController());
			SSLLibrary lib = SSLLibrary.instance(observer.getRuntime().getContext());
			if (lib.getIOAgent() instanceof EditorIOAgent && ((EditorIOAgent) lib.getIOAgent()).getJob() == null)
				((EditorIOAgent) lib.getIOAgent()).setJob(new StrategoObserverUpdateJob(observer));
			IStrategoTerm ast = fileState.getAnalyzedAst();
			OnSaveService onSave = fileState.getDescriptor().createService(OnSaveService.class, fileState.getParseController());
			onSave.invokeOnSave(ast);
		} catch (Exception e) {
			throw new RuntimeException("Could not call on-save handler.", e);
		}
	}

}
