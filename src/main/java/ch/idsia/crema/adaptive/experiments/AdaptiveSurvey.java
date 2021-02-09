package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.agents.AgentStudent;
import ch.idsia.crema.adaptive.experiments.agents.AgentTeacher;
import ch.idsia.crema.adaptive.experiments.agents.Student;
import ch.idsia.crema.adaptive.experiments.agents.Teacher;
import ch.idsia.crema.adaptive.experiments.answering.AnswerStrategyRandom;
import ch.idsia.crema.adaptive.experiments.answering.precise.AnswerStrategyBayesianStandard;
import ch.idsia.crema.adaptive.experiments.model.imprecise.CredalMinimalistic;
import ch.idsia.crema.adaptive.experiments.model.precise.BayesianMinimalistic;
import ch.idsia.crema.adaptive.experiments.persistence.PersistBayesian;
import ch.idsia.crema.adaptive.experiments.persistence.PersistCredal;
import ch.idsia.crema.adaptive.experiments.scoring.imprecise.ScoringFunctionUpperExpectedEntropy;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionExpectedEntropy;
import ch.idsia.crema.adaptive.experiments.scoring.precise.ScoringFunctionRandom;
import ch.idsia.crema.adaptive.experiments.stopping.StoppingConditionQuestionNumber;
import ch.idsia.crema.adaptive.experiments.stopping.imprecise.StoppingConditionCredalMeanEntropy;
import ch.idsia.crema.adaptive.experiments.stopping.precise.StoppingConditionBayesianMeanEntropy;

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
public class AdaptiveSurvey {

	static final int N_STUDENTS = 1;
	static final int PARALLEL_COUNT = 1; // TODO: add multiple threads

	public static void main(String[] args) throws Exception {

		// TODO: sampling students from skeleton

		System.out.println("\n================================================\n");
		System.out.println("Running Non Adaptive\n");

		nonAdaptive();

		System.out.println("\n================================================\n");
		System.out.println("Running Bayesian experiment\n");

		experimentBayesian();

		System.out.println("\n================================================\n");
		System.out.println("Running Credal experiment\n");

		experimentCredal();

		System.out.println("\n================================================\n");
	}

	static void nonAdaptive() throws Exception {
		ExecutorService es = Executors.newFixedThreadPool(PARALLEL_COUNT);
		final List<Callable<Void>> tasks = IntStream.range(0, N_STUDENTS)
				.boxed()
				.map(id -> (Callable<Void>) () -> {
					try {
						final AgentStudent student = new Student<>(id,
								new BayesianMinimalistic(5, .4, .6),
								new AnswerStrategyRandom<>(id)
						);
						final AgentTeacher teacher = new Teacher<>(
								new BayesianMinimalistic(5, .4, .6),
								new ScoringFunctionRandom(id),
								new StoppingConditionQuestionNumber<>(3)
						).setPersist(new PersistBayesian());

						new Experiment(teacher, student).run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				})
				.collect(Collectors.toList());

		es.invokeAll(tasks); // submit all tasks
		es.shutdown(); // wait until the end, then shutdown and proceed with the code
	}

	static void experimentBayesian() throws Exception {
		ExecutorService es = Executors.newFixedThreadPool(PARALLEL_COUNT);

		final List<Callable<Void>> tasks = IntStream.range(0, N_STUDENTS)
				.boxed()
				.map(id -> (Callable<Void>) () -> {
					try {
						final AgentStudent student = new Student<>(id,
								new BayesianMinimalistic(5, .4, .6),
								new AnswerStrategyBayesianStandard(id)
						).setSkill(new Skill(0), 1);
						final AgentTeacher teacher = new Teacher<>(
								new BayesianMinimalistic(5, .4, .6),
								new ScoringFunctionExpectedEntropy(),
								new StoppingConditionBayesianMeanEntropy(.5),
								new StoppingConditionQuestionNumber<>(5)
						).setPersist(new PersistBayesian());

						new Experiment(teacher, student).run();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				})
				.collect(Collectors.toList());

		es.invokeAll(tasks); // submit all tasks
		es.shutdown(); // wait until the end, then shutdown and proceede with the code
	}

	static void experimentCredal() throws Exception {
		ExecutorService es = Executors.newFixedThreadPool(PARALLEL_COUNT);

		final List<Callable<Void>> tasks = IntStream.range(0, N_STUDENTS)
				.boxed()
				.map(id -> (Callable<Void>) () -> {
					try {
						final AgentStudent student = new Student<>(id,
								new CredalMinimalistic(5, .4, .4, .6, .6),
								new AnswerStrategyRandom<>(id)
						);
						final AgentTeacher teacher = new Teacher<>(
								new CredalMinimalistic(5, .4, .4, .6, .6),
								new ScoringFunctionUpperExpectedEntropy(),
								new StoppingConditionCredalMeanEntropy(.5),
								new StoppingConditionQuestionNumber<>(5)
						).setPersist(new PersistCredal());

						new Experiment(teacher, student).run();
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
