	
import pandas as pd       

from unidecode import unidecode
from nltk import word_tokenize
from nltk.classify import NaiveBayesClassifier
from nltk.sentiment import SentimentAnalyzer
from nltk.sentiment.util import extract_unigram_feats, mark_negation
 

data = pd.read_csv("../data/labeledTrainData.tsv", header=0, delimiter="\t", quoting=3)
 
# 25000 movie reviews
print(data.shape) # (25000, 3) 
print(data["review"][0])         # Check out the review
print(data["sentiment"][0])          # Check out the sentiment (0/1)


import random
 
sentiment_data = zip(data["review"], data["sentiment"])
random.shuffle(sentiment_data)
 
# 80% for training
train_X, train_y = zip(*sentiment_data[:20000])
 
# Keep 20% for testing
test_X, test_y = zip(*sentiment_data[20000:])



 
# mark_negation appends a "_NEG" to words after a negation untill a punctuation mark.
# this means that the same after a negation will be handled differently 
# than the word that's not after a negation by the classifier
print(mark_negation("I like the movie .".split()))        # ['I', 'like', 'the', 'movie.']
print(mark_negation("I don't like the movie .".split()))  # ['I', "don't", 'like_NEG', 'the_NEG', 'movie._NEG']
 
# The nltk classifier won't be able to handle the whole training set
TRAINING_COUNT = 5000
 
analyzer = SentimentAnalyzer()
vocabulary = analyzer.all_words([mark_negation(word_tokenize(unidecode(clean_text(instance)))) 
                                 for instance in train_X[:TRAINING_COUNT]])
print("Vocabulary: ", len(vocabulary)) # 1356908
 
print("Computing Unigran Features ...")
unigram_features = analyzer.unigram_word_feats(vocabulary, min_freq=10)
print("Unigram Features: ", len(unigram_features)) # 8237
 
analyzer.add_feat_extractor(extract_unigram_feats, unigrams=unigram_features)
 
# Build the training set
_train_X = analyzer.apply_features([mark_negation(word_tokenize(unidecode(clean_text(instance)))) 
                                    for instance in train_X[:TRAINING_COUNT]], labeled=False)
 
# Build the test set
_test_X = analyzer.apply_features([mark_negation(word_tokenize(unidecode(clean_text(instance)))) 
                                   for instance in test_X], labeled=False)
 
trainer = NaiveBayesClassifier.train
classifier = analyzer.train(trainer, zip(_train_X, train_y[:TRAINING_COUNT]))
 
score = analyzer.evaluate(zip(_test_X, test_y))
print("Accuracy: ", score['Accuracy']) # 0.8064 for TRAINING_COUNT=5000
 