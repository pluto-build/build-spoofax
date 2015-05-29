package build.pluto.buildspoofax.evaluation;

import java.util.ArrayList;
import java.util.List;

public class MeasurementResult {

	private List<Long> incrementalTimes = new ArrayList<>();
	private List<Long> cleanBuildTimes = new ArrayList<>();

	public void addIncrementalTime(long time) {
		incrementalTimes.add(time);
	}

	public void addCleanBuildTimeTime(long time) {
		cleanBuildTimes.add(time);
	}

	public List<Long> getIncrementalTimes() {
		return incrementalTimes;
	}

	public List<Long> getCleanBuildTimes() {
		return cleanBuildTimes;
	}

}
