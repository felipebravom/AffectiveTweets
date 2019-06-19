<img src="img/logo.png" alt="alt text" width="30%" height="30%"> 

## About

[AffectiveTweets](http://weka.sourceforge.net/packageMetaData/AffectiveTweets/index.html) is a [WEKA](http://www.cs.waikato.ac.nz/~ml/weka/) package for analyzing emotion and sentiment of  tweets.  The source code is hosted on [Github](https://github.com/felipebravom/AffectiveTweets).

The package implements WEKA filters for calculating state-of-the-art affective analysis features from tweets that can be fed into machine learning algorithms. Many of these features were drawn from the [NRC-Canada System](http://saifmohammad.com/WebPages/NRC-Canada-Sentiment.htm). It also implements methods for building affective lexicons and distant supervision methods for training affective models from unlabelled tweets.



Description about the filters, installation instructions, and examples are given below.



## Official Baseline System

The package was made available as the official baseline system for the [WASSA-2017](http://optima.jrc.it/wassa2017/) Shared Task on Emotion Intensity [(EmoInt)](http://saifmohammad.com/WebPages/EmotionIntensity-SharedTask.html) and for [SemEval-2018](http://alt.qcri.org/semeval2018/) Task 1: [Affect in Tweets](http://www.saifmohammad.com/WebPages/affectintweets.htm). 


Five participating teams used AffectiveTweets in WASSA-2017 to generate feature vectors, including the teams that eventually ranked first, second, and third. For SemEval-2018, the package was used by 15 teams.



## Relevant Papers

The most relevant papers on which this package is based are:


 * [Sentiment Analysis of Short Informal Texts](http://saifmohammad.com/WebDocs/NRC-Sentiment-JAIR-2014.pdf). Svetlana Kiritchenko, Xiaodan Zhu and Saif Mohammad. Journal of Artificial Intelligence Research, volume 50, pages 723-762, August 2014. [BibTeX](http://saifmohammad.com/WebDocs/JAIR14-bibtex.txt)
 * [Meta-Level Sentiment Models for Big Social Data Analysis](http://www.sciencedirect.com/science/article/pii/S0950705114002068). F. Bravo-Marquez, M. Mendoza and B. Poblete. Knowledge-Based Systems Volume 69, October 2014, Pages 86–99. [BibTex](http://dblp.uni-trier.de/rec/bib2/journals/kbs/Bravo-MarquezMP14.bib)

 * [Stance and sentiment in tweets](http://saifmohammad.com/WebDocs/1605.01655v1.pdf). Saif M. Mohammad, Parinaz Sobhani, and Svetlana Kiritchenko. 2017. Special Section of the ACM Transactions on Internet Technology on Argumentation in Social Media 17(3). [BibTeX](http://saifmohammad.com/WebPages/Abstracts/stance-toit.bib.txt)
 * [Sentiment strength detection for the social Web](http://dl.acm.org/citation.cfm?id=2336261). Thelwall, M., Buckley, K., & Paltoglou, G. (2012). Journal of the American Society for Information Science and Technology, 63(1), 163-173. [BibTex](http://dblp.uni-trier.de/rec/bib2/journals/jasis/ThelwallBP12.bib)





## Citation
Please cite the following paper if using this package in an academic publication:

* F. Bravo-Marquez, E. Frank, B. Pfahringer, and S. M. Mohammad [AffectiveTweets: a WEKA Package for Analyzing Affect in Tweets](http://jmlr.org/papers/v20/18-450.html), In *Journal of Machine Learning Research* Volume 20(92), pages 1−6, 2019. ([pdf](https://felipebravom.com/publications/jmlr2019.pdf))

You are also welcome to cite a previous publication describing the package:

* S. M. Mohammad and F. Bravo-Marquez [Emotion Intensities in Tweets](http://anthology.aclweb.org/S/S17/S17-1007.pdf), In **Sem '17: Proceedings of the sixth joint conference on lexical and computational semantics (\*Sem)*, August 2017, Vancouver, Canada. ([pdf](https://felipebravom.com/publications/starsem2017.pdf))

You should also cite the papers describing any of the lexicons or resources you are using with this package. 

* Here is the [BibTex](fullBio.bib.txt) entry for the package along with the entries for the resources listed below. 

* Here is the [BibTex](shortBio.bib.txt) entry just for the package.


The individual references for each resource can be found through the links provided below.

### Filters



#### Tweet-level Filters 


1. __TweetToSparseFeatureVector__: calculates sparse features, such as word and character n-grams from tweets. There are parameters for filtering out infrequent features e.g., (n-grams occurring in less than *m* tweets) and for setting the weighting approach  (boolean or frequency based).
	* __Word n-grams__: extracts word n-grams from *n*=1 to a maximum value. 
		* __Negations__: add a prefix to words occurring in negated contexts, e.g., I don't like you => I don't NEG-like NEG-you. The prefixes only affect word n-gram features. The scope of negation finishes with the next punctuation expression *([\\.|,|:|;|!|\\?]+)* .
	* __Character n-grams__: calculates character n-grams.
	* __POS tags__: tags tweets using the [CMU Tweet NLP tool](http://www.cs.cmu.edu/~ark/TweetNLP/), and creates a vector space model based on the sequence of POS tags. [BibTex](http://dblp.uni-trier.de/rec/bib2/conf/acl/GimpelSODMEHYFS11.bib)
	* __Brown clusters__: maps the words in a tweet to Brown word clusters and creates a low-dimensional vector space model. It can be used with n-grams of word clusters. The word clusters are also taken from the [CMU Tweet NLP tool](http://www.cs.cmu.edu/~ark/TweetNLP/).


2. __TweetToLexiconFeatureVector__: calculates features from a tweet using several lexicons.
	* [MPQA](http://mpqa.cs.pitt.edu/lexicons/subj_lexicon): counts the number of positive and negative words from the MPQA subjectivity lexicon. [BibTex](http://dblp.uni-trier.de/rec/bib2/conf/naacl/WilsonWH05.bib)
	* [Bing Liu](https://www.cs.uic.edu/~liub/FBS/sentiment-analysis.html#lexicon): counts the number of positive and negative words from the Bing Liu lexicon. [BibTex](http://dblp.uni-trier.de/rec/bib2/conf/kdd/HuL04.bib)
	* [AFINN](https://github.com/fnielsen/afinn): calculates positive and negative variables by aggregating the positive and negative word scores provided by this lexicon. [BibTex](http://dblp.uni-trier.de/rec/bib2/conf/msm/Nielsen11.bib)
	* [Sentiment140](http://saifmohammad.com/WebPages/lexicons.html#NRCTwitter): calculates positive and negative variables by aggregating the positive and negative word scores provided by this lexicon created with tweets annotated by emoticons. [BibTex](http://saifmohammad.com/WebDocs/JAIR14-bibtex.txt)
	* [NRC Hashtag Sentiment lexicon](http://saifmohammad.com/WebPages/lexicons.html#NRCTwitter): calculates positive and negative variables by aggregating the positive and negative word scores provided by this lexicon created with tweets annotated with emotional hashtags. [BibTex](http://saifmohammad.com/WebDocs/JAIR14-bibtex.txt) 
	* [NRC Word-Emotion Association Lexicon](http://saifmohammad.com/WebPages/NRC-Emotion-Lexicon.htm): counts the number of words matching each emotion from this lexicon. [BibTex](http://saifmohammad.com/WebPages/Abstracts/crowdemo.bib.txt)
	* [NRC-10 Expanded](http://www.cs.waikato.ac.nz/ml/sa/lex.html#emolextwitter): adds the emotion associations of the words matching the Twitter Specific expansion of the NRC Word-Emotion Association Lexicon. [BibTex](http://dblp.uni-trier.de/rec/bib2/conf/webi/Bravo-MarquezFM16.bib)
	* [NRC Hashtag Emotion Association Lexicon](http://saifmohammad.com/WebPages/lexicons.html#HashEmo): adds the emotion associations of the words matching this lexicon. [BibTex](http://saifmohammad.com/WebPages/hashtagPersonality-bib.html)  
	* [SentiWordNet](http://sentiwordnet.isti.cnr.it): calculates positive and negative scores using SentiWordnet. We calculate a weighted average of the sentiment distributions of the synsets for word occurring in multiple synsets. The weights correspond to the reciprocal ranks of the senses in order to give higher weights to most popular senses. [BibTex](http://dblp.uni-trier.de/rec/bib2/conf/lrec/BaccianellaES10.bib) 
	* [Emoticons](https://github.com/fnielsen/afinn): calculates a positive and a negative score by aggregating the word associations provided by a list of emoticons. The list is taken from the [AFINN](https://github.com/fnielsen/afinn) project.
	* Negations: counts the number of negating words in the tweet.

 3. __TweetToInputLexiconFeatureVector__: calculates features from a tweet using a given list of affective lexicons, where each lexicon is represented as an [ARFF](https://weka.wikispaces.com/ARFF) file.  The features are calculated by adding or counting the affective associations of the words matching the given lexicons. All numeric and nominal attributes from each lexicon are considered. Numeric scores are added and nominal are counted. The [NRC-Affect-Intensity](http://www.saifmohammad.com/WebPages/AffectIntensity.htm) lexicon is used by deault.   [BibTex](http://dblp.uni-trier.de/rec/bib2/journals/corr/Mohammad17.bib)

4. __TweetToSentiStrengthFeatureVector__: calculates positive and negative sentiment strengths for a tweet using [SentiStrength](http://sentistrength.wlv.ac.uk/). Disclaimer: __SentiStrength__ can only be used for academic purposes from within this package. [BibTex](http://dblp.uni-trier.de/rec/bib2/journals/jasis/ThelwallBP12.bib)

5. __TweetToEmbeddingsFeatureVector__: calculates a tweet-level feature representation using pre-trained word embeddings. A dummy word-embedding formed by zeroes is used for word with no corresponding embedding. The tweet vectors can be calculated using the following schemes: 
	* Average word embeddings. 
	* Add word embeddings. 
	* Concatenation of first *k* embeddings. Dummy values are added if the tweet has less than *k* words. 
6. __TweetNLPPOSTagger__:  runs the Twitter-specific POS tagger from the CMU TweetNLP library on the given tweets. POS tags are prepended to the tokens. 


#### Word-level Filters


1. __PMILexiconExpander__: calculates the Pointwise Mutual Information (PMI) semantic orientation for each word in a corpus of tweets annotated by sentiment. The score is calculated by subtracting the PMI of the  target  word  with  a negative  sentiment from the PMI of the target word with a positive sentiment. This is a supervised filter.  [BibTex](http://dblp.uni-trier.de/rec/bib2/conf/acl/Turney02.bib) 


2. __TweetCentroid__:  calculates word distributional vectors from a corpus of unlabelled tweets by treating them as the centroid of the tweet vectors in which they appear. The vectors can be labelled using an affective lexicon to train a word-level affective classifier. This classifier can be used to expand the original lexicon.  [BibTex](http://dblp.uni-trier.de/rec/bib2/conf/sigir/Bravo-MarquezFP15.bib), [original paper](http://www.cs.waikato.ac.nz/~fbravoma/publications/sigir15.pdf)

3. __LabelWordVectors__: labels word vectors with an input lexicon in arff format. This filter is useful for training word-level affective classifiers.

### Distant Supervision Filters

1. __ASA__:  Annotate-Sample-Average (ASA) is a lexicon-based distant supervision method for training polarity classifiers in Twitter in the absence of labelled data. It takes a collection of unlabelled tweets and a polarity lexicon in arff format and creates synthetic labelled instances. Each labelled instance is created by sampling with replacement a number of tweets containing at least one word from the lexicon with the desired polarity, and averaging the feature vectors of the sampled tweets.  [BibTex](http://dblp.uni-trier.de/rec/bib2/conf/ecai/Bravo-MarquezFP16.bib), [original paper](http://www.cs.waikato.ac.nz/~fbravoma/publications/ecai2016.pdf)


1. __PTCM__:  The Partitioned Tweet Centroid Model (PTCM) is an adaption of the TweetCentroidModel for distant supervision.  As tweets and words are represented by the same feature vectors, a word-level classifier trained from a polarity lexicon and a corpus of unlabelled tweets can be used for classifying the sentiment of tweets represented by sparse feature vectors.  In other words, the labelled word vectors correspond to lexicon-annotated training data for message-level polarity classification.
The model includes a simple modification to the tweet centroid model for increasing the number of labelled instances, yielding *partitioned tweet centroids*.  This modification is based on partitioning the tweets associated with each word into smaller disjoint subsets of a fixed size. The method calculates one centroid per partition, which is labelled according to the lexicon.
[BibTex](http://dblp.uni-trier.de/rec/bib2/conf/webi/Bravo-MarquezFP16.bib), [original paper](https://www.cs.waikato.ac.nz/~fbravoma/publications/wi2016t.pdf)


1. __LexiconDistantSupervision__: This is the most popular distant supervision approach for Twitter sentiment analysis. It takes a collection of unlabelled tweets and a polarity lexicon in arff format of positive and negative tokens. If a word from the lexicon is found, the tweet is labelled with the word's polarity. Tweets with both positive and negative words are discarded. The word used for labelling the tweet can be removed from the content. Emoticons are used as the default lexicon. [original paper](http://cs.stanford.edu/people/alecmgo/papers/TwitterDistantSupervision09.pdf)

### Tokenizers

1. __TweetNLPTokenizer__: a Twitter-specific String tokenizer based on the [CMU Tweet NLP tool](http://www.cs.cmu.edu/~ark/TweetNLP/) that can be used with the existing [StringWordToVector](http://weka.sourceforge.net/doc.dev/weka/filters/unsupervised/attribute/StringToWordVector.html) Weka filter. 

### Other Resources

1. __Datasets__: The package provides some tweets annotated by affective values in gzipped [ARFF](http://weka.wikispaces.com/ARFF) format in $WEKA_HOME/packages/AffectiveTweets/data/. The default location for $WEKA_HOME is $HOME/wekafiles. 
2. __Affective Lexicons__: The package provides affective lexicons in [ARFF](http://weka.wikispaces.com/ARFF) format. These lexicons are located in $WEKA_HOME/packages/AffectiveTweets/lexicons/arff_lexicons/ and can be used with the  __TweetToInputLexiconFeatureVector__ filter.

3. __Pre-trained Word-Embeddings__: The package provides a file with pre-trained word vectors trained with the [Word2Vec](https://code.google.com/archive/p/word2vec/) tool in gzip compressed format. It is a tab separated file with the word in last column located in $WEKA_HOME/packages/AffectiveTweets/resources/w2v.twitter.edinburgh.100d.csv.gz. However, this is a toy example trained from a small collection of tweets. We recommend downloading [w2v.twitter.edinburgh10M.400d.csv.gz](https://github.com/felipebravom/AffectiveTweets/releases/download/1.0.0/w2v.twitter.edinburgh10M.400d.csv.gz), which provides  embeddings trained from 10 million tweets taken from the [Edinburgh corpus](http://www.aclweb.org/anthology/W/W10/W10-0513.pdf). The parameters were calibrated for classifying words into emotions. More info in this [paper](http://www.cs.waikato.ac.nz/~fjb11/publications/wi2016a.pdf).


## Documentation

The Java documentation is available [here](https://felipebravom.github.io/AffectiveTweets/doc/index.html).



## Team

### Main Developer
 * [Felipe Bravo-Marquez](https://felipebravom.com/)

## Contributors
 * [Saif Mohammad](http://saifmohammad.com/)
 * [Eibe Frank](http://www.cs.waikato.ac.nz/~eibe/)
 * [Bernhard Pfahringer](https://www.cs.waikato.ac.nz/~bernhard/)



## Contact
 * Email: fbravo at dcc.uchile.cl
 * If you have questions about Weka please refer to the Weka [mailing list](https://list.waikato.ac.nz/mailman/listinfo/wekalist). 



