class NamedEntityRecognition:
    
    def __init__(self):
        self.transition_probabilities = {}
        self.word_distribution = {}

        
    def pos_tag(self, sentence):
        ''' Reads the self.transition probabilities from the given file.

        >>> ner = NamedEntityRecognition()
        >>> ner.read_transition_probabilities_from_file("example-trans-probs.tsv")
        >>> ner.read_word_distribution_from_file("example-word-distrib.tsv")
        >>> ner.pos_tag(["James", "Bond", "is", "an", "agent"])

        '''
        k = 0
        for entry in sentence:
            if entry not in self.word_distribution:
                k += 1
        word_probs = []
        transitions = []
        for word in sentence:
            if word_probs == []:
                word_probs.append(self.transition_probabilities["BEG"])
            else:
                help_dict1 = {}
                help_dict2 = {}
                
                for word_class in self.word_distribution:
                    prob_max = 0
                    for word_class_old in word_probs[-1]:
                        
                        if (word_class in self.transition_probabilities[word_class_old] and
                            word in self.word_distribution[word_class]):
                            score = (self.word_distribution[word_class][word] *
                                    self.transition_probabilities[word_class_old][word_class] *
                                    word_probs[-1][word_class_old])
                            if score > prob_max:
                                help_dict1[word_class] = score
                                prob_max += score - prob_max
                                
                                help_dict2[word_class] = word_class_old
                transitions.append(help_dict2)
                word_probs.append(help_dict1)
                                
        res = []   
        counter = len(sentence) - 1
        tag = None
        for i in sentence:
            name = sentence[counter]
            if not tag:
                maxi = 0
                for item in word_probs[counter]:
                    if word_probs[counter][item] > maxi:
                        tag = item
                        maxi = word_probs[counter][item]
            else:
                tag = transitions[counter][tag]
            res = [(name, tag)] + res
            counter -= 1
        return res
        






    def read_transition_probabilities_from_file(self, filename):
        ''' Reads the self.transition probabilities from the given file.

        >>> ner = NamedEntityRecognition()
        >>> ner.read_transition_probabilities_from_file("example-trans-probs.tsv")
        >>> ner.transition_probabilities
            
        '''
        
        with open(filename) as f:
            for line in f:
                tag1, tag2, probability = line.strip().split("\t")

                if tag1 not in self.transition_probabilities:
                    self.transition_probabilities[tag1] = {}
                self.transition_probabilities[tag1][tag2] = float(probability)

    def read_word_distribution_from_file(self, filename):
        ''' Reads the word distribution from the given file.

        >>> ner = NamedEntityRecognition()
        >>> ner.read_word_distribution_from_file("example-word-distrib.tsv")
        >>> ner.word_distribution
        
        '''
        
        with open(filename) as f:
            for line in f:
                word, tag, probability = line.strip().split("\t")

                if tag not in self.word_distribution:
                    self.word_distribution[tag] = {}
                self.word_distribution[tag][word] = float(probability)


if __name__ == "__main__":
    import doctest
    doctest.testmod()
