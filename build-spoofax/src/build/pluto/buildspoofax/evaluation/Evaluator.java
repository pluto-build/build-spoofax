package build.pluto.buildspoofax.evaluation;

import java.io.IOException;
import java.util.List;

public class Evaluator {

	public void evaluate() throws IOException {
		EvaluationResult evaluationResult = new EvaluationResult();
		for (EvaluationMeasurement measurement : EvaluationConfiguration.measurements) {
			MeasurementResult result = new Measurer(measurement).measure();
			double cleanTime = calculateTime(result.getCleanBuildTimes());
			double incrementalTime = calculateTime(result.getIncrementalTimes());
			double speedup = cleanTime / incrementalTime;

			evaluationResult.putResult(measurement, cleanTime, incrementalTime, speedup);
		}
		System.out.println(evaluationResult);

	}

	private double calculateTime(List<Long> times) {
		double average = times.stream().mapToLong((Long l) -> l).skip(EvaluationConfiguration.NUM_DROP_FIRST).average().getAsDouble();
		return average;
	}

	public static void main(String[] args) throws IOException {
		new Evaluator().evaluate();
	}

}
