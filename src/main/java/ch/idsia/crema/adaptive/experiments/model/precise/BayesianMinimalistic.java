package ch.idsia.crema.adaptive.experiments.model.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:40
 */
public class BayesianMinimalistic extends AbstractModelBuilder<BayesianFactor> {

	/**
	 * A Bayesian model with one skill with two states and a given number of questions. All the questions have the same
	 * CPT that is built from the given <code>p1</code> and <code>p0</code> parameters.
	 *
	 * @param nQuestions number of questions in total, all questions will have the same template (value: 1).
	 * @param p1         probability to know the answer given that it has the skill (must be greater than p0)
	 * @param p0         probability to know the answer given that it has not the skill
	 */
	public BayesianMinimalistic(int nQuestions, double p1, double p0) {
		model = new DAGModel<>();

		int s = model.addVariable(2);
		model.setFactor(s, new BayesianFactor(model.getDomain(s), new double[]{.5, .5}));

		skills.add(s);

		for (int i = 0; i < nQuestions; i++) {
			final int q = model.addVariable(2);
			model.addParent(q, s);
			model.setFactor(q, new BayesianFactor(model.getDomain(s, q), new double[]{p1, p0, 1 - p1, 1 - p0}));

			questions.add(new Question(s, q, 1));
		}
	}

}
