package ch.idsia.crema.adaptive.experiments.scoring.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.scoring.ScoringFunction;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;

import java.util.Random;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 09:46
 */
public class ScoringFunctionRandom implements ScoringFunction<BayesianFactor> {

	final Random random;

	public ScoringFunctionRandom(long seed) {
		this.random = new Random(seed);
	}

	/**
	 * This scoring function can be used to propose answers in a pseudo-random order.
	 *
	 * @param model        model to work on
	 * @param question     question to evaluate
	 * @param observations evidence
	 * @return a random number between 0 and 1
	 * @throws Exception but should never happen...
	 */
	@Override
	public double score(DAGModel<BayesianFactor> model, Question question, TIntIntMap observations) throws Exception {
		return random.nextDouble();
	}
}
