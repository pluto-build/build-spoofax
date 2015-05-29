package build.pluto.buildspoofax.evaluation;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.sugarj.common.Log;

public class EvaluationConfiguration {

	public static final List<EvaluationMeasurement> measurements = Arrays.asList(new NoChangeMeasurement(), new GrammarChangeMeassurement(),
			new NameAnalysisMeasurement());
	public static final File PROJECT_ROOT = new File("/Volumes/DataSSD/Developer/Java/EclipseWorkspaces/HiwiSE/sdf/org.strategoxt.imp.editors.template");
	public static final int NUM_TRIES = 5;
	public static final int NUM_DROP_FIRST = 2;

	static {
		Log.log.setLoggingLevel(Log.CORE);
	}

}
