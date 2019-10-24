# Copyright 2018, University of Freiburg,
# Chair of Algorithms and Data Structures.
# Author: Hannah Bast <bast@cs.uni-freiburg.de>,
# Tim Krautschneider <tim@uk-is.de>

import re
import sys


class InvertedIndex:
    """ A simple inverted index, as explained in L1. """

    def __init__(self):
        """ Start with an empty index. """

        self.inverted_lists = {}

    def read_from_file(self, file_name):
        """
        Construct from given file.

        >>> ii = InvertedIndex()
        >>> ii.read_from_file("example.txt")
        >>> sorted(list(ii.inverted_lists.items()))
        [('doc', [1, 2, 3]), ('first', [1, 3]), ('sec', [2]), ('three', [3])]
        """

        with open(file_name) as file:
            record_id = 0
            for line in file:
                record_id += 1
                words = re.split("[^a-zA-Z]+", line)
                for word in words:
                    if (len(word) > 0):
                        word = word.lower()
                        # print(word)
                        if (word not in self.inverted_lists):
                            self.inverted_lists[word] = []
                        if self.inverted_lists[word] == []:
                            self.inverted_lists[word].append(record_id)
                        elif self.inverted_lists[word][-1] != record_id:
                            self.inverted_lists[word].append(record_id)

    def intersect(self, list1, list2):
        """
        Intersect two inverted lists

        >>> ii = InvertedIndex()
        >>> list1 = [1, 4, 5]
        >>> list2 = [2, 4, 5]
        >>> ii.intersect(list1, list2)
        [4, 5]
        """

        reslist = []
        c1 = 0
        c2 = 0
        while c1 < len(list1) and c2 < len(list2):
            if list1[c1] == list2[c2]:
                reslist.append(list1[c1])
                c1 += 1
                c2 += 1
            elif list1[c1] < list2[c2]:
                c1 += 1
            else:
                c2 += 1
        return reslist

    def process_query(self, wordlist):
        """
        Fetch inverted lists of keywords and intersect them.

        >>> ii = InvertedIndex()
        >>> ii.read_from_file("example.txt")
        >>> ii.process_query(["first", "doc"])
        [1, 3]
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
            resultlist = self.intersect(resultlist, invertedlists[counter])
            counter += 1
        return resultlist


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
