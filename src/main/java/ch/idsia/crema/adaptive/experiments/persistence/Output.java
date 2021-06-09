package ch.idsia.crema.adaptive.experiments.persistence;

import ch.idsia.crema.factor.GenericFactor;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 16:42
 */
public abstract class Output<F extends GenericFactor> {

	public TIntList skills = new TIntArrayList();
	public List<F> factors = new ArrayList<>();

	public abstract String serialize();

}
