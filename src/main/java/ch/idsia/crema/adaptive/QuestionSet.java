package ch.idsia.crema.adaptive;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Author:  Giorgia Adorni
 * Date:    28.01.2020 14:00
 */
public class QuestionSet {

//    private static final String[] levels = {"A1", "A2", "B1", "B2"};
    private static final String[] skills = {"Horen", "Kommunikation", "Lesen", "Wortschatz"};
    private static final String[] difficulties = {"easy", "medium-easy", "medium-hard", "hard"};

    private Map<Integer, Map<Integer, List<Integer>>> questions = null;
    private int questionNum = 0;
    private int askedQuestion = 0;

    public void loadKeyList() {
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
                int difficulty = ArrayUtils.indexOf(difficulties, tokens[1]);
                int skill = ArrayUtils.indexOf(skills, tokens[2]);

                questions.get(skill).get(difficulty).add(id);
                questionNum++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getQuestions(int skill, int difficulty) {
        if (questions == null) {
            loadKeyList();
        }
        return questions.get(skill).get(difficulty);
    }

    int getQuestionNum() {
        return questionNum;
    }

    public boolean isEmpty() {
        return questionNum == 0;
    }

    public void removeQuestion() {
        questionNum--;
    }

    public int getAskedQuestion() {
        return askedQuestion;
    }

    public void setAskedQuestion(int askedQuestion) {
        this.askedQuestion = askedQuestion;
    }

    public void addAskedQuestion() {
        askedQuestion ++;
    }
}
