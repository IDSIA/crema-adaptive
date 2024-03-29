package ch.idsia.crema.adaptive.experiments.scoring.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.Utils;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.inference.bp.BeliefPropagation;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.commons.math3.util.Precision;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 15:23
 */
public class ScoringFunctionBayesianMode implements ScoringFunction<BayesianFactor> {

	private final BeliefPropagation<BayesianFactor> inference = new BeliefPropagation<>();

	/**
	 * A {@link ScoringFunction} based on the mode of the probability to answer correctly.
	 *
	 * @param model        model to work on
	 * @param question     question to evaluate
	 * @param observations evidence
	 * @return a score between 0 and 1
	 * @throws Exception if something bad happens
	 */
	@Override
	public double score(DAGModel<BayesianFactor> model, Question question, TIntIntMap observations) throws Exception {
		final BayesianFactor PQ = inference.query(model, observations, question.variable);
		final BayesianFactor PS = inference.query(model, observations, question.skill);
		final double HS = mode(PS);

		double[] modes = new double[2];

		double HSQ = 0;
		for (int a = 0; a < 2; a++) {
			TIntIntMap obs = new TIntIntHashMap(observations);
			obs.put(question.variable, a);

			final BayesianFactor bf = inference.query(model, obs, question.skill);
			final double Pqi = PQ.getValue(a);

			final double mode = mode(bf);
			modes[a] = mode;

			HSQ += mode * Pqi;
		}

		if (Precision.equals(modes[0], modes[1], 1e-6)) {
			System.err.printf("ScoringFunctionMode: Found question with same mode=%f %s %n", modes[0], question);
		}

		return HSQ - HS;
	}

	private double mode(BayesianFactor bf) {
		final double[] data = bf.getData();
		return data[Utils.argmax(data)];
	}

}
