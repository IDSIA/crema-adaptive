package ch.idsia.crema.adaptive.credal.model;

import ch.idsia.crema.adaptive.credal.Question;
import ch.idsia.crema.adaptive.credal.Skill;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:40
 */
public class CredalMinimalistic extends AbstractModelBuilder<IntervalFactor> {

	public CredalMinimalistic(int nQuestions, double l0, double l1, double u0, double u1) {
		model = new DAGModel<>();

		int s = model.addVariable(2);
		model.setFactor(s, new IntervalFactor(model.getDomain(s), Strides.EMPTY, new double[][]{{.45, .45}}, new double[][]{{.55, .55}}));

		skills.add(new Skill(s));

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

			questions.add(new Question(i, s, q, 1));
		}
	}

}
