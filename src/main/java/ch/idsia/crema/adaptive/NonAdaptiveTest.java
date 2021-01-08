package ch.idsia.crema.adaptive;

import ch.idsia.crema.factor.credal.vertex.generator.CNGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: Credo3
 * Date:    21.02.2017 16:22
 */
public class NonAdaptiveTest {

	private static final int THREAD_POOL_SIZE = 4;

	private static final String[] csvs = {
			"adaptive/Hören2015-16.csv",
			"adaptive/Kommunikation2015-16.csv",
			"adaptive/Lesen2015-16.csv",
			"adaptive/Wortschatz und Strukturen2015-16.csv"
	};

	private static final int skillNumber = 4;
	private static final int levelNumber = 4;


	/**
	 * First id, inclusive.
	 */
	private static final int minStudent = 0;
	/**
	 * Last id, exclusive.
	 */
	private static final int maxStudent = 451;
	// private final Random random;
	private final static String credalFileName = "adaptive/cnParameters.txt";
	private final static String bayesFileName = "adaptive/cnParametersBayes.txt";
	private final static String trueLevelsFilename = "adaptive/students_levels_percentage.csv";
	private final static String trueLevelsFilename2 = "adaptive/students_levels_Bayesian.csv";
	static int[][] trueLevels;
	static int[][] trueLevels2;
	static int[][] bayesLevels;
	static boolean[][][] credalLevels;
	static int[] wrongStudents = {71, 172, 240, 404};
	// object variables ------------------------------------------------------------------------------------------------
	private final int student;
	private final double[][] wrongQuestion = new double[skillNumber][levelNumber]; // {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}
	private final double[][] rightQuestion = new double[skillNumber][levelNumber]; // {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}};
	private final AnswerSet[] qs = new AnswerSet[skillNumber];
	private final AdaptiveTests_old at;
	private final QuestionSet q;


	/**
	 * Create a survey test for a single student. Each students will have its lists of questions, its personal test,
	 * and its answer sheet.
	 *
	 * @param student refernece id of the students
	 */
	private NonAdaptiveTest(int student) {
		this.student = student;

		at = new AdaptiveTests_old();
		q = new QuestionSet();
		q.loadKeyList();

		for (int i = 0; i < qs.length; i++) {
			qs[i] = new AnswerSet().load(csvs[i]);
		}
	}

//    private static int[][] removeWrongStudents(int[][] trueLevels2) {
//    	List<Integer> remove = new ArrayList<Integer>();
//    	for (int e:wrongStudents) {
//    		if (e<maxStudent && e>=minStudent) remove.add(e);
//    	}
//		int[][] output = new int[trueLevels2.length-remove.size()][skillNumber];
//		int count=0;
//		for (int r=0;r<trueLevels2.length;r++){
//			if (!remove.contains(r)) {
//				output[count] = trueLevels2[r];
//				count+=1;
//			}
//		}
//		return output;
//	}
//    
//    private static boolean[][][] removeWrongStudents(boolean[][][] trueLevels2) {
//    	List<Integer> remove = new ArrayList<Integer>();
//    	for (int e:wrongStudents) {
//    		if (e<maxStudent && e>=minStudent) remove.add(e);
//    	}
//		boolean[][][] output = new boolean[trueLevels2.length-remove.size()][skillNumber][levelNumber];
//		int count=0;
//		for (int r=0;r<trueLevels2.length;r++){
//			if (!remove.contains(r)) {
//				output[count] = trueLevels2[r];
//				count+=1;
//			}
//		}
//		return output;
//	}

