# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

# Authors: Felipe Bravo-Marquez

	
import pandas as pd       
from nltk.tokenize import TweetTokenizer
from nltk.sentiment import SentimentIntensityAnalyzer
from nltk.sentiment.util import  mark_negation
from nltk.corpus import opinion_lexicon

from sklearn.feature_extraction.text import CountVectorizer  
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline, FeatureUnion
from sklearn.base import BaseEstimator, TransformerMixin
from sklearn.metrics import confusion_matrix, cohen_kappa_score, classification_report
import numpy as np




# load training and testing datasets as a pandas dataframe
train_data = pd.read_csv("dataset/twitter-train-B.txt", header=None, delimiter="\t",usecols=(2,3), names=("sent","tweet"))
test_data = pd.read_csv("dataset/twitter-test-gold-B.tsv", header=None, delimiter="\t",usecols=(2,3), names=("sent","tweet"))

# replace objective-OR-neutral and objective to neutral
train_data.sent = train_data.sent.replace(['objective-OR-neutral','objective'],['neutral','neutral'])

# use a Twitter-specific tokenizer
tokenizer = TweetTokenizer(preserve_case=False, reduce_len=True)


######################################################################################################
#
#  Train a linear model using n-grams features + features from Bing Liu's lexicon + the Vader method
#
#####################################################################################################
#import nltk
#nltk.download('opinion_lexicon')
#nltk.download('vader_lexicon')



class LiuFeatureExtractor(BaseEstimator, TransformerMixin):
    """Takes in a corpus of tweets and calculates features using Bing Liu's lexicon"""

    def __init__(self, tokenizer):
        self.tokenizer = tokenizer
        self.pos_set = set(opinion_lexicon.positive())
        self.neg_set = set(opinion_lexicon.negative())

    def liu_score(self,sentence):
        """Calculates the number of positive and negative words in the sentence using Bing Liu's Lexicon""" 
        tokenized_sent = self.tokenizer.tokenize(sentence)
        pos_words = 0
        neg_words = 0
        for word in tokenized_sent:
            if word in self.pos_set:
                pos_words += 1
            elif word in self.neg_set:
                neg_words += 1
        return [pos_words,neg_words]
    
    def transform(self, X, y=None):
        """Applies liu_score and vader_score on a data.frame containing tweets """
        values = []
        for tweet in X:
            values.append(self.liu_score(tweet))
        
        return(np.array(values))

    def fit(self, X, y=None):
        """This function must return `self` unless we expect the transform function to perform a 
        different action on training and testing partitions (e.g., when we calculate unigram features, 
        the dictionary is only extracted from the first batch)"""
        return self







class VaderFeatureExtractor(BaseEstimator, TransformerMixin):
    """Takes in a corpus of tweets and calculates features using the Vader method"""

    def __init__(self, tokenizer):
        self.tokenizer = tokenizer
        self.sid = SentimentIntensityAnalyzer()

  
    def vader_score(self,sentence):
        """ Calculates sentiment scores for a sentence using the Vader method """
        pol_scores = self.sid.polarity_scores(sentence)
        return(list(pol_scores.values()))

    def transform(self, X, y=None):
        """Applies vader_score on a data.frame containing tweets """
        values = []
        for tweet in X:
            values.append(self.vader_score(tweet))
        
        return(np.array(values))

    def fit(self, X, y=None):
        """Returns `self` unless something different happens in train and test"""
        return self




vectorizer = CountVectorizer(tokenizer = tokenizer.tokenize, preprocessor = mark_negation, ngram_range=(1,4))  
vader_feat = VaderFeatureExtractor(tokenizer)
liu_feat = LiuFeatureExtractor(tokenizer)
log_mod = LogisticRegression(solver='liblinear',multi_class='ovr')  



ngram_lex_clf = Pipeline([ ('feats', 
                            FeatureUnion([ ('ngram', vectorizer), ('vader',vader_feat),('liu',liu_feat)  ])),
    ('clf', log_mod)])


ngram_lex_clf.fit(train_data.tweet, train_data.sent)
pred_ngram_lex = ngram_lex_clf.predict(test_data.tweet)


conf_ngram_lex = confusion_matrix(test_data.sent, pred_ngram_lex)
kappa_ngram_lex = cohen_kappa_score(test_data.sent, pred_ngram_lex) 
class_rep = classification_report(test_data.sent, pred_ngram_lex)


print('Confusion Matrix for Logistic Regression + ngrams + features from Bing Liu\'s Lexicon and the Vader method')
print(conf_ngram_lex)
print('Classification Report')
print(class_rep)
print('kappa:'+str(kappa_ngram_lex))














