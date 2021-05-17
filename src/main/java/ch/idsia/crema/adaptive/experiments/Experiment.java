package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.agents.AgentStudent;
import ch.idsia.crema.adaptive.experiments.agents.AgentTeacher;

import static ch.idsia.crema.adaptive.experiments.Utils.separator;

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

	public String[] run() {
		int questions = 0;

		try {
			while (!teacher.stop()) {
				Question q = teacher.next();
				questions++;

				if (q == null)
					break;

				final int x = student.answer(q);
				teacher.check(q, x);

				System.out.printf("Student:  %3d ", student.getId());
				System.out.printf("Question: n=%2d v=%2d t=%2d s=%2d a=%d%n", questions, q.variable, q.template, q.skill, x);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String posteriors = student.getId() + separator + teacher.getResults();
		String answers = student.getAnswers();
		String profiles = student.getProfiles();
		String progress = String.join("\n", teacher.getProgress(student.getId()));

		return new String[]{posteriors, answers, profiles, progress};
	}

}