	public static void main(String[] args) throws Exception {
		ExecutorService es = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

		final Path path = Paths.get("output_nonAdaptive_" + new SimpleDateFormat("yyyy.MM.dd_HH-mm-ss").format(new Date()) + " .txt");

		String current = new java.io.File(".").getCanonicalPath();
		System.out.println("Current dir:" + current);
		String currentDir = System.getProperty("user.dir");
		System.out.println("Current dir using System:" + currentDir);

		List<Integer> remove = new ArrayList<>();
		for (int e : wrongStudents) {
			if (e < maxStudent && e >= minStudent) remove.add(e);
		}
		trueLevels = new int[maxStudent - minStudent - remove.size()][skillNumber];
		trueLevels2 = new int[maxStudent - minStudent - remove.size()][skillNumber];
		bayesLevels = new int[maxStudent - minStudent - remove.size()][skillNumber];
		credalLevels = new boolean[maxStudent - minStudent - remove.size()][skillNumber][levelNumber];
		readCsvLevels(remove);
		readCsvLevels2(remove);
//      AnswerSet qs = new AnswerSet().load(new FileInputStream("adaptive/Lesen2015-16.csv"));

//      final Path path = Paths.get(
//      System.getProperty("user.home") + "adaptive/" +
//              new SimpleDateFormat("yyyy.MM.dd_HH-mm-ss")
//                      .format(new Date()) + " .txt");

		AdaptiveFileTools.writeBNFile(credalFileName);

//		int a[][] = {{1,2,1,2},{1,1,0,0}};
//		int b[][] = {{1,2,1,2},{1,1,0,0}};
//		boolean[][][] c = {{{true,true,false,false},{true,true,false,true},{false, true,true, true}, {false,false,true,true}},
//				{{true,true,true,false},{true,true,false,true},{false, true,true, true}, {false,false,true,true}}};
//		CredalClassifiersEvaluation myTest = new CredalClassifiersEvaluation();
//		myTest.analyzer(a, c, b);

		// for each student
		for (int student = minStudent; student < maxStudent; student++) {

			final int studentId = student;
			es.submit(() -> {

				System.out.println("Start for student " + studentId);

				NonAdaptiveTest aslat = new NonAdaptiveTest(studentId);
				if (!remove.contains(studentId)) {
					aslat.test();

					saveToFile(aslat, path, studentId);
				}
			});
		}

		CredalClassifiersEvaluation myTest = new CredalClassifiersEvaluation();
		// Analysis of the results
		myTest.analyzer(trueLevels, credalLevels, bayesLevels);
		System.out.println("Empirical Bayes");
		myTest.analyzer(trueLevels2, credalLevels, bayesLevels);
		//es.shutdown();
	}

