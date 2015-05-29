package build.pluto.buildspoofax.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class CodeGenMeasurement implements EvaluationMeasurement {

	@Override
	public void modifyCleanBuild(File projectRoot) throws IOException {
		File typesFile = new File(projectRoot, "trans/generation/to-formatted.str");
		EvaluationUtils.replaceAllInFile(typesFile, Pattern.quote("template-elem-to-prod:\n  \tString(t) -> <string-to-prod> t"),
				"template-elem-to-prod:\n  	String(t) ->  t");
	}

}
