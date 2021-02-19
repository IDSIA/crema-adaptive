package ch.idsia.crema.adaptive.experiments.model.precise;

import ch.idsia.crema.adaptive.experiments.Question;
import ch.idsia.crema.adaptive.experiments.model.AbstractModelBuilder;
import ch.idsia.crema.factor.bayesian.BayesianFactor;
import ch.idsia.crema.model.graphical.DAGModel;

/**
 * Author:  Giorgia Adorni
 * Project: crema-adaptive
 * Date:    11.02.2021 17:00
 */
public class Bayesian4x4x4 extends AbstractModelBuilder<BayesianFactor> {

    /**
     * Build a Bayesian model where we have 4 skills with 4 states each.
     * Each skill has 4 templates of questions, each template has nQuestions
     * questions, and each question has 2 states.
     * All the questions in a template have the same CPT.
     *
     * @param nQuestions number of questions in total, all the questions in a
     *                   template have the same CPT.
     */
    public Bayesian4x4x4(int nQuestions) {

        model = new DAGModel<>();

        // skill-chain
        // S0 -> S1 -> S2 -> S3
        //  v     v     v     v
        // Q0    Q1    Q2    Q3
        int S0 = addSkillNode(model);
        int S1 = addSkillNode(model, S0);
        int S2 = addSkillNode(model, S1);
        int S3 = addSkillNode(model, S2);

        // for each skill...
        for (int s = S0; s <= S3; s++) {
            // ...add question nodes
            for (int i = 0; i < nQuestions; i++) {
                addQuestionNodeEasy(model, s, 10 * s + 1);
                addQuestionNodeMediumEasy(model, s, 10 * s + 2);
                addQuestionNodeMediumHard(model, s, 10 * s + 3);
                addQuestionNodeHard(model, s, 10 * s + 4);
            }
        }

    }

    /**
     * Add a skill node without parents.
     *
     * @param model add to this model
     * @return the new variable added
     */
    int addSkillNode(DAGModel<BayesianFactor> model) {
        int s = model.addVariable(4);

        model.setFactor(s, new BayesianFactor(model.getDomain(s), new double[]{.15, .35, .35, .15}));

        skills.add(s);

        return s;
    }

    /**
     * Add a skill node a single parent.
     *
     * @param model  add to this model
     * @param parent parent node
     * @return the new variable added
     */
    int addSkillNode(DAGModel<BayesianFactor> model, int parent) {
        int s = model.addVariable(4);
        model.addParent(s, parent);

        final BayesianFactor bF = new BayesianFactor(model.getDomain(s, parent),
                new double[]{.40, .30, .20, .10,   // P(S1|S0=0)
                        .25, .35, .25, .15,   // P(S1|S0=1)
                        .15, .25, .35, .25,   // P(S1|S0=2)
                        .10, .20, .30, .40}); // P(S1|S0=3)

        model.setFactor(s, bF);

        skills.add(s);

        return s;
    }

    /**
     * Add a question node for the easy difficulty.
     *
     * @param model  add to this model
     * @param parent skill parent node
     */
    public void addQuestionNodeEasy(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);
        
        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), new double[]{
                .6125, .7625, .8625, .9625,
                .3875, .2375, .1375, .0375
        });
        
        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    /**
     * Add a question node for the medium-easy difficulty.
     *
     * @param model  add to this model
     * @param parent skill parent node
     */
    public void addQuestionNodeMediumEasy(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), new double[]{
                .3375, .6125, .7625, .8625,
                .6625, .3875, .2375, .1375
        });

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    /**
     * Add a question node for the medium-hard difficulty.
     *
     * @param model  add to this model
     * @param parent skill parent node
     */
    public void addQuestionNodeMediumHard(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), new double[]{
                .2375, .3375, .6125, .7625,
                .7625, .6625, .3875, .2375
        });

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }

    /**
     * Add a question node for the hard difficulty.
     *
     * @param model  add to this model
     * @param parent skill parent node
     */
    public void addQuestionNodeHard(DAGModel<BayesianFactor> model, int parent, int template) {
        final int q = model.addVariable(2);
        model.addParent(q, parent);

        final BayesianFactor fQ = new BayesianFactor(model.getDomain(parent, q), new double[]{
                .1875, .2375, .3375, .6125,
                .8125, .7625, .6625, .3875
        });

        model.setFactor(q, fQ);

        questions.add(new Question(parent, q, template));
    }
}
