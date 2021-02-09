package ch.idsia.crema.adaptive.experiments.model;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.Skill;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:40
 */
public class BayesianMinimalistic extends AbstractModelBuilder<BayesianFactor> {

	public BayesianMinimalistic(int nQuestions, double p1, double p0) {
		model = new DAGModel<>();

		int s = model.addVariable(2);
		model.setFactor(s, new BayesianFactor(model.getDomain(s), new double[]{.5, .5}));

		skills.add(new Skill(s));

		for (int i = 0; i < nQuestions; i++) {
			final int q = model.addVariable(2);
			model.addParent(q, s);
			model.setFactor(q, new BayesianFactor(model.getDomain(s, q), new double[]{p1, 1 - p1, p0, 1 - p0}));

			questions.add(new Question(i, s, q, 1));
		}
	}

}
