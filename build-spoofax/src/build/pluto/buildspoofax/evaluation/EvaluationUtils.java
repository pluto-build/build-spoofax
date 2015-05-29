package build.pluto.buildspoofax.evaluation;

import java.io.File;
import java.io.IOException;

import org.sugarj.common.FileCommands;

public class EvaluationUtils {
	
	public static void replaceAllInFile(File file, String toReplace, String replaceWith) throws IOException {
		String content = FileCommands.readFileAsString(file);
		content = content.replaceAll(toReplace, replaceWith);
		FileCommands.writeToFile(file, content);
	}

}
