package build.pluto.buildspoofax.evaluation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.sugarj.common.Exec;
import org.sugarj.common.Exec.ExecutionResult;

import build.pluto.builder.BuildManager;
import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.buildspoofax.Main;

public class Measurer {

	private final EvaluationMeasurement measurement;

	private final int numTries;

	private final File projectRoot;

	private final BuildRequest req;

	public Measurer(EvaluationMeasurement measurement, Main.Input input) {
		BuildManager.ASSERT_SERIALIZABLE = false;
		this.measurement = measurement;
		numTries = EvaluationConfiguration.NUM_TRIES;
		projectRoot = EvaluationConfiguration.PROJECT_ROOT;
		req = new BuildRequest(Main.factory, input);
	}

	public MeasurementResult measure() throws IOException {
		MeasurementResult result = new MeasurementResult();
		for (int i = 0; i < numTries; i++) {
			clean();
			long cleanTime = measureBuild();
			result.addCleanBuildTimeTime(cleanTime);
			measurement.modifyCleanBuild(projectRoot);
			long time = measureBuild();
			result.addIncrementalTime(time);
		}
		return result;
	}

	private void clean() throws IOException {
		BuildManagers.clean(false, req);

		ExecutionResult result = Exec.run(false, projectRoot.getParentFile(), "git", "reset", "--hard");
		if (result.errMsgs.length != 0) {
			throw new RuntimeException("Clean not successful " + Arrays.toString(result.errMsgs));
		}
	}

	private void build() {
		BuildManagers.build(req);
	}

	private long measureBuild() {
		long begin = System.nanoTime();
		build();
		long end = System.nanoTime();
		return end - begin;
	}

}
