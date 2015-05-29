package build.pluto.buildspoofax.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class GrammarChangeMeassurement implements EvaluationMeasurement {

	@Override
	public void modifyCleanBuild(File projectRoot) throws IOException {
		File grammarFile = new File(projectRoot, "syntax/sorts/Sorts.sdf3");
		EvaluationUtils.replaceAllInFile(grammarFile, Pattern.quote("{prefer}"), "");
	}

}
