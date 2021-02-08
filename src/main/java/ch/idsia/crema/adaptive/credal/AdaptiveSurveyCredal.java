package ch.idsia.crema.adaptive.credal;

import ch.idsia.crema.adaptive.credal.answering.AnswerStrategyCredalRandom;
import ch.idsia.crema.adaptive.credal.model.CredalMinimalistic;
import ch.idsia.crema.adaptive.credal.scoring.ScoringFunctionCredalInfoGain;
import ch.idsia.crema.adaptive.credal.stopping.StoppingConditionCredalMeanEntropy;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;

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
public class AdaptiveSurveyCredal {

	static final int N_STUDENTS = 1;

	public static void main(String[] args) throws Exception {
		ExecutorService es = Executors.newFixedThreadPool(1); // TODO: add multiple threads

		final List<Callable<Void>> tasks = IntStream.range(0, N_STUDENTS)
				.boxed()
				.map(id -> (Callable<Void>) () -> {
					try {
						final AgentStudent<IntervalFactor> student = new AgentStudent<>(id,
								new CredalMinimalistic(5, .4, .4, .6, .6),
								new AnswerStrategyCredalRandom(id)
						);
						final AgentTeacher<IntervalFactor> teacher = new AgentTeacher<>(
								new CredalMinimalistic(5, .4, .4, .6, .6),
								new ScoringFunctionCredalInfoGain(),
								new StoppingConditionCredalMeanEntropy(.5)
						);

						while (!teacher.stop()) {
							Question q = teacher.next();
							if (q == null)
								break;

							final int x = student.answer(q);
							teacher.answer(q, x);
						}

						System.out.println(teacher.getNumberQuestionsDone());
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
