package ch.idsia.crema.adaptive.experiments.inference;

import ch.idsia.crema.core.Strides;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    15.04.2021 08:52
 */
class MergeObservedTest {

	@Test
	void testMergeObservedMerge2of2() {
		DAGModel<IntervalFactor> model = new DAGModel<>();

		final int y = model.addVariable(4);
		final int x1 = model.addVariable(2);
		final int x2 = model.addVariable(2);

		model.addParent(x1, y);
		model.addParent(x2, y);

		final IntervalFactor fy = new IntervalFactor(model.getDomain(y), Strides.EMPTY);
		fy.setLower(new double[]{.1, .3, .3, .1});
		fy.setUpper(new double[]{.2, .4, .4, .2});

		final IntervalFactor fx1 = new IntervalFactor(model.getDomain(x1), model.getDomain(y));
		fx1.setLower(new double[]{.600, .375}, 0);
		fx1.setLower(new double[]{.750, .225}, 1);
		fx1.setLower(new double[]{.850, .125}, 2);
		fx1.setLower(new double[]{.950, .025}, 3);

		fx1.setUpper(new double[]{.625, .400}, 0);
		fx1.setUpper(new double[]{.775, .250}, 1);
		fx1.setUpper(new double[]{.875, .150}, 2);
		fx1.setUpper(new double[]{.975, .050}, 3);

		final IntervalFactor fx2 = new IntervalFactor(model.getDomain(x2), model.getDomain(y));
		fx2.setLower(new double[]{.325, .650}, 0);
		fx2.setLower(new double[]{.600, .375}, 1);
		fx2.setLower(new double[]{.750, .225}, 2);
		fx2.setLower(new double[]{.850, .125}, 3);

		fx2.setUpper(new double[]{.350, .675}, 0);
		fx2.setUpper(new double[]{.625, .400}, 1);
		fx2.setUpper(new double[]{.775, .250}, 2);
		fx2.setUpper(new double[]{.875, .150}, 3);

		model.setFactor(y, fy);
		model.setFactor(x1, fx1);
		model.setFactor(x2, fx2);

		final TIntIntHashMap obs = new TIntIntHashMap();
		obs.put(x1, 0);
		obs.put(x2, 0);

		final MergeObserved mo = new MergeObserved();
		final DAGModel<IntervalFactor> merged = mo.execute(model, obs);

		System.out.println(merged);

		Assertions.assertEquals(2, merged.getVariables().length);
	}

	@Test
	void testMergeObservedMerge2of3() {
		DAGModel<IntervalFactor> model = new DAGModel<>();

		final int y = model.addVariable(4);
		final int x1 = model.addVariable(2);
		final int x2 = model.addVariable(2);
		final int x3 = model.addVariable(2);

		model.addParent(x1, y);
		model.addParent(x2, y);
		model.addParent(x3, y);

		final IntervalFactor fy = new IntervalFactor(model.getDomain(y), Strides.EMPTY);
		fy.setLower(new double[]{.1, .3, .3, .1});
		fy.setUpper(new double[]{.2, .4, .4, .2});

		final IntervalFactor fx1 = new IntervalFactor(model.getDomain(x1), model.getDomain(y));
		fx1.setLower(new double[]{.600, .375}, 0);
		fx1.setLower(new double[]{.750, .225}, 1);
		fx1.setLower(new double[]{.850, .125}, 2);
		fx1.setLower(new double[]{.950, .025}, 3);

		fx1.setUpper(new double[]{.625, .400}, 0);
		fx1.setUpper(new double[]{.775, .250}, 1);
		fx1.setUpper(new double[]{.875, .150}, 2);
		fx1.setUpper(new double[]{.975, .050}, 3);

		final IntervalFactor fx2 = new IntervalFactor(model.getDomain(x2), model.getDomain(y));
		fx2.setLower(new double[]{.325, .650}, 0);
		fx2.setLower(new double[]{.600, .375}, 1);
		fx2.setLower(new double[]{.750, .225}, 2);
		fx2.setLower(new double[]{.850, .125}, 3);

		fx2.setUpper(new double[]{.350, .675}, 0);
		fx2.setUpper(new double[]{.625, .400}, 1);
		fx2.setUpper(new double[]{.775, .250}, 2);
		fx2.setUpper(new double[]{.875, .150}, 3);

		final IntervalFactor fx3 = new IntervalFactor(model.getDomain(x3), model.getDomain(y));
		fx2.setLower(new double[]{.225, .750}, 0);
		fx2.setLower(new double[]{.325, .650}, 1);
		fx2.setLower(new double[]{.600, .375}, 2);
		fx2.setLower(new double[]{.750, .225}, 3);

		fx2.setUpper(new double[]{.250, .775}, 0);
		fx2.setUpper(new double[]{.350, .675}, 1);
		fx2.setUpper(new double[]{.625, .400}, 2);
		fx2.setUpper(new double[]{.775, .250}, 3);

		model.setFactor(y, fy);
		model.setFactor(x1, fx1);
		model.setFactor(x2, fx2);
		model.setFactor(x3, fx3);

		final TIntIntHashMap obs = new TIntIntHashMap();
		obs.put(x1, 0);
		obs.put(x2, 0);

		final MergeObserved mo = new MergeObserved();
		final DAGModel<IntervalFactor> merged = mo.execute(model, obs);

		System.out.println(merged);

		Assertions.assertEquals(3, merged.getVariables().length);
	}
}