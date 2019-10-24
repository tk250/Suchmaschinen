# Copyright 2018, University of Freiburg,
# Chair of Algorithms and Data Structures.
# Author: Hannah Bast <bast@cs.uni-freiburg.de>,
# Tim Krautschneider <tim@uk-is.de>

import math
import re
import sys


class InvertedIndex:
    """ A simple inverted index, as explained in L1. """

    def __init__(self):
        """ Start with an empty index. """

        self.inverted_lists = {}

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
        """

        recordlenghts = []
        with open(file_name) as file:
            record_id = 0
            for line in file:
                record_id += 1
                words = re.split("[^a-zA-Z]+", line)
                counter = 0
                for word in words:
                    if (len(word) > 0):
                        counter += 1
                        word = word.lower()
                        # print(word)
                        if (word not in self.inverted_lists):
                            self.inverted_lists[word] = []
                        if self.inverted_lists[word] == []:
                            self.inverted_lists[word].append([record_id, 1])
                        elif self.inverted_lists[word][-1][0] != record_id:
                            self.inverted_lists[word].append([record_id, 1])
                        else:
                            self.inverted_lists[word][-1][1] += 1
                recordlenghts.append(counter)
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

    def merge(self, list1, list2):
        """
        Intersect two inverted lists

        >>> ii = InvertedIndex()
        >>> list1 = [[1, 4.3], [2, 0.3], [5, 6.3]]
        >>> list2 = [[2, 4.8], [5, 3.9]]
        >>> ii.merge(list1, list2)
        [[1, 4.3], [2, 5.1], [5, 10.2]]
        """

        reslist = []
        c1 = 0
        c2 = 0
        while c1 < len(list1) and c2 < len(list2):
            if list1[c1][0] == list2[c2][0]:
                reslist.append([list1[c1][0], list1[c1][1] + list2[c2][1]])
                c1 += 1
                c2 += 1
            elif list1[c1] < list2[c2]:
                reslist.append(list1[c1])
                c1 += 1
            else:
                reslist.append(list2[c2])
                c2 += 1
        if c1 == len(list1):
            while c2 < len(list2):
                reslist.append(list2[c2])
                c2 += 1
        else:
            while c1 < len(list2):
                reslist.append(list1[c1])
                c1 += 1
        return reslist

    def second(self, element):
        """
        Helping function for process query to sort after the second element
        Got the idea from: https://www.w3schools.com/python/ref_list_sort.asp
        """

        return element[1]

    def process_query(self, wordlist):
        """
        Fetch inverted lists of keywords and intersect them.

        >>> ii = InvertedIndex()
        >>> ii.read_from_file("example.txt", 1.75, 0.75)
        >>> ii.process_query(["film", "animated"])
        [[2, 1.371], [4, 1.221], [1, 0.459]]
        >>> ii.process_query(["fiasd", "doC"])
        []
        """

        invertedlists = []
        for keyword in wordlist:
            if keyword in self.inverted_lists:
                invertedlists.append(self.inverted_lists[keyword])
            else:
                return[]
        resultlist = invertedlists[0]
        counter = 1
        while counter < len(invertedlists):
            resultlist = self.merge(resultlist, invertedlists[counter])
            counter += 1
        resultlist.sort(key=self.second, reverse=True)
        return resultlist[0:3]


class EvaluateInvertedIndex:
    """ Class to evaluate inverted indices"""

    def __init__(self):
        """ Start with an empty dict of queries. """

        self.queries = {}

    def read_benchmark(self, file_name):
        """
        Read a benchmark

        >>> eii = EvaluateInvertedIndex()
        >>> eii.read_benchmark("example-benchmark.txt")
        >>> sorted(list(eii.queries.items()))
        [('animated film', {1, 3, 4}), ('short film', {3, 4})]
        """

        with open(file_name) as file:
            for line in file:
                word = re.findall("[a-zA-Z][a-zA-Z ]*[a-zA-Z]", line)
                numbers = re.split("[^1-9]+", line)
                for num in numbers:
                    if len(num) == 0:
                        numbers.remove(num)
                result = [int(number) for number in numbers]
                self.queries[word[0]] = set(result)

    def average_precision(self, res_ids, relevant_ids):
        """
        Calculate the average precision

        >>> eii = EvaluateInvertedIndex()
        >>> eii.average_precision([7, 17, 9, 42, 5], {5, 7, 12, 42})
        0.525
        """

        relevant_list = []
        result = 0
        for i in range(len(res_ids)):
            if res_ids[i] in relevant_ids:
                relevant_list.append(i+1)
        for rel in relevant_list:
            result += self.precision_at_k(res_ids, relevant_ids, rel)
        return result/len(relevant_ids)

    def precision_at_k(self, res_ids, relevant_ids, k):
        """
        Calculate the precision of a given result

        >>> eii = EvaluateInvertedIndex()
        >>> eii.precision_at_k([5, 3, 6, 1, 2], {1, 2, 5, 6, 7, 8}, 0)
        0
        >>> eii.precision_at_k([5, 3, 6, 1, 2], {1, 2, 5, 6, 7, 8}, k=4)
        0.75
        >>> eii.precision_at_k([5, 3, 6, 1, 2], {1, 2, 5, 6, 7, 8}, k=8)
        0.5
        """

        if k < 1:
            return 0
        counter1 = 0
        counter2 = 0
        while counter1 < k:
            if counter1 < len(res_ids) and res_ids[counter1] in relevant_ids:
                counter2 += 1
            counter1 += 1
        return counter2/k


if __name__ == "__main__":
    if (len(sys.argv) != 2):
        print("Usage: python3 inverted_index.py <file>")
        sys.exit(1)
    file_name = sys.argv[1]
    ii = InvertedIndex()
    ii.read_from_file(file_name)
    f = open(file_name)
    file_lines = f.readlines()
    while True:
        inputword = input("Eingabe: ")
        if inputword in ii.inverted_lists:
            counter = 0
            while counter < 3 and counter < len(ii.inverted_lists[inputword]):
                print(file_lines[ii.inverted_lists[inputword][counter] - 1])
                counter += 1
