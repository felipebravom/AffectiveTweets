<img src="logofinal.png" alt="alt text" width="250px" height="200px">

## About

AffectiveTweets is a [WEKA](http://www.cs.waikato.ac.nz/~ml/weka/) package for analysing emotion and sentiment  of English written tweets. 


The package implements WEKA filters for converting tweets contained in string attributes into feature vectors that can be fed into machine learning algorithms:

1. __TweetToSparseFeatureVector__: calculates sparse features from tweets. There are options provides for filtering out infrequent features and setting the weighting approach  (boolean or frequency based).
 * Word N-grams
 * Negations
 * Character N-grams
 * POS tags (with N-Gram sequences)
 * Brown clusters (with N-gram sequences). 

2. __TweetToLexiconFeatureVector__: calculates features from a tweet using several lexicons.
 * MPQA
 * Bing Liu
 * AFINN
 * NRC word emotion association lexicon
 * SentiWordNet
 * Twitter-specfic lexicons

3. __TweetToSentiStrengthFeatureVector__: calculates positive and negative scores for a tweet using [SentiStrength](http://sentistrength.wlv.ac.uk/). Disclaimer: SentiStrength can only be used for academic purposes from whitin this package.
 
4. __TweetToEmbeddingsFeatureVector__: calculate a tweet-level feature representation using pre-trained word embeddings. The tweet vectors can be calculated using the following schemes: 
 * Average word embeddings.
 * Concatenation of first k embeddings (using dummy values if the tweet has less than k words). 

## Installation

* Install and build the newest version of WEKA from the SVN repository: 

```bash
svn co https://svn.cms.waikato.ac.nz/svn/weka/trunk/weka/
ant -f weka/build.xml exejar
```

* Install AffectiveTweets using the [WekaPackageManager](http://weka.wikispaces.com/How+do+I+use+the+package+manager%3F) 

```bash
java -cp weka/dist/weka.jar weka.core.WekaPackageManager -install-package https://github.com/felipebravom/AffectiveTweets/releases/download/1.0.0/AffectiveTweets1.0.0.zip
```

* Install other useful packages for classification, regression and evaluation

```bash
java -cp weka/dist/weka.jar weka.core.WekaPackageManager -install-package LibLINEAR
java -cp weka/dist/weka.jar weka.core.WekaPackageManager -install-package LibSVM
java -cp weka/dist/weka.jar weka.core.WekaPackageManager -install-package RankCorrelation
```


## Use

You can use AffectiveTweets from the command line or the GUI.

In the following example we will train an SVM from LibLinear on the Sent140test dataset using pretrained word embeddings as features. We use the FilteredClassfier that allows passing a filter to classifier.
We use the MultiFilter filter to nest multiple filters. In this we will nest the TweetToEmbeddingsFeatureVector filter with the Reorder filter that will discard useless String attributes and put the class label as the last attribute.
Moreover, we recommend incrementing the the memory available for the Java virtual machine using the -Xmx parameter:

```bash
java -Xmx4G -cp weka/dist/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier -t $HOME/wekafiles/packages/AffectiveTweets/data/sent140test.arff -split-percentage 66 -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToEmbeddingsFeatureVector -I 1 -B $HOME/wekafiles/packages/AffectiveTweets/resources/w2v.twitter.edinburgh.100d.csv.gz -S 0 -K 15 -L -O\" -F \"weka.filters.unsupervised.attribute.Reorder -R 4-last,3\"" -W weka.classifiers.functions.LibLINEAR -- -S 1 -C 1.0 -E 0.001 -B 1.0 -L 0.1 -I 1000
```
Note: The -Xmx parameter allows incrementing the memory available for the Java virtual machine. It is strongly recommend to allocate as much memory as possible for large datasets or for  calculate large dimensional features, such as word  ngrams. More info at: http://weka.wikispaces.com/OutOfMemoryException

The same can be done using the Weka GUI by running WEKA:

```bash
java -Xmx4G -jar weka/dist/weka.jar 
```


