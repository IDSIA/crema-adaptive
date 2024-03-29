package ch.idsia.crema.adaptive.experiments.persistence;

import ch.idsia.crema.adaptive.experiments.agents.Teacher;
import ch.idsia.crema.factor.GenericFactor;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 16:36
 */
public interface Persist<F extends GenericFactor> {

	/**
	 * Register the current state of a {@link Teacher}.
	 *
	 * @param teacher who keep the experiment
	 * @throws Exception if the inference cannot be done
	 */
	Output<F> register(Teacher<F> teacher) throws Exception;

}
