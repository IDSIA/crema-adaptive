package ch.idsia.crema.adaptive.experiments.model.imprecise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.Skill;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    05.02.2021 13:45
 */
public class Credal4Skill4Difficulty5Question extends AbstractModelBuilder<IntervalFactor> {

	/**
	 * Build a Credal model where we have 4 skills with 4 state each. Each skill has 4 templates of questions, each
	 * template has 5 questions, and each question has 2 states. All the questions in a template have the same CPT.
	 */
	public Credal4Skill4Difficulty5Question() {
		model = new DAGModel<>();

		// skill-chain
		// S0 -> S1 -> S2 -> S3
		//  v     v     v     v
		// Q0    Q1    Q2    Q3
		int S0 = addSkillNode(model);
		int S1 = addSkillNode(model, S0);
		int S2 = addSkillNode(model, S1);
		int S3 = addSkillNode(model, S2);

		// for each skill...
		int qid = 0;
		for (int s = S0; s <= S3; s++) {
			// ...add question nodes
			for (int i = 0; i < 5; i++) {
				addQuestionNodeEasy(model, s, qid, 10 * s + 1);
				addQuestionNodeMediumEasy(model, s, qid, 10 * s + 2);
				addQuestionNodeMediumHard(model, s, qid, 10 * s + 3);
				addQuestionNodeHard(model, s, qid, 10 * s + 4);
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
		int s = model.addVariable(4);
		final IntervalFactor fS = new IntervalFactor(model.getDomain(s), Strides.EMPTY);
		fS.setLower(new double[]{
				.1, .3, .3, .1
		});
		fS.setUpper(new double[]{
				.2, .4, .4, .2
		});
		model.setFactor(s, fS);

		skills.add(new Skill(s));

		return s;
	}

	/**
	 * Add a skill node a single parent.
	 *
	 * @param model  add to this model
	 * @param parent parent node
	 * @return the new variable added
	 */
	int addSkillNode(DAGModel<IntervalFactor> model, int parent) {
		int s = model.addVariable(4);
		model.addParent(s, parent);

		final IntervalFactor fS = new IntervalFactor(model.getDomain(s), model.getDomain(parent));
		fS.setLower(new double[]{.30, .20, .10, .01}, 0); // lP(S1|S0=0)
		fS.setLower(new double[]{.20, .30, .20, .10}, 1); // lP(S1|S0=1)
		fS.setLower(new double[]{.10, .20, .30, .20}, 2); // lP(S1|S0=2)
		fS.setLower(new double[]{.01, .10, .20, .30}, 3); // lP(S1|S0=3)

		fS.setUpper(new double[]{.40, .30, .20, .10}, 0);   // uP(S1|S0=0)
		fS.setUpper(new double[]{.30, .40, .30, .20}, 1);   // uP(S1|S0=1)
		fS.setUpper(new double[]{.20, .30, .40, .30}, 2);   // uP(S1|S0=2)
		fS.setUpper(new double[]{.10, .20, .30, .40}, 3);   // uP(S1|S0=3)

		model.setFactor(s, fS);
		return s;
	}

	/**
	 * Add a question node for the easy difficulty.
	 *
	 * @param model  add to this model
	 * @param parent skill parent node
	 */
	public void addQuestionNodeEasy(DAGModel<IntervalFactor> model, int parent, int qid, int template) {
		final int q = model.addVariable(2);
		model.addParent(q, parent);
		final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));

		fQ.setLower(new double[]{.600, .375}, 0); // lP(Q=right|S=0)
		fQ.setLower(new double[]{.750, .225}, 1); // lP(Q=right|S=1)
		fQ.setLower(new double[]{.850, .125}, 2); // lP(Q=right|S=2)
		fQ.setLower(new double[]{.950, .025}, 3); // lP(Q=right|S=3)

		fQ.setUpper(new double[]{.625, .400}, 0); // uP(Q=right|S=0)
		fQ.setUpper(new double[]{.775, .250}, 1); // uP(Q=right|S=1)
		fQ.setUpper(new double[]{.875, .150}, 2); // uP(Q=right|S=2)
		fQ.setUpper(new double[]{.975, .050}, 3); // uP(Q=right|S=3)

		model.setFactor(q, fQ);

		questions.add(new Question(qid, parent, q, template));
	}

