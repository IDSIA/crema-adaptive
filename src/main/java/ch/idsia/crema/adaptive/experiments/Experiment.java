package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.factor.GenericFactor;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    09.02.2021 08:45
 */
public class Experiment<F extends GenericFactor> {

	private final AgentTeacher<F> teacher;
	private final AgentStudent<F> student;

	public Experiment(AgentTeacher<F> teacher, AgentStudent<F> student) {
		this.teacher = teacher;
		this.student = student;
	}

	public void run() throws Exception {
		while (!teacher.stop()) {
			Question q = teacher.next();
			if (q == null)
				break;

			final int x = student.answer(q);
			teacher.answer(q, x);
		}

		System.out.println(teacher.getNumberQuestionsDone());
	}

}
