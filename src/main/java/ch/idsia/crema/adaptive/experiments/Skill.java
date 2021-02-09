package ch.idsia.crema.adaptive.experiments;

import java.util.Objects;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 16:46
 */
public class Skill {

	/**
	 * Variable index in the model.
	 */
	public final int variable;

	/**
	 * @param variable variable index
	 */
	public Skill(int variable) {
		this.variable = variable;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Skill skill = (Skill) o;
		return variable == skill.variable;
	}

	@Override
	public int hashCode() {
		return Objects.hash(variable);
	}
}
