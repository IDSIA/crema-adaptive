package ch.idsia.crema.adaptive.credal.model;

import ch.idsia.crema.adaptive.credal.Question;
import ch.idsia.crema.adaptive.credal.Skill;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 13:45
 */
public class CredalLanguage1Skill extends AbstractModelBuilder<IntervalFactor> {

	/**
	 * The model is composed as following:
	 * - 4 skills with 4 states
	 * - 20 questions for each skill, where
	 * - questions have 2 states, and
	 * - questions have 4 different definitions (based on 4 difficulty levels)
	 */
	public CredalLanguage1Skill(int nQuestions) {
		model = new DAGModel<>();

		final int s = model.addVariable(4);
		final IntervalFactor fS = new IntervalFactor(model.getDomain(s), Strides.EMPTY,
				new double[][]{{.1, .3, .3, .1}},
				new double[][]{{.2, .4, .4, .2}}
		);
		model.setFactor(s, fS);
		skills.add(new Skill(s));

		for (int i = 0; i < nQuestions; i++) {
			final int q = model.addVariable(2);
			model.addParent(q, s);

			final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(s),
					new double[][]{
							{.600, .375}, // lP(Q=right|S=0)
							{.750, .225}, // lP(Q=right|S=1)
							{.850, .125}, // lP(Q=right|S=2)
							{.950, .025}  // lP(Q=right|S=3)
					},
					new double[][]{
							{.625, .400}, // uP(Q=right|S=0)
							{.775, .250}, // uP(Q=right|S=1)
							{.875, .150}, // uP(Q=right|S=2)
							{.975, .050}  // uP(Q=right|S=3)
					}
			);

			model.setFactor(q, fQ);
			questions.add(new Question(i, s, q, 1));
		}
	}
}