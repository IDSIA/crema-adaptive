package ch.idsia.crema.adaptive.experiments;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    08.02.2021 14:57
 */
public class Question {

	public final int id;
	public final int skill;
	public final int template; // map question to its cpt, 0 means "no template"
	public final int variable;

	public Question(int id, int skill, int variable, int template) {
		this.id = id;
		this.skill = skill;
		this.variable = variable;
		this.template = template;
	}

	public Question(int id, int skill, int variable) {
		this.id = id;
		this.skill = skill;
		this.variable = variable;
		this.template = 0;
	}

	@Override
	public String toString() {
		return "Question{" +
				"id=" + id +
				", skill=" + skill +
				", template=" + template +
				", variable=" + variable +
				'}';
	}
}
