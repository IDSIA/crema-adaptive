package ch.idsia.crema.adaptive.experiments.model.imprecise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  		Claudio "Dna" Bonesana
 * Contributions: 	Giorgia Adorni
 * Project: 		crema-adaptive
 * Date:   			05.02.2021 13:45
 */
public class Credal4x2x4 extends AbstractModelBuilder<IntervalFactor> {

	private double eps = .05;

	public Credal4x2x4 setEps(double eps) {
		this.eps = eps;
		return this;
	}

	/**
	 * Build a Credal model where we have 4 skills with 4 states each.
	 * Each skill has 4 templates of questions, each template has nQuestions
	 * questions, and each question has 2 states.
	 * All the questions in a template have the same CPT.
	 *
	 * @param nQuestions number of questions in total, all the questions in a
	 *                   template have the same CPT.
	 */
	public Credal4x2x4(int nQuestions) {
		model = new DAGModel<>();

		// skill-chain
		// S0 -> S1 -> S2 -> S3
		//  v     v     v     v
		// Q0    Q1    Q2    Q3
		int S0 = addSkillNode(model);
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
	int addSkillNode(DAGModel<IntervalFactor> model) {
		int s = model.addVariable(2);
		final IntervalFactor fS = new IntervalFactor(model.getDomain(s), Strides.EMPTY);
		fS.setLower(new double[]{
				.5 - eps, .5 - eps,
		});
		fS.setUpper(new double[]{
				.5 + eps, .5 + eps
		});

		model.setFactor(s, fS);
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
	int addSkillNode(DAGModel<IntervalFactor> model, int parent, double p0, double p1) {
		int s = model.addVariable(2);
		model.addParent(s, parent);

		final IntervalFactor fS = new IntervalFactor(model.getDomain(s), model.getDomain(parent));
		fS.setLower(new double[]{p0 - eps, 1 - p0 - eps}, 0); // lP(S0|S1=0)
		fS.setUpper(new double[]{p0 + eps, 1 - p0 + eps}, 0); // uP(S0|S1=0)

		fS.setLower(new double[]{p1 - eps, 1 - p1 - eps}, 1); // lP(S0|S1=1)
		fS.setUpper(new double[]{p1 + eps, 1 - p1 + eps}, 1); // uP(S0|S1=1)

		model.setFactor(s, fS);
		skills.add(s);
		return s;
	}

	/**
	 * Add a question node for the easy difficulty.
	 *
	 * @param model  add to this model
	 * @param parent skill parent node
	 */
	public void addQuestion(DAGModel<IntervalFactor> model, int parent, int template, double p0, double p1) {
		final int q = model.addVariable(2);
		model.addParent(q, parent);
		final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));

		fQ.setLower(new double[]{p0 - eps, 1 - p0 - eps}, 0); // lP(Q=right|S=0)
		fQ.setUpper(new double[]{p0 + eps, 1 - p0 + eps}, 0); // uP(Q=right|S=0)

		fQ.setLower(new double[]{p1 - eps, 1 - p1 - eps}, 1); // lP(Q=right|S=1)
		fQ.setUpper(new double[]{p1 + eps, 1 - p1 + eps}, 1); // uP(Q=right|S=1)

		model.setFactor(q, fQ);

		questions.add(new Question(parent, q, template));
	}

}
