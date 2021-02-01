import itertools

import matplotlib.pyplot as plt
import numpy as np
from scipy.spatial.distance import hamming
from sklearn.metrics import confusion_matrix

from src.analysis.utils import save_visualisation, one_hot_encode


def brier_multicategory(targets, probs):
    """
    (reference: https://en.wikipedia.org/wiki/Brier_score)
    :param targets:
    :param probs:
    :return brier_score:
    """
    n_classes = targets.shape[1]
    one_hot_targets = one_hot_encode(targets, n_classes)

    brier_score = np.mean(np.sum((probs - one_hot_targets) ** 2, axis=2), axis=1)/2
    return brier_score


def hamming_distance(observations, predictions):
    """

    :param observations:
    :param predictions:
    :return hamming_loss, hamming_distance:
    """
    hamming_loss = hamming(observations, predictions)
    hamming_distance = hamming_loss * len(observations)

    return hamming_loss, hamming_distance


def precision(observations, predictions):
    """

    :param observations:
    :param predictions:
    :return precision:
    """
    temp = 0
    for i in range(observations.shape[0]):
        temp += sum(np.logical_and(observations[i], predictions[i])) / sum(observations[i])

    precision = temp / observations.shape[0]
    return precision


def exact_match_ratio(observations, predictions):
    """

    :param observations:
    :param predictions:
    :return exact match ratio:
    """
    return np.all(predictions == observations, axis=1).mean()


def plot_confusion_matrix(observations, predictions, labels):
    """
    This function prints and plots the confusion matrix.
    Normalization can be applied by setting `normalize=True`.
    :param observations:
    :param predictions:
    :param labels:
    :return:
    """
    cm = confusion_matrix(observations, predictions, labels=labels)
    np.set_printoptions(precision=2)

    cmap = plt.cm.Blues
    title = 'Confusion matrix'
    normalize = False

    if normalize:
        cm = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
        print("Normalized confusion matrix")
    else:
        print('Confusion matrix, without normalization')

    print(cm)

    plt.imshow(cm, interpolation='nearest', cmap=cmap)
    plt.title(title)
    plt.colorbar()
    tick_marks = np.arange(len(labels))
    plt.xticks(tick_marks, labels, rotation=45)
    plt.yticks(tick_marks, labels)

    fmt = '.2f' if normalize else 'd'
    thresh = cm.max() / 2.
    for i, j in itertools.product(range(cm.shape[0]), range(cm.shape[1])):
        plt.text(j, i, format(cm[i, j], fmt),
                 horizontalalignment="center",
                 color="white" if cm[i, j] > thresh else "black")

    plt.ylabel('Observed label')
    plt.xlabel('Predicted label')
    plt.tight_layout()

    img_dir = "output/img/"

    save_visualisation('confusion_matrix', img_dir)