	private static void readCsvLevels(List<Integer> remove) {

//		for(int i=0;i<trueLevels.length;i++)
//			for(int j=0;j<trueLevels[0].length;j++)
//				trueLevels[i][j]=NaN;
		try (Scanner scan = new Scanner(new File(trueLevelsFilename))) {
			scan.nextLine(); // rem,ove first line

			int col;
			int scount = 0;
			//System.out.println(first);
			for (int rowNumber = 0; rowNumber < minStudent; rowNumber++) scan.nextLine();
			for (int rowNumber = minStudent; rowNumber < maxStudent; rowNumber++) {
				String line = scan.nextLine();
				if (!remove.contains(rowNumber)) {
					col = 0;
					for (String element : line.split(" *,")) {
						trueLevels[scount][col] = Integer.parseInt(element);
						col++;
					}
					scount += 1;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void readCsvLevels2(List<Integer> remove) {
//		for(int i=0;i<trueLevels.length;i++)
//			for(int j=0;j<trueLevels[0].length;j++)
//				trueLevels[i][j]=NaN;
		try (Scanner scan = new Scanner(new File(trueLevelsFilename2))) {
			int col;
			int scount = 0;
			scan.nextLine(); // remove first line
			for (int rowNumber = 0; rowNumber < minStudent; rowNumber++) scan.nextLine();
			for (int rowNumber = minStudent; rowNumber < maxStudent; rowNumber++) {
				String line = scan.nextLine();
				if (!remove.contains(rowNumber)) {
					col = 0;
					for (String element : line.split(" *,")) {
						trueLevels2[scount][col] = Integer.parseInt(element);
						col++;
					}
					scount += 1;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static synchronized void saveToFile(NonAdaptiveTest aslat, Path path, int student) {
		try (BufferedWriter bw = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
			StringBuilder out = new StringBuilder();
			out.append(String.format("%3d ", aslat.student));

			// TODO: the level of the student is given by the interval dominance
			CNGenerator reachRobot = new CNGenerator();
			for (int s = 0; s < skillNumber; s++) {
				double[][] resultsCN = aslat.at.germanTest(credalFileName, s, aslat.rightQuestion, aslat.wrongQuestion);
				double[][] resultsBN = aslat.at.germanTest(bayesFileName, s, aslat.rightQuestion, aslat.wrongQuestion);

				double[][] resultsCNreach;
//     			resultsCNreach = resultsCN;
				resultsCNreach = reachRobot.makeReachable(resultsCN);

				double mpBayes = 0;
				double mpCredal = 0;
				int levCredal = -1;
				for (int lev = 0; lev < levelNumber; lev++) {
					if (resultsBN[0][lev] > mpBayes) {
						mpBayes = resultsBN[0][lev];
						bayesLevels[student][s] = lev;
					}
					if (resultsCNreach[0][lev] > mpCredal) {
						mpCredal = resultsCNreach[0][lev];
						levCredal = lev;
					}
				}

//     			double pSum = 0;
//     			for  (int lev=0; (lev<levelNumber); lev++) {
//     				if(!(lev==levCredal)) pSum+=resultsCN[1][lev];
//     			}
//     			if ((pSum+mpCredal)<1) mpCredal = 1-pSum;

				credalLevels[student][s][levCredal] = true;
				for (int lev = 0; lev < levelNumber; lev++) {
					if (resultsCNreach[1][lev] > mpCredal) {
						credalLevels[student][s][lev] = true;
					}
				}

				out.append(s).append(" - ")
						.append("credal: ").append(Arrays.toString(credalLevels[student][s])).append(", ")
						.append("bayes: ").append(Arrays.toString(bayesLevels[student])).append(", posteriors: ")
						.append(Arrays.toString(resultsCN[0])).append(", ")
						.append(Arrays.toString(resultsCN[1])).append(", ")
						.append(Arrays.toString(resultsBN[0])).append(", ");

				DecimalFormat df = new DecimalFormat("#.##");
				System.out.print("Skill: " + s + " Prob: "
						+ df.format(resultsCN[0][0]) + " " + df.format(resultsCN[1][0]) + " - " + df.format(resultsBN[0][0]) + "; "
						+ df.format(resultsCN[0][1]) + " " + df.format(resultsCN[1][1]) + " - " + df.format(resultsBN[0][1]) + "; "
						+ df.format(resultsCN[0][2]) + " " + df.format(resultsCN[1][2]) + " - " + df.format(resultsBN[0][2]) + "; "
						+ df.format(resultsCN[0][3]) + " " + df.format(resultsCN[1][3]) + " - " + df.format(resultsBN[0][3]) + "; \n");
				System.out.print("Skill: " + s + " Level: credal: "
						+ credalLevels[student][s][0] + " " + credalLevels[student][s][1] + " " + credalLevels[student][s][2] + " " + credalLevels[student][s][3] + "; "
						+ "bayes:" + bayesLevels[student][s] + "; \n");
			}
			out.append("\n");

			bw.write(out.toString());
		} catch (IOException ignored) {
		}
	}

	/**
	 * Perform the non adaptive test with the initialized data.
	 */
	private void test() {

		for (int s = 0; s < skillNumber; s++) {
			for (int l = 0; l < levelNumber; l++) {
				List<Integer> availableQuestions = q.getQuestions(s, l);
				for (int question : availableQuestions) {
					int answer = qs[s].getAnswer(student, question);
					rightQuestion[s][l] += answer;
					wrongQuestion[s][l] += 1 - answer;
				}
			}
		}

		StringBuilder rightS = new StringBuilder("right = {");
		StringBuilder wrongS = new StringBuilder("wrong = ");
		for (int s = 0; s < skillNumber; s++) {
			rightS
					.append("{")
					.append(rightQuestion[s][0]).append(", ")
					.append(rightQuestion[s][1]).append(", ")
					.append(rightQuestion[s][2]).append(", ")
					.append(rightQuestion[s][3])
					.append("},");
			wrongS
					.append("{")
					.append(wrongQuestion[s][0]).append(", ")
					.append(wrongQuestion[s][1]).append(", ")
					.append(wrongQuestion[s][2]).append(", ")
					.append(wrongQuestion[s][3])
					.append("},");

//            resultsCN = at.germanTest(credalFileName,s, rightQuestion, wrongQuestion);
//            resultsBN = at.germanTest(bayesFileName,s, rightQuestion, wrongQuestion);
//			
//            DecimalFormat df = new DecimalFormat("#.##");
//            System.out.print("Skill: " +s + " Prob: " 
//            		+ df.format(resultsCN[0][0])+" "+ df.format(resultsCN[1][0]) +" - "+ df.format(resultsBN[0][0]) +"; "
//            		+ df.format(resultsCN[0][1])+" "+ df.format(resultsCN[1][1])+" - "+ df.format(resultsBN[0][1]) +"; "
//            		+ df.format(resultsCN[0][2])+" "+ df.format(resultsCN[1][2])+" - "+ df.format(resultsBN[0][2]) +"; "
//            		+ df.format(resultsCN[0][3])+" "+ df.format(resultsCN[1][3])+" - "+ df.format(resultsBN[0][3]) +"; \n");
		}
		System.out.println(rightS.substring(0, rightS.length() - 1) + "}");
		System.out.println(wrongS.substring(0, wrongS.length() - 1) + "}");
	}

}
