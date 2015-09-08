package build.pluto.buildspoofax.evaluation;

import java.io.IOException;
import java.util.List;

import build.pluto.buildspoofax.Main;

public class Evaluator {

	public void evaluate(Main.Input input) throws IOException {
		EvaluationResult evaluationResult = new EvaluationResult();
		for (EvaluationMeasurement measurement : EvaluationConfiguration.measurements) {
			MeasurementResult result = new Measurer(measurement, input).measure();
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
	    // TODO: instantiate with real input
		new Evaluator().evaluate(null);
	}

}
