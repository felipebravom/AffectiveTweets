#### Create a Lexicon of sentiment words using the TweetCentroid method
The TweetCentroid method is a word-level classification model for automatically generating a Twitter-specific opinion lexicon from a corpus of unlabelled tweets. The tweets from the corpus are represented by two vectors: a bag-of-words vector and a semantic vector based on word-clusters trained with the Brown clustering algorithm. The tweet centroid model is a distributional representation for words in which they are treated  as the centroid of the tweet vectors in which they appear. The lexicon generation is conducted by training a word-level classifier using these centroids to form the instance space and a seed lexicon to label the training instances. 


* Open in the preprocess panel the sent140train.arff.gz. This is a large corpus, so make sure to increase the heap size when running Weka.

2. Train word vectors using the tweet centroid model using the TweetCentroid filter as follows: 

```bash
weka.filters.unsupervised.attribute.TweetCentroid -M 10 -N 10 -W -C -I 1 -P WORD- -Q CLUST- -L -H $HOME/wekafiles/packages/AffectiveTweets/resources/50mpaths2.txt.gz -R -T $HOME/wekafiles/packages/AffectiveTweets/resources/stopwords.txt
```

3. Label the resulting word vectors with a seed lexicon in arff format using the LabelWordVector Filter:

```bash
weka.filters.unsupervised.attribute.LabelWordVector -lexicon_evaluator "affective.core.ArffLexiconWordLabeller -lexiconFile /Users/admin/wekafiles/packages/AffectiveTweets/lexicons/arff_lexicons/metaLexEmo.arff -B MetaLexEmo -A 1" -U -I last
'''

4. Train a classifier on labelled words and add predictions as new attributes

```bash
weka.filters.supervised.attribute.AddClassification -remove-old-class -distribution -W "weka.classifiers.meta.FilteredClassifier -F \"weka.filters.unsupervised.attribute.RemoveType -T string\" -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000"
```

5. Remove useless attributes

```bash
weka.filters.unsupervised.attribute.Remove -V -R 979-last
```

6. Save Lexicon as ARFF