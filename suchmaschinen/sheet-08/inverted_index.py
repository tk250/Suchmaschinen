# Copyright 2018, University of Freiburg,
# Chair of Algorithms and Data Structures.
# Author: Hannah Bast <bast@cs.uni-freiburg.de>,
# Tim Krautschneider <tim@uk-is.de>


import math
import re

import numpy

from scipy.sparse import csr_matrix


class InvertedIndex:
    """ A simple inverted index, as explained in L1. """

    def __init__(self):
        """ Start with an empty index. """

        self.inverted_lists = {}
        self.terms = []
        self.num_terms = 0
        self.num_docs = 0
        self.sparse_row = None
        self.td_matrix = None
        self.term_ids = {}

    def read_from_file(self, file_name, k, b):
        """
        Construct from given file.

        >>> ii = InvertedIndex()
        >>> ii.read_from_file("example.txt", 1.75, 0.75)
        >>> sorted(list(ii.inverted_lists.items()))
        ... #doctest:+NORMALIZE_WHITESPACE
        [('animated', [[1, 0.459], [2, 0.402], [4, 0.358]]),
        ('animation', [[3, 2.211]]), ('film', [[2, 0.969], [4, 0.863]]),
        ('movie', [[1, 0.0], [2, 0.0], [3, 0.0], [4, 0.0]]),
        ('non', [[2, 1.938]]), ('short', [[3, 1.106], [4, 1.313]])]
        >>> (ii.num_terms, ii.num_docs)
        (6, 4)
        >>> ii.terms
        ['movie', 'animated', 'non', 'film', 'short', 'animation']
        """

        recordlenghts = []
        with open(file_name) as file:
            doc_id = 0
            for line in file:
                doc_id += 1
                terms = re.split("[^a-zA-Z]+", line)
                counter = 0
                for term in terms:
                    if (len(term) > 0):
                        counter += 1
                        term = term.lower()
                        # print(term)
                        if (term not in self.inverted_lists):
                            self.terms.append(term)
                            self.inverted_lists[term] = []
                        if self.inverted_lists[term] == []:
                            self.inverted_lists[term].append([doc_id, 1])
                        elif self.inverted_lists[term][-1][0] != doc_id:
                            self.inverted_lists[term].append([doc_id, 1])
                        else:
                            self.inverted_lists[term][-1][1] += 1
                recordlenghts.append(counter)
        self.num_docs = doc_id
        self.num_terms = len(self.inverted_lists.keys())
        avdl = 0

        for count in recordlenghts:
            avdl += count
        avdl = avdl / (len(recordlenghts))
        for entry in self.inverted_lists:
            c1 = 0
            for id in self.inverted_lists[entry]:
                c2 = id[0] - 1
                dl = recordlenghts[c2]
                n = len(recordlenghts)
                df = len(self.inverted_lists[entry])
                tf = self.inverted_lists[entry][c1][1]
                tfstar = tf * (k + 1)/(k * (1 - b + b * dl / avdl) + tf)
                bm25 = round(tfstar * math.log(n / df, 2), 3)
                self.inverted_lists[entry][c1][1] = bm25
                c1 += 1

    def preprocessing_vsm(self):
        """
        Built a termdocument from the inverted index

        >>> ii = InvertedIndex()
        >>> ii.read_from_file("example.txt", 1.75, 0.75)
        >>> ii.preprocessing_vsm()
        >>> ii.td_matrix
        array([[0.459, 0.402, 0.   , 0.358],
               [0.   , 0.   , 2.211, 0.   ],
               [0.   , 0.969, 0.   , 0.863],
               [0.   , 0.   , 0.   , 0.   ],
               [0.   , 1.938, 0.   , 0.   ],
               [0.   , 0.   , 1.106, 1.313]])
        """

        term = []
        document = []
        score = []
        counter = 0
        for entry in sorted(self.inverted_lists):
            self.term_ids[entry] = counter
            for doc in self.inverted_lists[entry]:
                term.append(counter)
                document.append(doc[0] - 1)
                score.append(doc[1])
            counter += 1
        self.sparse_row = csr_matrix((score, (term, document)))
        self.td_matrix = self.sparse_row.toarray()

    def process_query_vsm(self, query):
        """
        Process given Keyword query

        >>> ii = InvertedIndex()
        >>> ii.read_from_file("example.txt", 1.75, 0.75)
        >>> ii.preprocessing_vsm()
        >>> ii.process_query_vsm("movie film movie short")
        array([0.   , 0.969, 1.106, 2.176])
        """
        query_arr = numpy.zeros(self.num_terms)
        terms = re.split("[^a-zA-Z]+", query)
        for term in terms:
            if len(term) > 0:
                t_id = self.term_ids[term]
                query_arr[t_id] += 1
        return query_arr.dot(self.td_matrix)
