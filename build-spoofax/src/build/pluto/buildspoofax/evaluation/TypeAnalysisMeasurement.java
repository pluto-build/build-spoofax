package build.pluto.buildspoofax.evaluation;

import java.io.File;
import java.io.IOException;

public class TypeAnalysisMeasurement implements EvaluationMeasurement {

	@Override
	public void modifyCleanBuild(File projectRoot) throws IOException {
		File typesFile = new File(projectRoot, "trans/analysis/types-ts.ts");
		EvaluationUtils.replaceAllInFile(typesFile, "// ", " ");
		EvaluationUtils.replaceAllInFile(typesFile, "//\t", " ");
	}

}
