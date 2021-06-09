package ch.idsia.crema.adaptive.experiments.persistence;

import ch.idsia.crema.adaptive.experiments.Utils;
import ch.idsia.crema.factor.bayesian.BayesianFactor;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

import static ch.idsia.crema.adaptive.experiments.Utils.separator;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 16:26
 */
public class OutputBayesian extends Output<BayesianFactor> {

	@Override
	public String serialize() {
		final NumberFormat nf = Utils.numberFormat();
		return factors.stream()
				.map(BayesianFactor::getData)
				.flatMapToDouble(Arrays::stream)
				.mapToObj(nf::format)
				.collect(Collectors.joining(separator));
	}
}
