package build.pluto.buildspoofax.evaluation;

import java.io.File;
import java.io.IOException;

public class NameAnalysisMeasurement implements EvaluationMeasurement {

	@Override
	public void modifyCleanBuild(File projectRoot) throws IOException {
		File analysisFile = new File(projectRoot, "trans/analysis/attributes.str");
		EvaluationUtils.replaceAllInFile(analysisFile, "\"prefer\"", "\"\"");
	}

}
