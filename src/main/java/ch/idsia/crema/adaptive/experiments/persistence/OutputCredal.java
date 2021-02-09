package ch.idsia.crema.adaptive.experiments.persistence;

import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 16:26
 */
public class OutputCredal extends Output<IntervalFactor> {

	@Override
	public String serialize() {
		return factors.stream()
				.map(x -> ArrayUtils.addAll(x.getLower(), x.getUpper()))
				.flatMapToDouble(Arrays::stream)
				.mapToObj(nf::format)
				.collect(Collectors.joining(","));
	}
}
