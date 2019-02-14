	
import pandas as pd       

from unidecode import unidecode

from nltk.tokenize import TweetTokenizer
from nltk.sentiment import SentimentIntensityAnalyzer
from nltk.sentiment.util import  mark_negation

from sklearn.feature_extraction.text import CountVectorizer  
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline

from sklearn.metrics import f1_score, confusion_matrix, cohen_kappa_score

 
# http://michelleful.github.io/code-blog/2015/06/20/pipelines/  Scikit learn Pipelines for multiple types of featr=yres


# nltk.download('vader_lexicon')

# loads training and testing datasets as a pandas dataframe
train_data = pd.read_csv("dataset/twitter-train-B.txt", header=None, delimiter="\t",usecols=(2,3), names=("sent","tweet"))
test_data = pd.read_csv("dataset/twitter-test-gold-B.tsv", header=None, delimiter="\t",usecols=(2,3), names=("sent","tweet"))

# replaces objective-OR-neutral and objective to neutral
train_data.sent = train_data.sent.replace(['objective-OR-neutral','objective'],['neutral','neutral'])

tokenizer = TweetTokenizer(preserve_case=False, reduce_len=True)

vectorizer = CountVectorizer(tokenizer = tokenizer.tokenize, preprocessor = mark_negation, ngram_range=(1,4))  
log_mod = LogisticRegression()  
text_clf = Pipeline([('vect', vectorizer), ('clf', log_mod)])


text_clf.fit(train_data.tweet, train_data.sent)

predicted = text_clf.predict(test_data.tweet)

conf = confusion_matrix(test_data.sent, predicted)
f1_macro=f1_score(test_data.sent, predicted, average='macro') 
kappa = cohen_kappa_score(test_data.sent, predicted) 



########################################


from nltk.corpus import opinion_lexicon

sentence = 'I love beer'

sid = SentimentIntensityAnalyzer()
sid.polarity_scores(sentence)


def liu_score(sentence, tokenizer):    
    tokenized_sent = tokenizer.tokenize(sentence)
    pos_words = 0
    neg_words = 0
    for word in tokenized_sent:
        if word in opinion_lexicon.positive():
            pos_words += 1
        elif word in opinion_lexicon.negative():
            neg_words += 1
    return {'pos_words':pos_words,'neg_words':neg_words}


#http://michelleful.github.io/code-blog/2015/06/20/pipelines/
# Implement a FeatureExtractor using scikitlearn

from sklearn.base import BaseEstimator, TransformerMixin

class LiuPolarityExtractor(BaseEstimator, TransformerMixin):
    """Takes in dataframe, extracts road name column, outputs average word length"""

    def __init__(self):
        pass

    def liu_score(sentence, tokenizer):    
        tokenized_sent = tokenizer.tokenize(sentence)
        pos_words = 0
        neg_words = 0
        for word in tokenized_sent:
            if word in opinion_lexicon.positive():
                pos_words += 1
            elif word in opinion_lexicon.negative():
                neg_words += 1
        return [pos_words,neg_words]

    def transform(self, df, y=None):
        """The workhorse of this feature extractor"""
        return df.apply(self.liu_score)

    def fit(self, df, y=None):
        """Returns `self` unless something different happens in train and test"""
        return self





def convert_to_feature_dicts(tweets,remove_stop_words,n): 
    feature_dicts = []
    for tweet in tweets:
        # build feature dictionary for tweet
        feature_dict = {}
        if remove_stop_words:
            for segment in tweet:
                for token in segment:
                    if token not in stopwords and (n<=0 or total_train_bow[token]>=n):
                        feature_dict[token] = feature_dict.get(token,0) + 1
        else:
            for segment in tweet:
                for token in segment:
                    if n<=0 or total_train_bow[token]>=n:
                        feature_dict[token] = feature_dict.get(token,0) + 1
        feature_dicts.append(feature_dict)
    return feature_dicts


#https://sajalsharma.com/portfolio/sentiment_analysis_tweets




from nltk.sentiment.vader import SentimentIntensityAnalyzer 
  
hotel_rev = ["Great place to be when you are in Bangalore.","The place was being renovated when I visited so the seating was limited."]
  
sid = SentimentIntensityAnalyzer()
for sentence in hotel_rev:
     print(sentence)
     ss = sid.polarity_scores(sentence)
     print(ss)
     for k in ss:
         print(k)
     print()


sent = "I didn't like this movie . It was bad .".split()
mark_negation(sent)





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

print(mark_negation("I don't like the movie .".split()))  # ['I', "don't", 'like_NEG', 'the_NEG', 'movie._NEG']
 
# The nltk classifier won't be able to handle the whole training set
TRAINING_COUNT = 5000
 

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
 