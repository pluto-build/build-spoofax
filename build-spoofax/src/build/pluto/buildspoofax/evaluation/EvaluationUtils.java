package build.pluto.buildspoofax.evaluation;

import java.io.File;
import java.io.IOException;

import org.sugarj.common.FileCommands;

public class EvaluationUtils {
	
	public static void replaceAllInFile(File file, String toReplace, String replaceWith) throws IOException {
		String content = FileCommands.readFileAsString(file);
		String newContent = content.replaceAll(toReplace, replaceWith);
		if (content.equals(newContent)) {
			throw new RuntimeException("Replacement in " + file + " did not changed content");
		}
		FileCommands.writeToFile(file, newContent);
	}

}
