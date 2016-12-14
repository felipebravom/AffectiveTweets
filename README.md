<img src="logofinal.png" alt="alt text" width="250px" height="200px">

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
