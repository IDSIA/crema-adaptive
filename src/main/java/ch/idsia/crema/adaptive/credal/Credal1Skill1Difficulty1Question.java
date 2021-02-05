package ch.idsia.crema.adaptive.credal;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 13:45
 */
public class Credal1Skill1Difficulty1Question extends ModelBuilder<IntervalFactor> {

	final DAGModel<IntervalFactor> model;

	public Credal1Skill1Difficulty1Question() {
		super(1, 1);
		model = new DAGModel<>();

		final int q = model.addVariable(2);
		final int s = model.addVariable(4);

		model.addParent(q, s);

		final IntervalFactor fS = new IntervalFactor(model.getDomain(s), Strides.EMPTY);
		fS.setLower(new double[]{
				.1, .3, .3, .1
		});
		fS.setUpper(new double[]{
				.2, .4, .4, .2
		});
		model.setFactor(s, fS);
		final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(s));

		fQ.setLower(new double[]{.600, .375}, 0); // lP(Q=right|S=0)
		fQ.setLower(new double[]{.750, .225}, 1); // lP(Q=right|S=1)
		fQ.setLower(new double[]{.850, .125}, 2); // lP(Q=right|S=2)
		fQ.setLower(new double[]{.950, .025}, 3); // lP(Q=right|S=3)

		fQ.setUpper(new double[]{.625, .400}, 0); // uP(Q=right|S=0)
		fQ.setUpper(new double[]{.775, .250}, 1); // uP(Q=right|S=1)
		fQ.setUpper(new double[]{.875, .150}, 2); // uP(Q=right|S=2)
		fQ.setUpper(new double[]{.975, .050}, 3); // uP(Q=right|S=3)

		model.setFactor(q, fQ);

		varSkills[0] = s;
		varQuestions[0][0] = q;
	}

	/**
	 * The model is composed as following:
	 * - 4 skills with 4 states
	 * - 20 questions for each skill, where
	 * - questions have 2 states, and
	 * - questions have 4 different definitions (based on 4 difficulty levels)
	 *
	 * @return a new credal model defined with {@link IntervalFactor}s.
	 */
	@Override
	public DAGModel<IntervalFactor> getModel() {
		return model;
	}
}
