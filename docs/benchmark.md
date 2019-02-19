# Benchmark



In this site we show how to benchmark AffectiveTweets against similar models created using the [NLTK sentiment analysis module](http://www.nltk.org/api/nltk.sentiment.html) and [Scikit-learn](https://scikit-learn.org/stable/index.html) on the dataset from the  [SemEval 2013 Sentiment Analysis in Twitter Message Polarity Classification task](https://www.cs.york.ac.uk/semeval-2013/task2/).  



## AffectiveTweets Scripts

First, we need to transform the training and testing datasets into Arff format:

```bash
java -cp dist/AffectiveTweets/AffectiveTweets.jar:"lib/" weka.core.converters.SemEvalToArff benchmark/dataset/twitter-train-B.txt benchmark/dataset/twitter-train-B.arff
java -cp dist/AffectiveTweets/AffectiveTweets.jar:"lib/" weka.core.converters.SemEvalToArff benchmark/dataset/twitter-test-gold-B.tsv benchmark/dataset/twitter-test-gold-B.arff

```



###  Linear Model using a ngram features, with marked negation, n=1,2,3,4

```bash
java -Xmx4G -cp  /home/fbravoma/weka-3-9-3/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier -v -o -t $HOME/workspace/AffectiveTweets/benchmark/dataset/twitter-train-B.arff -T  $HOME/workspace/AffectiveTweets/benchmark/dataset/twitter-test-gold-B.arff -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToSparseFeatureVector -E 5 -D 3 -I 0 -F -M 3 -R -G 0 -taggerFile /home/fbravoma/wekafiles/packages/AffectiveTweets/resources/model.20120919 -wordClustFile /home/fbravoma/wekafiles/packages/AffectiveTweets/resources/50mpaths2.txt.gz -Q 4 -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.Reorder -R 3-last,2\"" -S 1 -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000
```



### Linear Model using a representation made by Bing Liu's Lexicon + SentiStrength



```bash
java -Xmx4G -cp /home/fbravoma/weka-3-9-3/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier  -t $HOME/workspace/AffectiveTweets/benchmark/dataset/twitter-train-B.arff -T  $HOME/workspace/AffectiveTweets/benchmark/dataset/twitter-test-gold-B.arff -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToSentiStrengthFeatureVector -L /home/fbravoma/wekafiles/packages/AffectiveTweets/lexicons/SentiStrength/english -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToLexiconFeatureVector -D -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.Reorder -R 3-last,2\"" -S 1 -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000
```



###  Linear Model using a representation made by ngrams +Bing Liu's Lexicon + SentiStrength

```bash
java -Xmx4G -cp /home/fbravoma/weka-3-9-3/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier  -t $HOME/workspace/AffectiveTweets/benchmark/dataset/twitter-train-B.arff -T  $HOME/workspace/AffectiveTweets/benchmark/dataset/twitter-test-gold-B.arff -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToSparseFeatureVector -E 5 -D 3 -I 0 -F -M 3 -R -G 0 -taggerFile /home/fbravoma/wekafiles/packages/AffectiveTweets/resources/model.20120919 -wordClustFile /home/fbravoma/wekafiles/packages/AffectiveTweets/resources/50mpaths2.txt.gz -Q 4 -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToSentiStrengthFeatureVector -L /home/fbravoma/wekafiles/packages/AffectiveTweets/lexicons/SentiStrength/english -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToLexiconFeatureVector -D -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.Reorder -R 3-last,2\"" -S 1 -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000
```



###  Linear Model using a representation made by ngrams + All Lexicons

```bash
java -Xmx4G -cp /home/fbravoma/weka-3-9-3/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier  -t $HOME/workspace/AffectiveTweets/benchmark/dataset/twitter-train-B.arff -T  $HOME/workspace/AffectiveTweets/benchmark/dataset/twitter-test-gold-B.arff -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToSparseFeatureVector -E 5 -D 3 -I 0 -F -M 3 -R -G 0 -taggerFile /home/fbravoma/wekafiles/packages/AffectiveTweets/resources/model.20120919 -wordClustFile /home/fbravoma/wekafiles/packages/AffectiveTweets/resources/50mpaths2.txt.gz -Q 4 -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToSentiStrengthFeatureVector -L /home/fbravoma/wekafiles/packages/AffectiveTweets/lexicons/SentiStrength/english -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToLexiconFeatureVector -F -D -R -A -N -P -J -H -Q -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.Reorder -R 3-last,2\"" -S 1 -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000
```





##  NLTK + SciKit-learn Scripts


Import the following libraries.

```python
import pandas as pd       
from nltk.tokenize import TweetTokenizer
from nltk.sentiment import SentimentIntensityAnalyzer
from nltk.sentiment.util import  mark_negation
from nltk.corpus import opinion_lexicon

from sklearn.feature_extraction.text import CountVectorizer  
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.base import BaseEstimator, TransformerMixin
from sklearn.metrics import confusion_matrix, cohen_kappa_score


import numpy as np
```



Load training and testing datasets as a pandas dataframe



```python
train_data = pd.read_csv("dataset/twitter-train-B.txt", header=None, delimiter="\t",usecols=(2,3), names=("sent","tweet"))
test_data = pd.read_csv("dataset/twitter-test-gold-B.tsv", header=None, delimiter="\t",usecols=(2,3), names=("sent","tweet"))

# replaces objective-OR-neutral and objective to neutral
train_data.sent = train_data.sent.replace(['objective-OR-neutral','objective'],['neutral','neutral'])

tokenizer = TweetTokenizer(preserve_case=False, reduce_len=True)

```



### Train a linear model using n-gram features

```python
vectorizer = CountVectorizer(tokenizer = tokenizer.tokenize, preprocessor = mark_negation, ngram_range=(1,4))  
log_mod = LogisticRegression()  
text_clf = Pipeline([('vect', vectorizer), ('clf', log_mod)])

text_clf.fit(train_data.tweet, train_data.sent)

predicted = text_clf.predict(test_data.tweet)

conf = confusion_matrix(test_data.sent, predicted)
kappa = cohen_kappa_score(test_data.sent, predicted) 

print('Confusion Matrix for Logistic Regression + ngram features')
print(conf)
print('kappa:'+str(kappa))

 ```



### Train a linear model using features from Bing Liu's lexicon + the Vader method



```python
class LexiconFeatureExtractor(BaseEstimator, TransformerMixin):
    """Takes in a corpus of tweets and calculates features using Bing Liu's lexicon and the Vader method"""

    def __init__(self, tokenizer):
        self.tokenizer = tokenizer
        self.pos_set = set(opinion_lexicon.positive())
        self.neg_set = set(opinion_lexicon.negative())
        self.sid = SentimentIntensityAnalyzer()

    def liu_score(self,sentence):
        
        tokenized_sent = self.tokenizer.tokenize(sentence)
        pos_words = 0
        neg_words = 0
        for word in tokenized_sent:
            if word in self.pos_set:
                pos_words += 1
            elif word in self.neg_set:
                neg_words += 1
        return [pos_words,neg_words]
    
    
    def vader_score(self,sentence):
        pol_scores = self.sid.polarity_scores(sentence)
        return(list(pol_scores.values()))

    def transform(self, X, y=None):
        """The workhorse of this feature extractor"""
        values = []
        for tweet in X:
            values.append(self.liu_score(tweet)+self.vader_score(tweet))
        
        return(np.array(values))

    def fit(self, X, y=None):
        """Returns `self` unless something different happens in train and test"""
        return self

lex_feat = LexiconFeatureExtractor(tokenizer)

log_mod = LogisticRegression()  
lex_clf = Pipeline([('lexicon', lex_feat), ('clf', log_mod)])


lex_clf.fit(train_data.tweet, train_data.sent)
pred_lex = lex_clf.predict(test_data.tweet)


conf_lex = confusion_matrix(test_data.sent, pred_lex)
kappa_lex = cohen_kappa_score(test_data.sent, pred_lex) 

print('Confusion Matrix for Logistic Regression + features from Bing Liu\'s Lexicon and the Vader method')
print(conf_lex)
print('kappa:'+str(kappa_lex))


```



### Train a linear model using n-grams features + features from Bing Liu's lexicon + the Vader method



```python
from sklearn.pipeline import Pipeline, FeatureUnion

ngram_lex_clf = Pipeline([
    ('feats', FeatureUnion([
        ('ngram', vectorizer), # can pass in either a pipeline
        ('lexicon',lex_feat) # or a transformer
    ])),
    ('clf', log_mod)  # classifier
])


ngram_lex_clf.fit(train_data.tweet, train_data.sent)
pred_ngram_lex = ngram_lex_clf.predict(test_data.tweet)


conf_ngram_lex = confusion_matrix(test_data.sent, pred_ngram_lex)
kappa_ngram_lex = cohen_kappa_score(test_data.sent, pred_ngram_lex) 

print('Confusion Matrix for Logistic Regression + ngrams + features from Bing Liu\'s Lexicon and the Vader method')
print(conf_ngram_lex)
print('kappa:'+str(kappa_ngram_lex))
```





# Results

Classification results on the testing partition using the Kappa statistics as performance metric are shown in the following table.



| Features                                   | Implementation     | Kappa Score |
| ------------------------------------------ | ------------------ | ----------- |
| Word n-grams                               | Scikitlearn + NLTK | 0.424       |
| Word n-grams                               | AffectiveTweets    | 0.446       |
| Liu Lexicon  + Vader                       | Scikitlearn + NLTK | 0.408       |
| Liu Lexicon  + SentiStrength               | AffectiveTweets    | 0.402       |
| Word n-grams + Liu Lexicon + Vader         | Scikitlearn + NLTK | 0.506       |
| Word n-grams + Liu Lexicon + SentiStrength | AffectiveTweets    | 0.494       |
| Word n-grams + All lexicons                | AffectiveTweets    | 0.522       |

