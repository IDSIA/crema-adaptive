import matplotlib.pyplot as plt
import numpy as np
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score

from src.analysis.multilabel_classification_metrics import brier_multicategory, hamming_distance, exact_match_ratio, \
    plot_confusion_matrix, class_metrics_barplot, metric_per_question
from src.analysis.utils import save_metrics, check_dir, save_visualisation

if __name__ == '__main__':

    tests = ['adaptive_entropy', 'non_adaptive_entropy']
    n_skills = 4
    n_difficulties = 4
    n_posteriors = n_skills * n_difficulties
    n_questions = 80

    dict_metrics = []
    avg_accuracy_per_question = []
    avg_brier_per_question = []

    fig, axes = plt.subplots(1, 2, sharey=True, figsize=(9, 4))
    fig.suptitle('Confusion matrix', fontweight='bold')

    for test in tests:
        initial_profiles_file = "../../output/" + test + "/initial_profiles.txt"
        final_posterior_file = "../../output/" + test + "/final_posterior.txt"
        posteriors_file = "../../output/" + test + "/posteriors.txt"

        final_posterior = np.loadtxt(final_posterior_file,
                                     delimiter=', ',
                                     usecols=np.arange(1, n_posteriors + 1))
        final_posterior = np.reshape(final_posterior, (-1, n_skills, n_difficulties))

        predicted_classes = np.argmax(final_posterior, axis=2)

        observed_classes = np.loadtxt(initial_profiles_file,
                                      delimiter=', ',
                                      dtype=np.int32,
                                      usecols=np.arange(1, n_skills + 1))

        all_posteriors = np.loadtxt(posteriors_file,
                                    delimiter=', ',
                                    usecols=np.arange(1, (n_posteriors * n_questions) + 1))
        all_posteriors = np.reshape(all_posteriors, (-1, n_questions, n_skills, n_difficulties))
        all_predicted_classes = np.argmax(all_posteriors, axis=3)

        # Analysis
        hamming_losses = []
        hamming_distances = []
        accuracy_scores = []
        precision_scores = []
        recall_scores = []
        f1_scores = []
        all_accuracy_scores = []
        all_brier_scores = []

        brier_scores = brier_multicategory(observed_classes, final_posterior)

        for i in range(len(predicted_classes)):
            hamming_loss, hamming_dist = hamming_distance(observed_classes[i], predicted_classes[i])
            hamming_losses.append(hamming_loss)
            hamming_distances.append(hamming_dist)

            accuracy = accuracy_score(observed_classes[i], predicted_classes[i])

            accuracy_scores.append(accuracy)

        for i in range(len(predicted_classes)):
            all_accuracies = []

            for j in range(n_questions):
                all_accuracy = accuracy_score(observed_classes[i], all_predicted_classes[i][j])
                all_accuracies.append(all_accuracy)

            all_accuracy_scores.append(all_accuracies)

        for j in range(n_questions):
            all_briers = brier_multicategory(observed_classes, all_posteriors[:, j, ...])
            all_brier_scores.append(all_briers)

        hamming_losses = np.array(hamming_losses)
        hamming_distances = np.array(hamming_distances)
        accuracy_scores = np.array(accuracy_scores)
        all_accuracy_scores = np.array(all_accuracy_scores)

        avg_all_accuracy_scores = np.mean(all_accuracy_scores, axis=0)
        avg_accuracy_per_question.append(avg_all_accuracy_scores)

        avg_all_brier_scores = np.mean(np.array(all_brier_scores), axis=1)
        avg_brier_per_question.append(avg_all_brier_scores)

        out_dir = "output/" + test + "/"
        check_dir(out_dir)

        out_file = out_dir + 'metrics.csv'
        metrics = np.column_stack([brier_scores, hamming_losses, hamming_distances, accuracy_scores])
        header = 'brier_scores, hamming_losses, hamming_distances, accuracy_scores'
        save_metrics(out_file, metrics, header)

        # Save averaged metrics
        avg_brier_scores = np.mean(brier_scores)
        avg_hamming_losses = np.mean(hamming_losses)
        avg_hamming_distances = np.mean(hamming_distances)
        avg_accuracy_scores = np.mean(accuracy_scores)

        EMR = exact_match_ratio(observed_classes, predicted_classes)

        predictions = np.array(predicted_classes).flat
        observations = np.array(observed_classes).flat

        macroavg_precision = precision_score(observations, predictions, labels=np.arange(4), average='macro')
        macroavg_recall = recall_score(observations, predictions, labels=np.arange(4), average='macro')
        macroavg_f1 = f1_score(observations, predictions, labels=np.arange(4), average='macro')

        # Save avg metrics
        out_file = out_dir + 'avg_metrics.csv'
        header = 'brier_scores, hamming_losses, hamming_distances, accuracy_scores, exact_match_ratio, macroavg_precision, macroavg_recall, macroavg_f1_score'
        metrics = np.column_stack([avg_brier_scores, avg_hamming_losses, avg_hamming_distances,
                                   avg_accuracy_scores, EMR, macroavg_precision, macroavg_recall, macroavg_f1])
        # metrics = np.c_[avg_brier_scores, avg_hamming_losses, avg_hamming_distances,
        #                 avg_accuracy_scores, EMR, macroavg_precision, macroavg_recall, macroavg_f1]
        save_metrics(out_file, metrics, header)

        # Get and plot the confusion matrix
        if test == 'adaptive_entropy':
            ax = axes.flat[0]
        else:
            ax = axes.flat[1]
        cm, img = plot_confusion_matrix(ax, observations, predictions, labels=np.arange(4), title=test)

        # Compute metrics for each class:
        cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]  # normalize the diagonal entries

        class_accuracy = cm.diagonal()  # the diagonal entries are the accuracies of each class
        class_precision = precision_score(observations, predictions, labels=np.arange(4), average=None)
        class_recall = recall_score(observations, predictions, labels=np.arange(4), average=None)
        class_f1 = f1_score(observations, predictions, labels=np.arange(4), average=None)

        out_file = out_dir + 'skills_avg_metrics.csv'
        header = 'skill_0_accuracy, skill_1_accuracy, skill_2_accuracy, skill_3_accuracy, ' \
                 'skill_0_precision, skill_1_precision, skill_2_precision, skill_3_precision, ' \
                 'skill_0_recall, skill_1_recall, skill_2_recall, skill_3_recall, ' \
                 'skill_0_f1_score, skill_1_f1_score, skill_2_f1_score, skill_3_f1_score'
        metrics = np.column_stack([class_accuracy[::, None].T, class_precision[::, None].T,
                                   class_recall[::, None].T, class_f1[::, None].T])
        save_metrics(out_file, metrics, header)

        dict_metrics.append({
            'accuracy/X0': class_accuracy[0],
            'accuracy/X1': class_accuracy[1],
            'accuracy/X2': class_accuracy[2],
            'accuracy/X3': class_accuracy[3],
            'precision/X0': class_precision[0],
            'precision/X1': class_precision[1],
            'precision/X2': class_precision[2],
            'precision/X3': class_precision[3],
            'recall/X0': class_recall[0],
            'recall/X1': class_recall[1],
            'recall/X2': class_recall[2],
            'recall/X3': class_recall[3],
            'f1_score/X0': class_f1[0],
            'f1_score/X1': class_f1[1],
            'f1_score/X2': class_f1[2],
            'f1_score/X3': class_f1[3]
        })

    # for ax in fig.axes:
    #     plt.sca(ax)
    #     plt.xticks(rotation=45)
    fig.colorbar(img, ax=axes.ravel().tolist())
    # fig.tight_layout()
    save_visualisation('confusion_matrix', 'output/')

    class_metrics_barplot(tests, dict_metrics)

    save_visualisation('class_metrics', 'output/')

    metric_per_question(avg_accuracy_per_question, 'Accuracy', tests, [(0, 10), (0, -20)])
    save_visualisation('accuracy_per_question', 'output/')

    metric_per_question(avg_brier_per_question, 'Brier score', tests, [(0, -20), (0, 10)])
    save_visualisation('brier_per_question', 'output/')