	/**
	 * Add a question node for the medium-easy difficulty.
	 *
	 * @param model  add to this model
	 * @param parent skill parent node
	 */
	public void addQuestionNodeMediumEasy(DAGModel<IntervalFactor> model, int parent, int qid, int template) {
		final int q = model.addVariable(2);
		model.addParent(q, parent);
		final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));

		fQ.setLower(new double[]{.325, .650}, 0);// lP(Q=right|S=0)
		fQ.setLower(new double[]{.600, .375}, 1);// lP(Q=right|S=1)
		fQ.setLower(new double[]{.750, .225}, 2);// lP(Q=right|S=2)
		fQ.setLower(new double[]{.850, .175}, 3);// lP(Q=right|S=3)

		fQ.setUpper(new double[]{.350, .675}, 0); // uP(Q=right|S=0)
		fQ.setUpper(new double[]{.625, .400}, 1); // uP(Q=right|S=1)
		fQ.setUpper(new double[]{.775, .250}, 2); // uP(Q=right|S=2)
		fQ.setUpper(new double[]{.875, .150}, 3); // uP(Q=right|S=3)

		model.setFactor(q, fQ);

		questions.add(new Question(qid, parent, q, template));
	}

	/**
	 * Add a question node for the medium-hard difficulty.
	 *
	 * @param model  add to this model
	 * @param parent skill parent node
	 */
	public void addQuestionNodeMediumHard(DAGModel<IntervalFactor> model, int parent, int qid, int template) {
		final int q = model.addVariable(2);
		model.addParent(q, parent);
		final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));

		fQ.setLower(new double[]{.225, .750}, 0); // lP(Q=right|S=0)
		fQ.setLower(new double[]{.325, .650}, 1); // lP(Q=right|S=1)
		fQ.setLower(new double[]{.600, .375}, 2); // lP(Q=right|S=2)
		fQ.setLower(new double[]{.750, .225}, 3); // lP(Q=right|S=3)

		fQ.setUpper(new double[]{.250, .775}, 0); // uP(Q=right|S=0)
		fQ.setUpper(new double[]{.350, .675}, 1); // uP(Q=right|S=1)
		fQ.setUpper(new double[]{.625, .400}, 2); // uP(Q=right|S=2)
		fQ.setUpper(new double[]{.775, .250}, 3); // uP(Q=right|S=3)

		model.setFactor(q, fQ);

		questions.add(new Question(qid, parent, q, template));
	}

	/**
	 * Add a question node for the hard difficulty.
	 *
	 * @param model  add to this model
	 * @param parent skill parent node
	 */
	public void addQuestionNodeHard(DAGModel<IntervalFactor> model, int parent, int qid, int template) {
		final int q = model.addVariable(2);
		model.addParent(q, parent);
		final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));

		fQ.setLower(new double[]{.175, .800}, 0); // lP(Q=right|S=0)
		fQ.setLower(new double[]{.225, .750}, 1); // lP(Q=right|S=1)
		fQ.setLower(new double[]{.325, .650}, 2); // lP(Q=right|S=2)
		fQ.setLower(new double[]{.600, .375}, 3); // lP(Q=right|S=3)

		fQ.setUpper(new double[]{.200, .825}, 0); // uP(Q=right|S=0)
		fQ.setUpper(new double[]{.250, .775}, 1); // uP(Q=right|S=1)
		fQ.setUpper(new double[]{.350, .675}, 2); // uP(Q=right|S=2)
		fQ.setUpper(new double[]{.625, .400}, 3); // uP(Q=right|S=3)

		model.setFactor(q, fQ);

		questions.add(new Question(qid, parent, q, template));
	}

}
