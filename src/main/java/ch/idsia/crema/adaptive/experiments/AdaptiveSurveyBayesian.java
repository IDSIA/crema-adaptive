package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.answering.AnswerStrategyBayesianRandom;
import ch.idsia.crema.adaptive.experiments.model.BayesianMinimalistic;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunctionBayesianInfoGain;
import ch.idsia.crema.adaptive.experiments.stopping.StoppingConditionBayesianMeanEntropy;
import ch.idsia.crema.factor.bayesian.BayesianFactor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    04.02.2021 16:09
 */
public class AdaptiveSurveyBayesian {

	static final int N_STUDENTS = 1;

	public static void main(String[] args) throws Exception {
		ExecutorService es = Executors.newFixedThreadPool(1); // TODO: add multiple threads

		final List<Callable<Void>> tasks = IntStream.range(0, N_STUDENTS)
				.boxed()
				.map(id -> (Callable<Void>) () -> {
					try {
						final AgentStudent<BayesianFactor> student = new AgentStudent<>(id,
								new BayesianMinimalistic(5, .4, .6),
								new AnswerStrategyBayesianRandom(id)
						);
						final AgentTeacher<BayesianFactor> teacher = new AgentTeacher<>(
								new BayesianMinimalistic(5, .4, .6),
								new ScoringFunctionBayesianInfoGain(),
								new StoppingConditionBayesianMeanEntropy(.5)
						);

						Experiment<BayesianFactor> experiment = new Experiment<>(teacher, student);
						experiment.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				})
				.collect(Collectors.toList());

		es.invokeAll(tasks); // submit all tasks
		es.shutdown(); // wait until the end, then shutdown and proceede with the code
	}

}
