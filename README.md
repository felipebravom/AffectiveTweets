# AffectiveTweets
AffectiveTweets is an open source project based on [WEKA](http://www.cs.waikato.ac.nz/~ml/weka/) for analysing emotion and sentiment from  tweets. 


The package implements three WEKA filters for converting tweets from string format into feature vectors:

1. __TweetToSparseFeatureVector__: calculates sparse feature from a tweets: Word N-grams, Character N-grams, POS tags (with N-Gram sequences), and Brown clusters (with N-gram sequences). There will be an option for filtering out infrequent features and setting the weighting approach (boolean or frequency based)
2. __TweetToLexiconFeatureVector__: calculates features from a tweet using several lexicons.
3. __TweetToEmbeddingsFeatureVector__: calculate features using pre-trained embeddings e.g., average embeddings, concatenation of first k embeddings (using dummy values if the tweet is shorter than k). 
