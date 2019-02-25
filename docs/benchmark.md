On this page we show how to benchmark models created using AffectiveTweets against similar models created using the [NLTK sentiment analysis module](http://www.nltk.org/api/nltk.sentiment.html) and [Scikit-learn](https://scikit-learn.org/stable/index.html). We represents tweets using  word n-grams and lexicon-based features and train logistic regression models on the Twitter Message Polarity Classification dataset from the  [SemEval 2013 Sentiment Analysis Task](https://www.cs.york.ac.uk/semeval-2013/task2/).  


The code for reproducing these experiments can be downloaded from [here](https://github.com/felipebravom/AffectiveTweets/tree/master/benchmark).



## AffectiveTweets Scripts

The following bash scripts assume that AffectiveTweets and Weka are already installed. Declare the following variables according to your installation paths.
```
export WEKA_HOME=/home/fbravoma/wekafiles/
export WEKA_PATH=/home/fbravoma/weka-3-9-3/
```


We need to transform the training and testing datasets into Arff format:


```bash
java -cp $WEKA_HOME/packages/AffectiveTweets/AffectiveTweets.jar:$WEKA_PATH/weka.jar weka.core.converters.SemEvalToArff benchmark/dataset/twitter-train-B.txt benchmark/dataset/twitter-train-B.arff


java -cp $WEKA_HOME/packages/AffectiveTweets/AffectiveTweets.jar:$WEKA_PATH/weka.jar weka.core.converters.SemEvalToArff benchmark/dataset/twitter-test-gold-B.tsv benchmark/dataset/twitter-test-gold-B.arff

```



###  Linear model using  n-grams  (n=1,2,3,4). 

We train a linear model using word n-grams as features with marked negation using the Weka command-line interface:

```bash
java -Xmx4G -cp  $WEKA_PATH/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier -v -o -t benchmark/dataset/twitter-train-B.arff -T benchmark/dataset/twitter-test-gold-B.arff -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToSparseFeatureVector -E 5 -D 3 -I 0 -F -M 3 -R -G 0 -taggerFile $WEKA_HOME/packages/AffectiveTweets/resources/model.20120919 -wordClustFile $WEKA_HOME/packages/AffectiveTweets/resources/50mpaths2.txt.gz -Q 4 -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.Reorder -R 3-last,2\"" -S 1 -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000
```

Results:

```
Time taken to test model on test data: 1.56 seconds

=== Error on test data ===

Correctly Classified Instances        2545               66.7453 %
Incorrectly Classified Instances      1268               33.2547 %
Kappa statistic                          0.4457
Mean absolute error                      0.2642
Root mean squared error                  0.3945
Relative absolute error                 59.454  %
Root relative squared error             83.6944 %
Total Number of Instances             3813     


=== Detailed Accuracy By Class ===

                 TP Rate  FP Rate  Precision  Recall   F-Measure  MCC      ROC Area  PRC Area  Class
                 0.616    0.136    0.760      0.616    0.680      0.501    0.816     0.789     positive
                 0.829    0.388    0.617      0.829    0.708      0.442    0.799     0.724     neutral
                 0.361    0.037    0.644      0.361    0.463      0.416    0.851     0.559     negative
Weighted Avg.    0.667    0.229    0.681      0.667    0.658      0.462    0.814     0.725     


=== Confusion Matrix ===

    a    b    c   <-- classified as
  968  543   61 |    a = positive
  221 1360   59 |    b = neutral
   84  300  217 |    c = negative

```





### Linear model using  Bing Liu's Lexicon + SentiStrength

We train a linear model using features derived from Bing Liu's Lexicon and the SentiStrength method:


```bash
java -Xmx4G -cp $WEKA_PATH/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier -v -o -t benchmark/dataset/twitter-train-B.arff -T benchmark/dataset/twitter-test-gold-B.arff -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToSentiStrengthFeatureVector -L $WEKA_HOME/packages/AffectiveTweets/lexicons/SentiStrength/english -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToLexiconFeatureVector -D -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.Reorder -R 3-last,2\"" -S 1 -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000
```
Results:

```
Time taken to test model on test data: 3.32 seconds

=== Error on test data ===

Correctly Classified Instances        2457               64.4375 %
Incorrectly Classified Instances      1356               35.5625 %
Kappa statistic                          0.4029
Mean absolute error                      0.3198
Root mean squared error                  0.4016
Relative absolute error                 71.9598 %
Root relative squared error             85.1839 %
Total Number of Instances             3813     


=== Detailed Accuracy By Class ===

                 TP Rate  FP Rate  Precision  Recall   F-Measure  MCC      ROC Area  PRC Area  Class
                 0.622    0.171    0.718      0.622    0.666      0.463    0.794     0.714     positive
                 0.802    0.399    0.603      0.802    0.688      0.403    0.764     0.667     neutral
                 0.275    0.033    0.611      0.275    0.379      0.344    0.790     0.483     negative
Weighted Avg.    0.644    0.247    0.651      0.644    0.630      0.418    0.781     0.657     


=== Confusion Matrix ===

    a    b    c   <-- classified as
  977  537   58 |    a = positive
  278 1315   47 |    b = neutral
  106  330  165 |    c = negative

```



###  Linear model using  n-grams + Bing Liu's Lexicon + SentiStrength
Now we combine the features from the two previous examples:

```bash
java -Xmx4G -cp $WEKA_PATH/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier  -v -o -t benchmark/dataset/twitter-train-B.arff -T benchmark/dataset/twitter-test-gold-B.arff -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToSparseFeatureVector -E 5 -D 3 -I 0 -F -M 3 -R -G 0 -taggerFile $WEKA_HOME/packages/AffectiveTweets/resources/model.20120919 -wordClustFile $WEKA_HOME/packages/AffectiveTweets/resources/50mpaths2.txt.gz -Q 4 -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToSentiStrengthFeatureVector -L $WEKA_HOME/packages/AffectiveTweets/lexicons/SentiStrength/english -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToLexiconFeatureVector -D -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.Reorder -R 3-last,2\"" -S 1 -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000
```

Results:

```
Time taken to test model on test data: 13.04 seconds

=== Error on test data ===

Correctly Classified Instances        2648               69.4466 %
Incorrectly Classified Instances      1165               30.5534 %
Kappa statistic                          0.4949
Mean absolute error                      0.2401
Root mean squared error                  0.3786
Relative absolute error                 54.0243 %
Root relative squared error             80.3157 %
Total Number of Instances             3813     


=== Detailed Accuracy By Class ===

                 TP Rate  FP Rate  Precision  Recall   F-Measure  MCC      ROC Area  PRC Area  Class
                 0.649    0.139    0.766      0.649    0.703      0.527    0.845     0.812     positive
                 0.823    0.335    0.649      0.823    0.726      0.485    0.826     0.766     neutral
                 0.463    0.039    0.690      0.463    0.554      0.502    0.896     0.642     negative
Weighted Avg.    0.694    0.208    0.704      0.694    0.689      0.505    0.845     0.766     


=== Confusion Matrix ===

    a    b    c   <-- classified as
 1021  485   66 |    a = positive
  232 1349   59 |    b = neutral
   80  243  278 |    c = negative

```



###   Linear model using  n-grams + SentiStrength + all lexicons
Now we include features from all the lexicons implemented by AffectiveTweets:

```bash
java -Xmx4G -cp $WEKA_PATH/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier -v -o -t benchmark/dataset/twitter-train-B.arff -T  benchmark/dataset/twitter-test-gold-B.arff -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToSparseFeatureVector -E 5 -D 3 -I 0 -F -M 3 -R -G 0 -taggerFile $WEKA_HOME/packages/AffectiveTweets/resources/model.20120919 -wordClustFile $WEKA_HOME/packages/AffectiveTweets/resources/50mpaths2.txt.gz -Q 4 -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToSentiStrengthFeatureVector -L $WEKA_HOME/packages/AffectiveTweets/lexicons/SentiStrength/english -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToLexiconFeatureVector -F -D -R -A -N -P -J -H -Q -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.Reorder -R 3-last,2\"" -S 1 -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000
```

Results:

```
Time taken to test model on test data: 13.29 seconds

=== Error on test data ===

Correctly Classified Instances        2706               70.9677 %
Incorrectly Classified Instances      1107               29.0323 %
Kappa statistic                          0.5215
Mean absolute error                      0.2289
Root mean squared error                  0.372 
Relative absolute error                 51.5039 %
Root relative squared error             78.9079 %
Total Number of Instances             3813     


=== Detailed Accuracy By Class ===

                 TP Rate  FP Rate  Precision  Recall   F-Measure  MCC      ROC Area  PRC Area  Class
                 0.658    0.131    0.779      0.658    0.713      0.544    0.854     0.823     positive
                 0.831    0.319    0.663      0.831    0.738      0.509    0.835     0.772     neutral
                 0.514    0.037    0.720      0.514    0.600      0.550    0.913     0.687     negative
Weighted Avg.    0.710    0.197    0.720      0.710    0.706      0.530    0.855     0.779     


=== Confusion Matrix ===

    a    b    c   <-- classified as
 1034  471   67 |    a = positive
  224 1363   53 |    b = neutral
   70  222  309 |    c = negative

```



##  NLTK + SciKit-learn Scripts

Now we will build similar models using **python 3.6**.

First we need to import the following libraries. 

```python
import pandas as pd       
from nltk.tokenize import TweetTokenizer
from nltk.sentiment import SentimentIntensityAnalyzer
from nltk.sentiment.util import  mark_negation
from nltk.corpus import opinion_lexicon

from sklearn.feature_extraction.text import CountVectorizer  
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline, FeatureUnion
from sklearn.base import BaseEstimator, TransformerMixin
from sklearn.metrics import confusion_matrix, cohen_kappa_score
import numpy as np
```



Make sure to install all of them using **pip** or **conda**. 

Next, load training and testing datasets as pandas dataframes:

```python

# load training and testing datasets as a pandas dataframe
train_data = pd.read_csv("dataset/twitter-train-B.txt", header=None, delimiter="\t",usecols=(2,3), names=("sent","tweet"))
test_data = pd.read_csv("dataset/twitter-test-gold-B.tsv", header=None, delimiter="\t",usecols=(2,3), names=("sent","tweet"))

# replace objective-OR-neutral and objective to neutral
train_data.sent = train_data.sent.replace(['objective-OR-neutral','objective'],['neutral','neutral'])

# use a Twitter-specific tokenizer
tokenizer = TweetTokenizer(preserve_case=False, reduce_len=True)


```



### Linear model using  n-grams  (n=1,2,3,4). 

We replicate the same model we created previously using AffectiveTweets. N-grams are extracted using [CountVectorizer](https://scikit-learn.org/stable/modules/generated/sklearn.feature_extraction.text.CountVectorizer.html) from Scikit-learn. N-grams inside a negation word are marked.  



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



Results:

```

Confusion Matrix for Logistic Regression + ngram features:
[[ 172  335   94]
 [  31 1433  176]
 [  48  620  904]]
kappa:0.4236034809946826
```





### Linear model using  Bing Liu's Lexicon + Vader

Unfortunately, SentiStrength is not implemented in the NLTK sentiment module. However, NLTK implements [Vader](https://github.com/cjhutto/vaderSentiment), which is another popular lexicon-based sentiment analysis method.  

We implement a linear model using features derived from Bing Liu's lexicon and Vader.

First, we need to make sure that the required NLTK resources are installed:

```python
import nltk
nltk.download('opinion_lexicon')
nltk.download('vader_lexicon')
```



We extend Scikit-learn classes [BaseEstimator](https://scikit-learn.org/stable/modules/generated/sklearn.base.BaseEstimator.html) and and [TransformerMixin](https://scikit-learn.org/stable/modules/generated/sklearn.base.TransformerMixin.html) to implement a feature extractor:



```python
class LexiconFeatureExtractor(BaseEstimator, TransformerMixin):
    """Takes in a corpus of tweets and calculates features using Bing Liu's lexicon and the Vader method"""

    def __init__(self, tokenizer):
        self.tokenizer = tokenizer
        self.pos_set = set(opinion_lexicon.positive())
        self.neg_set = set(opinion_lexicon.negative())
        self.sid = SentimentIntensityAnalyzer()

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
    
    
    def vader_score(self,sentence):
        """ Calculates sentiment scores for a sentence using the Vader method """
        pol_scores = self.sid.polarity_scores(sentence)
        return(list(pol_scores.values()))

    def transform(self, X, y=None):
        """Applies liu_score and vader_score on a data.frame containing tweets """
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

Results:

```
Confusion Matrix for Logistic Regression + features from Bing Liu's Lexicon and the Vader method
[[ 169  323  109]
 [  51 1275  314]
 [  58  491 1023]]
kappa:0.408231856331834
```







### Linear model using  n-grams + Bing Liu's Lexicon + Vader

We can combine the feature spaces of the two previous examples using the class [FeatureUnion](https://scikit-learn.org/stable/modules/generated/sklearn.pipeline.FeatureUnion.html) from Scikit-learn:

```python

ngram_lex_clf = Pipeline([('feats', FeatureUnion([ ('ngram', vectorizer), ('lexicon',lex_feat) ])), ('clf', log_mod)])


ngram_lex_clf.fit(train_data.tweet, train_data.sent)
pred_ngram_lex = ngram_lex_clf.predict(test_data.tweet)


conf_ngram_lex = confusion_matrix(test_data.sent, pred_ngram_lex)
kappa_ngram_lex = cohen_kappa_score(test_data.sent, pred_ngram_lex) 

print('Confusion Matrix for Logistic Regression + ngrams + features from Bing Liu\'s Lexicon and the Vader method')
print(conf_ngram_lex)
print('kappa:'+str(kappa_ngram_lex))

```



Results:

```
Confusion Matrix for Logistic Regression + ngrams + features from Bing Liu's Lexicon and the Vader method
[[ 268  261   72]
 [  45 1387  208]
 [  56  493 1023]]
kappa:0.5058311344923361
```





# Summary of Results

A table summarising all the experiments from above is shown as follows:



| Features                                   | Implementation     | Kappa Score |
| ------------------------------------------ | ------------------ | ----------- |
| Word n-grams                               | Scikitlearn + NLTK | 0.424       |
| Word n-grams                               | AffectiveTweets    | 0.446       |
| Liu Lexicon  + Vader                       | Scikitlearn + NLTK | 0.408       |
| Liu Lexicon  + SentiStrength               | AffectiveTweets    | 0.402       |
| Word n-grams + Liu Lexicon + Vader         | Scikitlearn + NLTK | 0.506       |
| Word n-grams + Liu Lexicon + SentiStrength | AffectiveTweets    | 0.494       |
| Word n-grams + All lexicons + SentiStrength               | AffectiveTweets    | 0.522       |

