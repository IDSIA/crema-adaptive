package ch.idsia.crema.adaptive.experiments.model.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.Skill;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 13:45
 */
public class BayesianLanguage1Skill extends AbstractModelBuilder<BayesianFactor> {

	/**
	 * @param nQuestions number of questions for the skill, all questions will have the same template
	 */
	public BayesianLanguage1Skill(int nQuestions) {
		model = new DAGModel<>();

		final int s = model.addVariable(4);
		final BayesianFactor fS = new BayesianFactor(model.getDomain(s), new double[]{.15, .35, .35, .15});
		model.setFactor(s, fS);
		skills.add(new Skill(s));

		for (int i = 0; i < nQuestions; i++) {
			final int q = model.addVariable(2);
			model.addParent(q, s);

			final BayesianFactor fQ = new BayesianFactor(model.getDomain(s, q), new double[]{
					.600, .400,
					.750, .250,
					.850, .150,
					.950, .050
			});
			// TODO: check correctness!

			model.setFactor(q, fQ);
			questions.add(new Question(i, s, q, 1));
		}
	}
}
