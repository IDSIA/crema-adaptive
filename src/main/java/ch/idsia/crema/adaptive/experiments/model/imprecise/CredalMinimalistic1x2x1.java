package ch.idsia.crema.adaptive.experiments.model.imprecise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:40
 */
public class CredalMinimalistic1x2x1 extends AbstractModelBuilder<IntervalFactor> {

	/**
	 * A Bayesian model with one skill with two states and a given number of questions. All the questions have the same
	 * CPT that is built from the given lower and upper parameters.
	 *
	 * @param nQuestions number of questions in total, all questions will have the same template (value: 1).
	 * @param l0         lower probability to know the answer given that it has the skill
	 * @param l1         lower probability to know the answer given that it has not the skill
	 * @param u0         upper probability to know the answer given that it has the skill
	 * @param u1         upper probability to know the answer given that it has not the skill
	 */
	public CredalMinimalistic1x2x1(int nQuestions, double l0, double l1, double u0, double u1) {
		model = new DAGModel<>();

		int s = model.addVariable(2);
		model.setFactor(s, new IntervalFactor(model.getDomain(s), Strides.EMPTY, new double[][]{{.45, .45}}, new double[][]{{.55, .55}}));

		skills.add(s);

		for (int i = 0; i < nQuestions; i++) {
			final int q = model.addVariable(2);
			model.addParent(q, s);
			model.setFactor(q, new IntervalFactor(model.getDomain(q), model.getDomain(s),
					new double[][]{
							{l1, 1 - u1}, // lP(Q|S=0)
							{l0, 1 - u0}  // lP(Q|S=1)
					},
					new double[][]{
							{u1, 1 - l1}, // uP(Q|S=0)
							{u0, 1 - l0}  // uP(Q|S=1)
					}
			));

			questions.add(new Question(s, q, 1));
		}
	}

}
