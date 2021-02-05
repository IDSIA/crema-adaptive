package ch.idsia.crema.adaptive.credal;

import ch.idsia.crema.entropy.AbellanEntropy;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.HashSet;
import java.util.Set;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    04.02.2021 17:25
 */
class Survey {

	final int student;

	private final ModelBuilder<? extends GenericFactor> builder;

	private final AbellanEntropy entropy;
	private final ApproxLP2 approx;

	private final DAGModel<? extends GenericFactor> model;

	private final TIntIntMap observations = new TIntIntHashMap();

	private final Set<Integer> questionsDone = new HashSet<>();

	public Survey(int student, ModelBuilder<? extends GenericFactor> builder) {
		this.builder = builder;
		this.student = student;

		// model is defined there
		model = builder.getModel();

		entropy = new AbellanEntropy();

		approx = new ApproxLP2();
		approx.initialize(null);
	}

	public int getNumberQuestionsDone() {
		return questionsDone.size();
	}

	/**
	 * @param question should be inside (5, 104)
	 * @param answer   should be 0 or 1
	 */
	public void answer(int question, int answer) {
		observations.put(question, answer);
		questionsDone.add(question);
	}

	/**
	 * @return return true if we need to stop, otherwise false
	 * @throws Exception if inference goes wrong
	 */
	public boolean stop() throws Exception {
		double mean = 0;

		for (int skill = 0; skill < 1; skill++) {
			final IntervalFactor res = approx.query(model, skill, observations);

			// compute entropy of the current skill
			final double[] PS = entropy.getMaxEntropy(res.getLower(), res.getUpper());
			final double HS = H(PS);

			mean += HS / 4;
		}

		return mean < 0.2;
	}

	/**
	 * @param skill    skill variable in the model
	 * @param question question variable in the model
	 * @return the score associated with the given question
	 * @throws Exception if inference goes wrong
	 */
	public double score(int skill, int question) throws Exception {
		// compute... something similar to a information gain
		final double[] HSQs = new double[2];
		for (int a = 0; a < 2; a++) {
			TIntIntMap obs = new TIntIntHashMap(observations);
			obs.put(question, a);

			final IntervalFactor query = approx.query(model, skill, obs);
			final double[] PSq = entropy.getMaxEntropy(query.getLower(), query.getUpper());
			final double HSq = H(PSq);

			HSQs[a] = HSq;
		}

		final IntervalFactor pQ = approx.query(model, question, observations);
		final double[] lower = pQ.getLower();

		final double score0 = lower[0] * HSQs[0] + (1 - lower[0]) * HSQs[1];
		final double score1 = lower[1] * HSQs[1] + (1 - lower[1]) * HSQs[0];

		return Math.max(score0, score1);
	}

	/**
	 * @return the next question based on an entropy analysis, -1 if nothing is found
	 * @throws Exception if inference goes wrong
	 */
	int next() throws Exception {
		double maxIG = 0.;
		int nextQuestion = -1;

		// for each skill...
		for (int s = 0; s < builder.varSkills.length; s++) {
			final int skill = builder.varSkills[s];

			// ...for each question
			for (int question : builder.varQuestions[s]) {
				if (questionsDone.contains(question))
					continue; // skip

				final double score = score(skill, question);

				if (score > maxIG) {
					maxIG = score;
					nextQuestion = question;
				}
			}
		}

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

}
