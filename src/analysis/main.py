import numpy as np
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score

from src.analysis.multilabel_classification_metrics import brier_multicategory, hamming_distance, exact_match_ratio, \
    plot_confusion_matrix
from src.analysis.parser import parse_predicted_probabilities, parse_profiles
from src.analysis.utils import save_metrics, check_dir

if __name__ == '__main__':

    simulations = np.arange(4)

    global_brier_scores = []
    global_hamming_losses = []
    global_hamming_distances = []
    global_accuracy_scores = []
    global_EMR = []

    predictions = []
    observations = []

    for sim in simulations:

        in_file = open("../../output/sim_" + str(sim) + "/initial_profiles.txt", "r")
        out_file = open("../../output/sim_" + str(sim) + "/predicted_profiles.txt", "r")

        predicted_probabilities = parse_predicted_probabilities(out_file)
        predicted_classes = np.argmax(predicted_probabilities, axis=1)

        observed_classes = parse_profiles(in_file)

        predictions.append(predicted_classes)
        observations.append(observed_classes)

        # Analysis
        brier_scores = []
        hamming_losses = []
        hamming_distances = []
        accuracy_scores = []
        precision_scores = []
        recall_scores = []
        f1_scores = []

        for i in range(len(predicted_classes)):
            brier_score = brier_multicategory(observed_classes[i], predicted_probabilities[i])
            brier_scores.append(brier_score)

            hamming_loss, hamming_dist = hamming_distance(observed_classes[i], predicted_classes[i])
            hamming_losses.append(hamming_loss)
            hamming_distances.append(hamming_dist)

            accuracy = accuracy_score(observed_classes[i], predicted_classes[i])

            accuracy_scores.append(accuracy)

        brier_scores = np.array(brier_scores)
        hamming_losses = np.array(hamming_losses)
        hamming_distances = np.array(hamming_distances)
        accuracy_scores = np.array(accuracy_scores)

        global_brier_scores.append(brier_scores)
        global_hamming_losses.append(hamming_losses)
        global_hamming_distances.append(hamming_distances)
        global_accuracy_scores.append(accuracy_scores)

        out_dir = "output/sim_" + str(sim) + "/"
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
        global_EMR.append(EMR)

        out_file = out_dir + 'avg_metrics.csv'
        header = 'brier_scores, hamming_losses, hamming_distances, accuracy_scores, exact_match_ratio'
        save_metrics(out_file,
                     np.c_[avg_brier_scores, avg_hamming_losses, avg_hamming_distances, avg_accuracy_scores, EMR],
                     header)

    global_brier_scores = np.mean(np.array(global_brier_scores), axis=0)
    global_hamming_losses = np.mean(np.array(global_hamming_losses), axis=0)
    global_hamming_distances = np.mean(np.array(global_hamming_distances), axis=0)
    global_accuracy_scores = np.mean(np.array(global_accuracy_scores), axis=0)

    out_file = 'output/metrics.csv'

    metrics = np.column_stack(
        [global_brier_scores, global_hamming_losses, global_hamming_distances, global_accuracy_scores])
    header = 'global_brier_scores, global_hamming_losses, global_hamming_distances, global_accuracy_scores'
    save_metrics(out_file, metrics, header)

    avg_global_brier_scores = np.mean(global_brier_scores)
    avg_global_hamming_losses = np.mean(global_hamming_losses)
    avg_global_hamming_distances = np.mean(global_hamming_distances)
    avg_global_accuracy_scores = np.mean(global_accuracy_scores)
    avg_global_EMR = np.mean(np.array(global_EMR))

    predictions = np.array(predictions).flat
    observations = np.array(observations).flat

    macroavg_precision = precision_score(observations, predictions, labels=np.arange(4), average='macro')
    class_precision = precision_score(observations, predictions, labels=np.arange(4), average=None)

    macroavg_recall = recall_score(observations, predictions, labels=np.arange(4), average='macro')
    class_recall = recall_score(observations, predictions, labels=np.arange(4), average=None)

    macroavg_f1 = f1_score(observations, predictions, labels=np.arange(4), average='macro')
    class_f1 = f1_score(observations, predictions, labels=np.arange(4), average=None)

    out_file = 'output/avg_metrics.csv'

    metrics = np.column_stack([avg_global_brier_scores, avg_global_hamming_losses, avg_global_hamming_distances,
                               avg_global_accuracy_scores, avg_global_EMR, macroavg_precision, macroavg_recall,
                               macroavg_f1, class_precision[::, None].T, class_recall[::, None].T,
                               class_f1[::, None].T])

    header = 'avg_global_brier_scores, avg_global_hamming_losses, avg_global_hamming_distances, ' \
             'avg_global_accuracy_scores, avg_global_EMR, macroavg_precision, macroavg_recall, macroavg_f1_score, ' \
             'skill_0_precision, skill_1_precision, skill_2_precision, skill_3_precision, ' \
             'skill_0_recall, skill_1_recall, skill_2_recall, skill_3_recall,' \
             'skill_0_f1_score, skill_1_f1_score, skill_2_f1_score, skill_3_f1_score'
    save_metrics(out_file, metrics, header)

    plot_confusion_matrix(observations, predictions, labels=np.arange(4))
