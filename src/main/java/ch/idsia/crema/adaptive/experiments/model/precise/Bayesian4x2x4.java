package ch.idsia.crema.adaptive.experiments.model.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Giorgia Adorni
 * Project: crema-adaptive
 * Date:    11.02.2021 17:00
 */
public class Bayesian4x2x4 extends AbstractModelBuilder<BayesianFactor> {

	/**
	 * Build a Bayesian model where we have 4 skills with 4 states each.
	 * Each skill has 4 templates of questions, each template has nQuestions
	 * questions, and each question has 2 states.
	 * All the questions in a template have the same CPT.
	 *
	 * @param nQuestions number of questions in total, all the questions in a
	 *                   template have the same CPT.
	 */
	public Bayesian4x2x4(int nQuestions) {

		model = new DAGModel<>();

		// skill-chain
		// S0 -> S1 -> S2 -> S3
		//  v     v     v     v
		// Q0    Q1    Q2    Q3
		int S0 = addSkillNode(model, .5);
		int S1 = addSkillNode(model, S0, .8, .2);
		int S2 = addSkillNode(model, S1, .8, .2);
		int S3 = addSkillNode(model, S2, .8, .2);

		// for each skill...
		for (int s = S0; s <= S3; s++) {
			// ...add question nodes
			for (int i = 0; i < nQuestions; i++) {
				addQuestion(model, s, 10 * s + 1, .6, .9);
				addQuestion(model, s, 10 * s + 2, .4, .9);
				addQuestion(model, s, 10 * s + 3, .1, .7);
				addQuestion(model, s, 10 * s + 4, .1, .6);
			}
		}
	}

	/**
	 * Add a skill node without parents.
	 *
	 * @param model add to this model
	 * @return the new variable added
	 */
	int addSkillNode(DAGModel<BayesianFactor> model, double p) {
		int s = model.addVariable(2);

		model.setFactor(s, new BayesianFactor(model.getDomain(s), new double[]{p, 1-p}));

		skills.add(s);

		return s;
	}

	/**
	 * Add a skill node a single parent.
	 *
	 * @param model  add to this model
	 * @param parent parent node
	 * @return the new variable added
	 */
	int addSkillNode(DAGModel<BayesianFactor> model, int parent, double p0, double p1) {
		int s = model.addVariable(2);
		model.addParent(s, parent);

		final BayesianFactor bF = new BayesianFactor(model.getDomain(parent, s),
				new double[]{
						/*P(s0=0|s1=0)*/     p0, /*P(s0=1|s1=0)*/    p1,
						/*P(s0=0|s1=1)*/ 1 - p0, /*P(s0=1|s1=1)*/1 - p1,
				});

		model.setFactor(s, bF);

		skills.add(s);

		return s;
	}

	/**
	 * Add a question node for the easy difficulty.
	 *
	 * @param model  add to this model
	 * @param parent skill parent node
	 */
	public void addQuestion(DAGModel<BayesianFactor> model, int parent, int template, double p0, double p1) {
		final int q = model.addVariable(2);
		model.addParent(q, parent);

		final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), new double[]{
				/*P(S=0|q=right)*/    p0, /*P(S=1|q=wrong)*/     p1,
				/*P(S=0|q=wrong)*/1 - p0, /*P(S=1|q=wrong)*/ 1 - p1,
		});

		model.setFactor(q, fQ);

		questions.add(new Question(parent, q, template));
	}

}
