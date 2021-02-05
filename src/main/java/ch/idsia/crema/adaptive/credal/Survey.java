package ch.idsia.crema.adaptive.credal;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.entropy.AbellanEntropy;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    04.02.2021 17:25
 */
class Survey {

	public static final int RIGHT = 0;

	final int student;
	// questionsPerSkillPerDifficulty: 4 skill, 4 difficulties, 5 questions
	final int[][][] qVariables = new int[4][4][5];

	private final AbellanEntropy entropy;
	private final ApproxLP2 approx;

	private final DAGModel<IntervalFactor> model;

	private final TIntIntMap observations = new TIntIntHashMap();

	/**
	 * {skill, difficulty} -> qVar
	 */
	private final Map<Integer[], LinkedList<Integer>> questionsAvailable = new HashMap<>();

	private int nQuestionsDone = 0;

	public Survey(int student) {
		this.student = student;

		// model is defined there
		model = model();

		entropy = new AbellanEntropy();

		approx = new ApproxLP2();
		approx.initialize(null);
//		approx.initialize(new HashMap<>() {{
//			put(ISearch.MAX_TIME, "8");
//			put(GreedyWithRandomRestart.MAX_RESTARTS, "4");
//			put(GreedyWithRandomRestart.MAX_PLATEAU, "2");
//		}});

		for (int skill = 0; skill < 4; skill++) {
			for (int difficulty = 0; difficulty < 4; difficulty++) {
				final Integer[] key = {skill, difficulty};
				questionsAvailable.put(key, new LinkedList<>());
				questionsAvailable.get(key).addAll(
						Arrays.stream(qVariables[skill][difficulty]).boxed().collect(Collectors.toList())
				);
			}
		}
	}

	public int getnQuestionsDone() {
		return nQuestionsDone;
	}

	/**
	 * @param question should be inside (5, 104)
	 * @param answer   should be 0 or 1
	 */
	void answer(int question, int answer) {
		observations.put(question, answer);
		nQuestionsDone++;
	}

	boolean stop() throws InterruptedException {
		double mean = 0;

		for (int skill = 0; skill < 4; skill++) {
			final IntervalFactor res = approx.query(model, skill, observations);

			// compute entropy of the current skill
			final double[] PS = entropy.getMaxEntropy(res.getLower(), res.getUpper());
			final double HS = H(PS);

			mean += HS / 4;
		}

		return mean < 0.2;
	}

	/**
	 * @return the next question based on an entropy analysis, -1 if nothing is found
	 */
	int next() throws InterruptedException {
		double maxIG = 0.;
		Integer[] nextKey = null;
		int nextQuestion = -1;

		// for each skill...
		for (int skill = 0; skill < 4; skill++) {
			final IntervalFactor res = approx.query(model, skill, observations);

			// compute entropy of the current skill
			final double[] PS = entropy.getMaxEntropy(res.getLower(), res.getUpper());
			final double HS = H(PS);

			// ...and for each difficulty
			for (int difficulty = 0; difficulty < 4; difficulty++) {
				final Integer[] key = {skill, difficulty};
				final LinkedList<Integer> questions = questionsAvailable.get(key);
				if (questions.isEmpty())
					continue;

				// get the next possible query (one of the many with the given skill and difficulty)
				final Integer q = questions.peekFirst();

				/*
				 *******************************************************************************************************
				 * This is what's used to optimize for the next question q
				 */

				// compute... something similar to a information gain
				final double[] HSQs = new double[2];
				for (int a = 0; a < 2; a++) {
					TIntIntMap obs = new TIntIntHashMap(observations);
					obs.put(q, a);

					final IntervalFactor query = approx.query(model, skill, obs);
					final double[] PSq = entropy.getMaxEntropy(query.getLower(), query.getUpper());
					final double HSq = H(PSq);

					HSQs[a] = HSq;
				}

				final IntervalFactor pQ = approx.query(model, q, observations);
				final double[] lower = pQ.getLower();

				final double score0 = lower[0] * HSQs[0] + (1 - lower[0]) * HSQs[1];
				final double score1 = lower[1] * HSQs[1] + (1 - lower[1]) * HSQs[0];
				final double score = HS - Math.max(score0, score1);

				/*
				 *******************************************************************************************************
				 */

				if (score > maxIG) {
					maxIG = score;
					nextKey = key;
					nextQuestion = q;
				}
			}
		}

		if (nextQuestion >= 0)
			questionsAvailable.get(nextKey).remove(nextQuestion);

		return nextQuestion;
	}

