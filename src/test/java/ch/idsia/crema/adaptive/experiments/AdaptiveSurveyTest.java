package ch.idsia.crema.adaptive.experiments;

import ch.idsia.crema.adaptive.experiments.model.imprecise.Credal4x4x4;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.inference.approxlp2.ApproxLP2;
import ch.idsia.crema.model.graphical.DAGModel;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: crema-adaptive
 * Date:    18.02.2021 09:24
 */
class AdaptiveSurveyTest {

	static class Params {
		TIntIntMap obs = new TIntIntHashMap();
		int question;
		int skill;
		int state;

		@Override
		public String toString() {
			return "question=" + question +
					" skill=" + skill +
					" state=" + state +
					" obs=" + obs;
		}
	}

	@Test
	void testCPTs() {
		final DAGModel<IntervalFactor> model = new Credal4x4x4(5).getModel();

		for (int v : model.getVariables()) {
			final IntervalFactor f = model.getFactor(v);

			if (v < 1) {
				assertTrue(Arrays.stream(f.getLower()).sum() < 1.0);
				assertTrue(Arrays.stream(f.getUpper()).sum() > 1.0);
			} else if (v < 4) {
				for (int i = 0; i < 4; i++) {
					assertTrue(Arrays.stream(f.getLower(i)).sum() < 1.0);
					assertTrue(Arrays.stream(f.getUpper(i)).sum() > 1.0);
				}
			} else {
				for (int i = 0; i < 4; i++) {
					final double[] lower = f.getLower(i);
					final double[] upper = f.getUpper(i);
					assertEquals(1.0, lower[0] + upper[1], v + "=" + i);
					assertEquals(1.0, lower[1] + upper[0], v + "=" + i);
				}
			}
		}
	}

	@Test
	void testingNoFeasibleSolution() throws Exception {
		final ApproxLP2 approx = new ApproxLP2();
		final Credal4x4x4 builder = new Credal4x4x4(5);
		final DAGModel<IntervalFactor> model = builder.getModel();

//		final Map<Integer, Integer> questionToTemplate = builder.questions.stream()
//				.collect(Collectors.toMap(q -> q.variable, t -> t.template));

		final Pattern pattern = Pattern.compile("([a-z0-9]+=\\d+\\s*)");

		Files.readAllLines(Paths.get("NoFeasibleSolutions.txt")).stream()
				.filter(line -> line.startsWith("No Feasible Solution"))
				.map(line -> line.substring(line.indexOf(": ") + 2))
				.map(line -> {
					// question=8 skill=0 state=1 obs={30=1, 29=1, 28=0, 27=1, 26=0, 25=0, 24=0, 9=1, 8=1, 7=1, 6=0, 5=1, 4=1, 31=0}
					Params p = new Params();

					final Matcher matcher = pattern.matcher(line);
					int i = 0;
					while (matcher.find()) {
						final String group = matcher.group(i).trim();
						final String[] tokens = group.split("=");
						switch (tokens[0]) {
							case "question":
								p.question = Integer.parseInt(tokens[1]);
								break;
							case "skill":
								p.skill = Integer.parseInt(tokens[1]);
								break;
							case "state":
								p.state = Integer.parseInt(tokens[1]);
								break;
							default:
								p.obs.put(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
						}
					}

					return p;
				})
				.forEach(p -> {
					try {
						final IntervalFactor ignored = approx.query(model, p.skill, p.obs);
						System.out.println("VALID: " + p);
					} catch (Exception e) {
						System.err.println("ERROR: " + p);
					}
				});

//		final Map<Integer, List<Integer>> collect = IntStream.of(obs.keys()).boxed().collect(Collectors.groupingBy(questionToTemplate::get, Collectors.toList()));
//		collect.forEach((k, v) -> System.out.printf("%2d: %s%n", k, v));
	}
}