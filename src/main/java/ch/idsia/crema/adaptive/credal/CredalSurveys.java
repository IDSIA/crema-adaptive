package ch.idsia.crema.adaptive.credal;

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
public class CredalSurveys {

	static final int N_STUDENTS = 1;

	public static void main(String[] args) throws Exception {
		ExecutorService es = Executors.newFixedThreadPool(1); // TODO: add multiple threads

		final List<Callable<Void>> tasks = IntStream.range(0, N_STUDENTS)
				.boxed()
				.map(student -> (Callable<Void>) () -> {
					try {
						final AnswerFunction af = new AnswerFunctionRandom(student);
						final Survey survey = new Survey(
								student,
								new Credal4Skill4Difficulty20Question(),

								// these two can also be two lambdas :)
								new ScoringFunctionInfoGain(),
								new StoppingConditionEntropy()
						);

						int q;
						while (survey.stop()) {
							q = survey.next();
							if (q < 0)
								break;

							final int x = af.answer(q);
							survey.answer(q, x);
						}

						System.out.println(survey.getNumberQuestionsDone());
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