	private double H(double[] d) {
		double h = 0.0;

		for (double v : d) {
			// log base 4
			double logXv = Math.log(v) / Math.log(d.length);
			h += v * logXv;
		}

		return -h;
	}

	/**
	 * @return a new credal model defined with {@link IntervalFactor}s.
	 */
	/*
	 TODO: this is absolutely not generic! It has always:
	    - 4 skills with 4 states
	    - 20 questions for each skill, where
	    - questions have 2 states, and
	    - questions have 4 different definitions (based on 4 difficulty levels)
	 */
	public DAGModel<IntervalFactor> model() {
		DAGModel<IntervalFactor> model = new DAGModel<>();

		// skill-chain
		// S0 -> S1 -> S2 -> S3
		//  v     v     v     v
		// Q0*   Q1*   Q2*   Q3*
		int S0 = addSkillNode(model);
		int S1 = addSkillNode(model, S0);
		int S2 = addSkillNode(model, S1);
		int S3 = addSkillNode(model, S2);

		/*

		Sx -> {
			easy: 1 2 3 4 5
			medium: 1 2 3 4 5
			...
		}
		 */

		// for each skill...
		for (int s = S0; s <= S3; s++) {
			// ...add 5 questions...
			for (int q = 0; q < 5; q++) {
				// ...for all levels of difficulty
				qVariables[s][0][q] = addQuestionNodeEasy(model, s);
				qVariables[s][1][q] = addQuestionNodeMediumEasy(model, s);
				qVariables[s][2][q] = addQuestionNodeMediumHard(model, s);
				qVariables[s][3][q] = addQuestionNodeHard(model, s);
			}
		}

		return model;
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
	 * @return the new variable added
	 */
	public int addQuestionNodeEasy(DAGModel<IntervalFactor> model, int parent) {
		final int q = model.addVariable(2);
		model.addParent(q, parent);
		final IntervalFactor fQ = new IntervalFactor(model.getDomain(q), model.getDomain(parent));

		fQ.setLower(new double[]{.600, .375}, 0); // lP(Q=right|S=0)
		fQ.setLower(new double[]{.750, .225}, 1); // lP(Q=right|S=1)
		fQ.setLower(new double[]{.850, .125}, 2); // lP(Q=right|S=2)
		fQ.setLower(new double[]{.950, .075}, 3); // lP(Q=right|S=3)

		fQ.setUpper(new double[]{.625, .400}, 0); // uP(Q=right|S=0)
		fQ.setUpper(new double[]{.775, .250}, 1); // uP(Q=right|S=1)
		fQ.setUpper(new double[]{.875, .150}, 2); // uP(Q=right|S=2)
		fQ.setUpper(new double[]{.975, .050}, 3); // uP(Q=right|S=3)

		model.setFactor(q, fQ);
		return q;
	}

	/**
	 * Add a question node for the medium-easy difficulty.
	 *
	 * @param model  add to this model
	 * @param parent skill parent node
	 * @return the new variable added
	 */
	public int addQuestionNodeMediumEasy(DAGModel<IntervalFactor> model, int parent) {
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
		return q;
	}

	/**
	 * Add a question node for the medium-hard difficulty.
	 *
	 * @param model  add to this model
	 * @param parent skill parent node
	 * @return the new variable added
	 */
	public int addQuestionNodeMediumHard(DAGModel<IntervalFactor> model, int parent) {
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
		return q;
	}

	/**
	 * Add a question node for the hard difficulty.
	 *
	 * @param model  add to this model
	 * @param parent skill parent node
	 * @return the new variable added
	 */
	public int addQuestionNodeHard(DAGModel<IntervalFactor> model, int parent) {
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
		return q;
	}
}
