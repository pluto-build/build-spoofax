package build.pluto.buildspoofax.evaluation;

import java.io.File;
import java.io.IOException;

public interface EvaluationMeasurement {
	
	public void modifyCleanBuild(File projectRoot) throws IOException;

}
