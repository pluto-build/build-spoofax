package build.pluto.buildspoofax.evaluation;

import java.util.HashMap;
import java.util.Map;

public class EvaluationResult {

	private Map<EvaluationMeasurement, Double> averageTime = new HashMap<>();
	private Map<EvaluationMeasurement, Double> speedupTime = new HashMap<>();
	private Map<EvaluationMeasurement, Double> cleanTime = new HashMap<>();

	public void putResult(EvaluationMeasurement measurement, double clean, double averageTime, double speedup) {
		this.averageTime.put(measurement, averageTime);
		cleanTime.put(measurement, clean);
		speedupTime.put(measurement, speedup);
	}

	public double getAverageTime(EvaluationMeasurement measurement) {
		return averageTime.get(measurement);
	}

	public double getSpeedup(EvaluationMeasurement measurement) {
		return speedupTime.get(measurement);
	}

	public double getCleanTime(EvaluationMeasurement measurement) {
		return cleanTime.get(measurement);
	}

	@Override
	public String toString() {
		String s = "";
		for (EvaluationMeasurement measurement : averageTime.keySet()) {
			s += "== " + measurement.getClass().getSimpleName() + " ==\n";
			s += "Clean      : " + (getCleanTime(measurement) / 1000000000) + " s\n";
			s += "Incremental: " + (getAverageTime(measurement) / 1000000000) + " s\n";
			s += "Speedup:     " + (getSpeedup(measurement)) + " \n";
		}
		return s;
	}

}
