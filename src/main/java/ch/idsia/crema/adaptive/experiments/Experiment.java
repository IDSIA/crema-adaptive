package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.agents.AgentStudent;
import ch.idsia.crema.adaptive.experiments.agents.AgentTeacher;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 08:45
 */
public class Experiment {

	private final AgentTeacher teacher;
	private final AgentStudent student;

	public Experiment(AgentTeacher teacher, AgentStudent student) {
		this.teacher = teacher;
		this.student = student;
	}

	public void run() throws Exception {
		int question = 0;

		while (!teacher.stop()) {
			Question q = teacher.next();

			System.out.println("Question: " + question);
			question++;

			if (q == null)
				break;

			final int x = student.answer(q);
			teacher.check(q, x);
		}
	}

}
