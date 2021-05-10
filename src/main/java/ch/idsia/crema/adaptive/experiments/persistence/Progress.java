package ch.idsia.crema.adaptive.experiments.persistence;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.Utils;
import ch.idsia.crema.factor.GenericFactor;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static ch.idsia.crema.adaptive.experiments.Utils.separator;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    10.05.2021 13:31
 */
public class Progress<F extends GenericFactor> {

	private final double score;
	private final Question question;
	private int answer;

	private Output<F> output;

	public Progress(double score, Question question) {
		this.score = score;
		this.question = question;
	}

	public void setAnswer(int answer) {
		this.answer = answer;
	}

	public void setOutput(Output<F> output) {
		this.output = output;
	}

	public Output<F> getOutput() {
		return output;
	}

	public String serialize(int id) {
		final NumberFormat nf = Utils.numberFormat();

		List<String> data = new ArrayList<>();
		data.add("" + id);
		data.add("" + question.skill);
		data.add("" + question.variable);
		data.add("" + question.template);
		data.add(nf.format(score));
		data.add("" + answer);
		data.add(output.serialize());

		return String.join(separator, data);
	}

}
