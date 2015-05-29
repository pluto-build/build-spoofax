package build.pluto.buildspoofax.evaluation;

import java.io.File;
import java.io.IOException;

public class EditorServiceMeasurement implements EvaluationMeasurement {

	@Override
	public void modifyCleanBuild(File projectRoot) throws IOException {
		File typesFile = new File(projectRoot, "editor/TemplateLang-Colorer.esv");
		EvaluationUtils.replaceAllInFile(typesFile, "grey", "red");
	}

}
