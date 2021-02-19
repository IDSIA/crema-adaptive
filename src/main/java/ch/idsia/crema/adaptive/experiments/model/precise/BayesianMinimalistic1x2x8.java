package ch.idsia.crema.adaptive.experiments.model.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Giorgia Adorni
 * Project: crema-adaptive
 * Date:    18.02.2021 16:00
 */
public class BayesianMinimalistic1x2x8 extends AbstractModelBuilder<BayesianFactor> {

    /**
     * A Bayesian model with one skill with two states and a given number of questions. All the questions have the same
     * CPT that is built from the given <code>p1</code> and <code>p0</code> parameters.
     *
     * @param nQuestions number of questions in total, all questions will have the same template (value: 1).
     */
    public BayesianMinimalistic1x2x8(int nQuestions) {
        model = new DAGModel<>();

        int s = model.addVariable(2);
        model.setFactor(s, new BayesianFactor(model.getDomain(s), new double[]{.5, .5}));

        skills.add(s);

        for (int i = 0; i < nQuestions; i++) {
            addQuestionNodeL1(model, s, 10 * s + 1);
            addQuestionNodeL2(model, s, 10 * s + 2);
            addQuestionNodeL3(model, s, 10 * s + 3);
            addQuestionNodeL4(model, s, 10 * s + 4);
            addQuestionNodeL5(model, s, 10 * s + 5);
            addQuestionNodeL6(model, s, 10 * s + 6);
            addQuestionNodeL7(model, s, 10 * s + 7);
            addQuestionNodeL8(model, s, 10 * s + 8);
        }
    }

    public void addQuestionNodeL1(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), new double[]{
                .9, .1,
                .1, .9
        });

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL2(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), new double[]{
                .85, .15,
                .15, .85
        });

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL3(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), new double[]{
                .8, .2,
                .2, .8
        });

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL4(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), new double[]{
                .75, .25,
                .25, .75
        });

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL5(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), new double[]{
                .7, .3,
                .3, .7
        });

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL6(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), new double[]{
                .65, .35,
                .35, .65
        });

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL7(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), new double[]{
                .6, .4,
                .4, .6
        });

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    public void addQuestionNodeL8(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), new double[]{
                .55, .45,
                .45, .55
        });

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }
}
