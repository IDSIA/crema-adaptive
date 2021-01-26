import os

import matplotlib.pyplot as plt
import numpy as np


def check_dir(directory):
    """
    Check if the path is a directory, if not create it.
    :param directory: path to the directory
    """
    os.makedirs(directory, exist_ok=True)


def save_metrics(out_file, metrics, header):
    """

    :param out_file:
    :param metrics:
    :param header:
    """
    np.savetxt(out_file, metrics, delimiter=',', fmt='%f', header=header)


def one_hot_encode(x, n_classes):
    """
    One hot encode a list of sample labels. Return a one-hot encoded vector for each label.
    (source: https://stackoverflow.com/a/42879831)
    : x: List of sample Labels
    : return: Numpy array of one-hot encoded labels
     """
    return np.eye(n_classes)[x]


def save_visualisation(filename, img_dir):
    """
    :param filename: name of the image
    :param img_dir: path where to save the image
    """
    check_dir(img_dir)

    file = os.path.join(img_dir, '%s.pdf' % filename)
    img = os.path.join(img_dir, '%s.png' % filename)

    plt.savefig(file)
    plt.savefig(img, dpi=300, bbox_inches='tight')
    plt.close()
