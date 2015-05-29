package build.pluto.buildspoofax.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class NameAnalysisMeasurement implements EvaluationMeasurement {

	@Override
	public void modifyCleanBuild(File projectRoot) throws IOException {
		File analysisFile = new File(projectRoot, "trans/analysis/names.nab");
		EvaluationUtils.replaceAllInFile(analysisFile, Pattern.quote("Sort(s):\n \trefers to Sort s"), "");
	}

}
