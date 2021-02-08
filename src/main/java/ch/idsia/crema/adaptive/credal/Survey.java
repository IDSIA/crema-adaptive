package ch.idsia.crema.adaptive.credal;

import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.HashSet;
import java.util.Set;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    04.02.2021 17:25
 */
class Survey {

	final int student;

	private final AbstractModelBuilder<? extends GenericFactor> builder;
	private final DAGModel<? extends GenericFactor> model;

	private final TIntIntMap observations = new TIntIntHashMap();

	private final Set<Integer> questionsDone = new HashSet<>();

	private final ScoringFunction scoringFunction;
	private final StoppingCondition stoppingCondition;

	public Survey(int student, AbstractModelBuilder<? extends GenericFactor> builder, ScoringFunction scoringFunction, StoppingCondition stoppingCondition) {
		this.builder = builder;
		this.student = student;

		this.scoringFunction = scoringFunction;
		this.stoppingCondition = stoppingCondition;

		// model is defined there
		model = builder.getModel();
	}

	public int getNumberQuestionsDone() {
		return questionsDone.size();
	}

	/**
	 * @param question should be inside (5, 104)
	 * @param answer   should be 0 or 1
	 */
	public void answer(int question, int answer) {
		observations.put(question, answer);
		questionsDone.add(question);
	}

	/**
	 * @return return true if we need to stop, otherwise false
	 * @throws Exception if inference goes wrong
	 */
	public boolean stop() throws Exception {
		return stoppingCondition.stop(model, builder.skills, observations);
	}

	/**
	 * @return the next question based on an entropy analysis, -1 if nothing is found
	 * @throws Exception if inference goes wrong
	 */
	int next() throws Exception {
		double maxIG = 0.;
		int nextQuestion = -1;

		// for each skill...
		for (int s = 0; s < builder.skills.length; s++) {
			final int skill = builder.skills[s];

			// ...for each question
			for (int question : builder.questions[s]) {
				if (questionsDone.contains(question))
					continue; // skip

				final double score = scoringFunction.score(model, skill, question, observations);

				if (score > maxIG) {
					maxIG = score;
					nextQuestion = question;
				}
			}
		}

		return nextQuestion;
	}

}
