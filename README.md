<img src="logofinal.png" alt="alt text" width="250px" height="200px">

## About

AffectiveTweets is a [WEKA](http://www.cs.waikato.ac.nz/~ml/weka/) package for analysing emotion and sentiment  of English written tweets. 


The package implements WEKA filters for converting tweets contained in string attributes into feature vectors that can be fed into machine learning algorithms:

1. __TweetToSparseFeatureVector__: calculates sparse features from tweets. There are options provides for filtering out infrequent features and setting the weighting approach  (boolean or frequency based).
 * __Word n-grams__: extracts word n-grams from n=1 to maximum value. 
 * __Negations__: add a prefix to words occurring in negated contexts e.g., I don't like you => I don't NEG-like NEG-you. The prefixes only affect the word ngram features. The scope of negation finishes with the next punctuation mark.
 * __Character n-grams__: calculates character n-grams.
 * __POS tags__: tags tweets using the [CMU Tweet NLP tool](http://www.cs.cmu.edu/~ark/TweetNLP/). It creates a vector space model based on the sequence of POS tags allowing to set the maximum POS n-gram size.
 * __Brown clusters__: maps the words to Brown word clusters and creates a vector space model of lower dimensionality. It can be used with n-grams of word clusters. The word clusters are also taken from the [CMU Tweet NLP tool](http://www.cs.cmu.edu/~ark/TweetNLP/).

2. __TweetToLexiconFeatureVector__: calculates features from a tweet using several lexicons.
 * [MPQA](http://mpqa.cs.pitt.edu/lexicons/subj_lexicon): counts the number of positive and negative words from the MPQA subjectivity lexicon.
 * Bing Liu
 * [AFINN]( https://github.com/fnielsen/afinn): calculates a positive and negative score by aggregating the word associations provided by this lexicon.
 * NRC word emotion association lexicon
 * SentiWordNet
 * Twitter-specfic lexicons

3. __TweetToSentiStrengthFeatureVector__: calculates positive and negative scores for a tweet using [SentiStrength](http://sentistrength.wlv.ac.uk/). Disclaimer: SentiStrength can only be used for academic purposes from whitin this package.
 
4. __TweetToEmbeddingsFeatureVector__: calculate a tweet-level feature representation using pre-trained word embeddings. The tweet vectors can be calculated using the following schemes: 
 * Average word embeddings.
 * Add word embeddings. 
 * Concatenation of first k embeddings (using dummy values if the tweet has less than k words). 

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

1. You can use AffectiveTweets from the command line or the GUI.

 In the following example we will train an SVM from LibLinear on the Sent140test dataset using pretrained word embeddings as features. We use the FilteredClassfier that allows directly  passing a filter to the classifier.
 We use the MultiFilter filter to nest multiple filters. We will nest the TweetToEmbeddingsFeatureVector filter with the Reorder filter  that will discard useless String attributes and put the class label as the last attribute:

 ```bash
java -Xmx4G -cp weka/dist/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier -t $HOME/wekafiles/packages/AffectiveTweets/data/sent140test.arff -split-percentage 66 -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToEmbeddingsFeatureVector -I 1 -B $HOME/wekafiles/packages/AffectiveTweets/resources/w2v.twitter.edinburgh.100d.csv.gz -S 0 -K 15 -L -O\" -F \"weka.filters.unsupervised.attribute.Reorder -R 4-last,3\"" -W weka.classifiers.functions.LibLINEAR -- -S 1 -C 1.0 -E 0.001 -B 1.0 -L 0.1 -I 1000
```
Note: The -Xmx parameter allows incrementing the memory available for the Java virtual machine. It is strongly recommend to allocate as much memory as possible for large datasets or when calculating large dimensional features, such as word  ngrams. More info at: http://weka.wikispaces.com/OutOfMemoryException .

2. The same can be done using the Weka GUI by running WEKA:

```bash
java -Xmx4G -jar weka/dist/weka.jar 
```


