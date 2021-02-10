package ch.idsia.crema.adaptive.experiments.persistence;

import ch.idsia.crema.factor.GenericFactor;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 16:42
 */
public abstract class Output<F extends GenericFactor> {

	protected final NumberFormat nf;

	public TIntList skills = new TIntArrayList();
	public List<F> factors = new ArrayList<>();

	public Output() {
		nf = NumberFormat.getNumberInstance(Locale.ROOT);
		nf.setMaximumFractionDigits(4);
		nf.setMinimumFractionDigits(4);
	}

	public abstract String serialize();

}
