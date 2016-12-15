<img src="logofinal.png" alt="alt text" width="250px" height="200px"> 

## About

AffectiveTweets is a [WEKA](http://www.cs.waikato.ac.nz/~ml/weka/) package for analysing emotion and sentiment  of English written tweets developed by [Felipe Bravo-Marquez](http://www.cs.waikato.ac.nz/~fjb11/). 

The package implements WEKA filters for converting tweets contained in string attributes into feature vectors that can be fed into machine learning algorithms.

### Filters

1. __TweetToSparseFeatureVector__: calculates sparse features from tweets. There are options provides for filtering out infrequent features and setting the weighting approach  (boolean or frequency based).
 * __Word n-grams__: extracts word n-grams from n=1 to a maximum value. 
 * __Negations__: add a prefix to words occurring in negated contexts e.g., I don't like you => I don't NEG-like NEG-you. The prefixes only affect the word n-gram features. The scope of negation finishes with the next punctuation mark.
 * __Character n-grams__: calculates character n-grams.
 * __POS tags__: tags tweets using the [CMU Tweet NLP tool](http://www.cs.cmu.edu/~ark/TweetNLP/). It creates a vector space model based on the sequence of POS tags allowing to set the maximum POS n-gram size.
 * __Brown clusters__: maps the words to Brown word clusters and creates a vector space model of lower dimensionality. It can be used with n-grams of word clusters. The word clusters are also taken from the [CMU Tweet NLP tool](http://www.cs.cmu.edu/~ark/TweetNLP/).

2. __TweetToLexiconFeatureVector__: calculates features from a tweet using several lexicons.
 * [MPQA](http://mpqa.cs.pitt.edu/lexicons/subj_lexicon): counts the number of positive and negative words from the MPQA subjectivity lexicon.
 * [Bing Liu](https://www.cs.uic.edu/~liub/FBS/sentiment-analysis.html#lexicon): counts the number of positive and negative words from the Bing Liu lexicon.
 * [AFINN](https://github.com/fnielsen/afinn): calculates a positive and negative score by aggregating the positive and negative word scores provided by this lexicon.
 * [Sentiment140](http://saifmohammad.com/WebPages/lexicons.html): calculates a positive and negative score by aggregating the positive and negative word scores provided by this lexicon created with tweets annotated by emoticons. 
 * [NRC Hashtag Sentiment lexicon](http://saifmohammad.com/WebPages/lexicons.html): calculates a positive and negative score by aggregating the positive and negative word scores provided by this lexicon created with tweets annotated with emotional hashtags. 
 * [NRC Word-Emotion Association Lexicon](http://saifmohammad.com/WebPages/NRC-Emotion-Lexicon.htm): counts the number of words matching each emotion from this lexicon.
 * [NRC-10 Expanded](http://www.cs.waikato.ac.nz/ml/sa/lex.html#emolextwitter): adds the emotion associations of the words matching the Twitter Specific expansion of the NRC Word-Emotion Association Lexicon.
 * [NRC Hashtag Emotion Association Lexicon](http://saifmohammad.com/WebPages/lexicons.html): adds the emotion associations of the words matching this lexicon.  
 * [SentiWordNet](http://sentiwordnet.isti.cnr.it): calculates positive and negative scores using SentiWordnet. We calculate a weighted average of the sentiment distributions of the synsets for word occurring in multiple synsets. The weights correspond to the reciprocal ranks of the senses in order to give higher weights to most popular senses. 
 * [Emoticons](https://github.com/fnielsen/afinn): calculates a positive and a negative score by aggregating the word associations provided by a list of emoticons. The list is taken from the [AFINN](https://github.com/fnielsen/afinn) project.
 * Negations: counts the number of negating words in the tweet.
 
3. __TweetToSentiStrengthFeatureVector__: calculates positive and negative scores for a tweet using [SentiStrength](http://sentistrength.wlv.ac.uk/). Disclaimer: __SentiStrength__ can only be used for academic purposes from within this package.
 
4. __TweetToEmbeddingsFeatureVector__: calculate a tweet-level feature representation using pre-trained word embeddings. The tweet vectors can be calculated using the following schemes: 
 * Average word embeddings.
 * Add word embeddings. 
 * Concatenation of first k embeddings. Dummy values are added if the tweet has less than k words. 
Note: A dummy word-embedding formed by zeroes is used if a word hat no embedding associated. 


### Tokenizers

1. __TweetNLPTokenizer__: a Twitter-specific String tokenizer based on the [CMU Tweet NLP tool](http://www.cs.cmu.edu/~ark/TweetNLP/) that can be used with the existing [StringWordToVector](http://weka.sourceforge.net/doc.dev/weka/filters/unsupervised/attribute/StringToWordVector.html) Weka filter. 

### Other Resources

1. __Datasets__: The package provides some tweets annotated by affective values in [ARFF](http://weka.wikispaces.com/ARFF) format in $HOME/wekafiles/packages/AffectiveTweets/data/ .
2. __Pre-trained Word-Embeddings__: The package provides two pre-trained word vectors trained with the [Word2Vec](https://code.google.com/archive/p/word2vec/) package. They are located in $HOME/wekafiles/packages/AffectiveTweets/resources/
 * __w2v.twitter.edinburgh.100d.csv.gz__: a toy example trained with a small collection of tweets.
 * __w2v.twitter.edinburgh10M.400d.csv.gz__: embeddings trained from 10 million tweets taken from the [Edinburgh corpus](http://www.aclweb.org/anthology/W/W10/W10-0513.pdf). The parameters were calibrated for classifying words into emotions. More info in this [paper](http://www.cs.waikato.ac.nz/~fjb11/publications/wi2016a.pdf).
 
## Installation

* Download the latest developer branch of [Weka](http://www.cs.waikato.ac.nz/ml/weka/snapshots/weka_snapshots.html) or build it from the SVN repository: 

```bash
svn co https://svn.cms.waikato.ac.nz/svn/weka/trunk/weka/
ant -f weka/build.xml exejar
```

* Install AffectiveTweets using the [WekaPackageManager](http://weka.wikispaces.com/How+do+I+use+the+package+manager%3F) 

```bash
java -cp weka/dist/weka.jar weka.core.WekaPackageManager -install-package https://github.com/felipebravom/AffectiveTweets/releases/download/1.0.0/AffectiveTweets1.0.0.zip
```

* (Optional) Install other useful packages for classification, regression and evaluation

```bash
java -cp weka/dist/weka.jar weka.core.WekaPackageManager -install-package LibLINEAR
java -cp weka/dist/weka.jar weka.core.WekaPackageManager -install-package LibSVM
java -cp weka/dist/weka.jar weka.core.WekaPackageManager -install-package RankCorrelation
```


## Examples
The package can be used from the command line or the Weka GUI.

1. In the following example we will train an SVM from LibLinear on the Sent140test dataset using pre-trained word embeddings as features. We use the FilteredClassfier that allows directly  passing a filter to the classifier.
 We use the MultiFilter filter to nest multiple filters. We will nest the TweetToEmbeddingsFeatureVector filter with the Reorder filter  that will discard useless String attributes and put the class label as the last attribute:

 ```bash
java -Xmx4G -cp weka/dist/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier -t $HOME/wekafiles/packages/AffectiveTweets/data/sent140test.arff -split-percentage 66 -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToEmbeddingsFeatureVector -I 1 -B $HOME/wekafiles/packages/AffectiveTweets/resources/w2v.twitter.edinburgh.100d.csv.gz -S 0 -K 15 -L -O\" -F \"weka.filters.unsupervised.attribute.Reorder -R 4-last,3\"" -W weka.classifiers.functions.LibLINEAR -- -S 1 -C 1.0 -E 0.001 -B 1.0 -L 0.1 -I 1000
```
Note: The -Xmx parameter allows incrementing the memory available for the Java virtual machine. It is strongly recommend to allocate as much memory as possible for large datasets or when calculating large dimensional features, such as word n-grams. More info at: http://weka.wikispaces.com/OutOfMemoryException .

2. The same can be done using the Weka GUI by running WEKA:

```bash
java -Xmx4G -jar weka/dist/weka.jar 
```


## Citation
There is no official publication related to this project yet. In the meanwhile please cite the following paper if using this package in an academic publication:

* F. Bravo-Marquez, E. Frank, S. M. Mohammad, and B. Pfahringer __Determining Word--Emotion Associations from Tweets by Multi-Label Classification__, In WI '16: Proceedings of the 2016 IEEE/WIC/ACM International Conference on Web Intelligence, Omaha, Nebraska, USA 2016. Pages 536-539. DOI:10.1109/WI.2016.90
