package ch.idsia.crema.adaptive.experiments;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 14:57
 */
public class Question {

	/**
	 * Variable index in the model.
	 */
	public final int skill;
	/**
	 * Skill variable (parent node) in the model.
	 */
	public final int variable;
	/**
	 * Map question to its cpt, 0 means "no template".
	 */
	public final int template;

	/**
	 * @param skill    variable index
	 * @param variable skill variable
	 * @param template template index value
	 */
	public Question(int skill, int variable, int template) {
		this.skill = skill;
		this.variable = variable;
		this.template = template;
	}

	/**
	 * @param skill    variable index
	 * @param variable skill variable
	 */
	public Question(int skill, int variable) {
		this.skill = skill;
		this.variable = variable;
		this.template = 0;
	}

	@Override
	public String toString() {
		return "Question{" +
				"skill=" + skill +
				", template=" + template +
				", variable=" + variable +
				'}';
	}
}
