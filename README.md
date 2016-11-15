# AffectiveTweets
AffectiveTweets is an open source project based on [WEKA](http://www.cs.waikato.ac.nz/~ml/weka/) for analysing emotion and sentiment from  tweets. 


The package implements three WEKA filters for converting tweets from string format into feature vectors:

1. __TweetToSparseFeatureVector__: calculates sparse feature from a tweet. 
 * Word N-grams
 * Character N-grams
 * POS tags (with N-Gram sequences)
 * Brown clusters (with N-gram sequences). 
There are options provides for filtering out infrequent features and setting the weighting approach of the sparse features (boolean or frequency based).
2. __TweetToLexiconFeatureVector__: calculates features from a tweet using several lexicons.
 * MPQA
 * Bing Liu
 * NRC Emotion
 * Twitter-specfic lexicons
 
3. __TweetToEmbeddingsFeatureVector__: calculate a tweet-leve feature representation using pre-trained word embeddings as input. 
* Average word embedding.
* Concatenation of first k embeddings (using dummy values if the tweet is shorter than k). 
