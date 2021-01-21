package ch.idsia.crema.adaptive;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Author:  Claudio "Dna" Bonesana
 * Project: Credo3
 * Date:    22.02.2017 15:15
 */
class QuestionSet {

    private static final String[] levels = {"A1", "A2", "B1", "B2"};
    private static final String[] skills = {"Horen", "Kommunikation", "Lesen", "Wortschatz"};

    /**
     * ID Skill -> ID Level -> Question
     */
    private Map<Integer, Map<Integer, List<Integer>>> questions = null;
    private int questionNum = 0;

    void loadKeyList() {
        questions = new HashMap<>();

        for (int s = 0; s < skills.length; s++) {
            questions.put(s, new HashMap<>());
            for (int l = 0; l < skills.length; l++) {
                questions.get(s).put(l, new ArrayList<>());
            }
        }

        try (Scanner scan = new Scanner(new File("adaptive/keys.txt"))) {
            while (scan.hasNext()) {
                String line = scan.nextLine();
                String[] tokens = line.split(" ");

                int id = Integer.parseInt(tokens[0]);
                int lvl = ArrayUtils.indexOf(levels, tokens[2]);
                int skill = ArrayUtils.indexOf(skills, tokens[3]);

                questions.get(skill).get(lvl).add(id);
                questionNum++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    List<Integer> getQuestions(int skill, int level) {
        if (questions == null) {
            loadKeyList();
        }
        return questions.get(skill).get(level);
    }

    boolean isEmpty() {
        return questionNum == 0;
    }

    public void revomeQuestion() {
        questionNum--;
    }
}
