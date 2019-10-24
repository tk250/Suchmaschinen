"""
Copyright 2019, University of Freiburg.

Elmar Haussmann <haussmann@cs.uni-freiburg.de>
Patrick Brosi <brosi@cs.uni-freiburg.de>
Claudius Korzen <korzen@cs.uni-freiburg.de>
"""

import re

import numpy

from scipy.sparse import csr_matrix


def generate_vocabularies(filename):
    """
    Reads the given file and generates vocabularies mapping from label/class to
    label ids and from word to word id.

    You should call this ONLY on your training data.
    """

    # Map from label/class to label id.
    class_vocabulary = dict()

    # Map from word to word id.
    word_vocabulary = dict()

    class_id = 0
    word_id = 0

    # Read the file (containing the training data).
    with open(filename, "r") as f:
        for line in f:
            label, text = line.strip().split('\t')

            if label not in class_vocabulary:
                class_vocabulary[label] = class_id
                class_id += 1

            # Remove all non-characters and non-digits from the text.
            text = re.sub("\\W+", " ", text.lower())
            # Split the text into words.
            words = text.split()

            # Add the words to the vocabulary.
            for word in words:
                if word not in word_vocabulary:
                    word_vocabulary[word] = word_id
                    word_id += 1

    return word_vocabulary, class_vocabulary


def read_labeled_data(filename, class_vocab, word_vocab):
    """
    Reads the given file and returns a sparse document-term matrix as well as a
    list of labels of each document. You need to provide a class and word
    vocabulary. Words not in the vocabulary are ignored. Documents labeled
    with classes not in the class vocabulary are also ignored.

    The returned document-term matrix X has size n x m, where n is the number
    of documents and m the number of word ids. The value at i, j denotes the
    number of times word id j is present in document i.

    The returned labels vector y has size n (one label for each document). The
    value at index j denotes the label (class id) of document j.
    """

    labels = []
    row, col, value = [], [], []
    num_examples = 0
    num_cols = len(word_vocab)

    with open(filename, "r") as f:
        for i, line in enumerate(f):
            label, text = line.strip().split('\t')

            if label in class_vocab:
                num_examples += 1
                labels.append(class_vocab[label])
                words = re.sub("\\W+", " ", text.lower()).split()
                for w in words:
                    if w in word_vocab:
                        w_id = word_vocab[w]
                        row.append(i)
                        col.append(w_id)
                        # Duplicate values at the same position i,j are summed.
                        value.append(1.0)

    x = csr_matrix((value, (row, col)), shape=(num_examples, num_cols))
    y = numpy.array(labels)
    return x, y


class NaiveBayes(object):
    """
    A simple naive bayes classifier as explained in the lecture.

    >>> numpy.set_printoptions(formatter={"float": lambda x: ("%.3f" % x)})
    """

    def __init__(self):
        """
        Creates a new naive bayes classifier supporting num_classes of classes
        and num_features of words.
        """
        # The stored probabilities of each class.
        self.p_c = None
        # The stored probabilities of each word in each class
        self.p_wc = None

    def train(self, x, y):
        """
        Trains on the sparse document-term matrix X and associated labels y.

        In the test case below, p_wc is a class-term-matrix and has a row
        for each class and a column for each term. So the value at i,j is
        the p_wc for the j-th term in the i-th class.

        p_c is an array of global probabilities for each class.

        Remember to use epsilon = 1/10 for your training, as described in the
        lecture!

        >>> wv, cv = generate_vocabularies("example_train.tsv")
        >>> X, y = read_labeled_data("example_train.tsv", cv, wv)
        >>> nb = NaiveBayes()
        >>> nb.train(X, y)
        >>> numpy.round(numpy.exp(nb.p_wc), 3)
        array([[0.664, 0.336],
               [0.320, 0.680]])
        >>> numpy.round(numpy.exp(nb.p_c), 3)
        array([0.429, 0.571])
        """

        maxi = 0
        for c1 in y:
            if c1 > maxi:
                maxi = c1
        total_amount = len(y)
        self.p_c = [0 for x in range(maxi + 1)]
        for c2 in y:
            self.p_c[c2] += 1 / total_amount
        self.p_c = numpy.log(self.p_c)
        num_classes = len(self.p_c)
        doc_list0 = [1 if c == 0 else 0 for c in y]
        new_y = numpy.array(doc_list0)
        for i in range(1, num_classes):
            doc_list = [1 if c == i else 0 for c in y]
            new_y = numpy.vstack([new_y, doc_list])
        self.p_wc = new_y * x
        ncs = numpy.sum(self.p_wc, axis=1)
        num_words = x.shape[1]
        for j in range(len(ncs)):
            self.p_wc[j] = numpy.log((self.p_wc[j] + 1/10)
                                     / (ncs[j] + 1/10 * num_words))

    def predict(self, x):
        """
        Predicts a label for each example in the document-term matrix,
        based on the learned probabities stored in this class.

        Returns a list of predicted label ids.

        >>> wv, cv = generate_vocabularies("example_train.tsv")
        >>> X, y = read_labeled_data("example_train.tsv", cv, wv)
        >>> nb = NaiveBayes()
        >>> nb.train(X, y)
        >>> X_test, y_test = read_labeled_data("example_test.tsv", cv, wv)
        >>> nb.predict(X_test)
        array([0, 1, 0])
        >>> nb.predict(X)
        array([0, 0, 1, 0, 1, 1, 1])
        """

        res = self.p_wc * numpy.transpose(x)
        for i in range(len(self.p_c)):
            res[i] = res[i] + self.p_c[i]
        result = res.argmax(0).astype('int')
        return result

    def evaluate(self, x, y):
        """
        Predicts the labels of X and computes the precisions, recalls and
        F1 scores for each class.

        >>> wv, cv = generate_vocabularies("example_train.tsv")
        >>> X_train, y_train = read_labeled_data("example_train.tsv", cv, wv)
        >>> X_test, y_test = read_labeled_data("example_test.tsv", cv, wv)
        >>> nb = NaiveBayes()
        >>> nb.train(X_train, y_train)
        >>> precisions, recalls, f1_scores = nb.evaluate(X_test, y_test)
        >>> precisions
        {0: 0.5, 1: 1.0}
        >>> recalls
        {0: 1.0, 1: 0.5}
        >>> {x: '%.2f' % f1_scores[x] for x in f1_scores}
        {0: '0.67', 1: '0.67'}
        """

        d_c = numpy.zeros(len(self.p_c))
        d_c_prime = numpy.zeros(len(self.p_c))
        c_cut = numpy.zeros(len(self.p_c))
        test_result = self.predict(x)
        for j in range(len(test_result)):
            if test_result[j] == y[j]:
                c_cut[y[j]] += 1
            d_c[y[j]] += 1
            d_c_prime[test_result[j]] += 1
        p = c_cut / d_c_prime
        r = c_cut / d_c
        f_measure = 2 * p * r / (p + r)
        p = dict(enumerate(p))
        r = dict(enumerate(r))
        f_measure = dict(enumerate(f_measure))
        return p, r, f_measure
