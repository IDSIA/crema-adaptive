package ch.idsia.crema.adaptive.experiments.persistence;

import ch.idsia.crema.adaptive.experiments.Utils;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import org.apache.commons.lang3.ArrayUtils;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

import static ch.idsia.crema.adaptive.experiments.Utils.separator;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 16:26
 */
public class OutputCredal extends Output<IntervalFactor> {

	@Override
	public String serialize() {
		final NumberFormat nf = Utils.numberFormat();
		return factors.stream()
				.map(x -> ArrayUtils.addAll(x.getLower(), x.getUpper()))
				.flatMapToDouble(Arrays::stream)
				.mapToObj(nf::format)
				.collect(Collectors.joining(separator));
	}
}
