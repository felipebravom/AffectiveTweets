export WEKA_HOME=/home/felipe/wekafiles/
export WEKA_PATH=/home/felipe/weka-3-9-3/


java -cp $WEKA_HOME/packages/AffectiveTweets/AffectiveTweets.jar:$WEKA_PATH/weka.jar weka.core.converters.SemEvalToArff dataset/twitter-train-B.txt dataset/twitter-train-B.arff


java -cp $WEKA_HOME/packages/AffectiveTweets/AffectiveTweets.jar:$WEKA_PATH/weka.jar weka.core.converters.SemEvalToArff dataset/twitter-test-gold-B.tsv dataset/twitter-test-gold-B.arff


echo "Linear model using  n-grams  (n=1,2,3,4)." 
start=`date +%s`
java -Xmx4G -cp  $WEKA_PATH/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier -v -o -t dataset/twitter-train-B.arff -T dataset/twitter-test-gold-B.arff -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToSparseFeatureVector -E 5 -D 3 -I 0 -F -M 3 -R -G 0 -taggerFile $WEKA_HOME/packages/AffectiveTweets/resources/model.20120919 -wordClustFile $WEKA_HOME/packages/AffectiveTweets/resources/50mpaths2.txt.gz -Q 4 -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.Reorder -R 3-last,2\"" -S 1 -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000

end=`date +%s`
runtime=$((end-start))
echo time = $runtime



echo "Linear model using  n-grams + Bing Liu's Lexicon" 
start=`date +%s`
java -Xmx4G -cp $WEKA_PATH/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier  -v -o -t dataset/twitter-train-B.arff -T dataset/twitter-test-gold-B.arff -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToSparseFeatureVector -E 5 -D 3 -I 0 -F -M 3 -R -G 0 -taggerFile $WEKA_HOME/packages/AffectiveTweets/resources/model.20120919 -wordClustFile $WEKA_HOME/packages/AffectiveTweets/resources/50mpaths2.txt.gz -Q 4 -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToLexiconFeatureVector -D -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.Reorder -R 3-last,2\"" -S 1 -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000
end=`date +%s`
runtime=$((end-start))
echo time = $runtime




echo  "Linear model using  Bing Liu's Lexicon + SentiStrength"
start=`date +%s`
java -Xmx4G -cp $WEKA_PATH/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier -v -o -t dataset/twitter-train-B.arff -T dataset/twitter-test-gold-B.arff -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToSentiStrengthFeatureVector -L $WEKA_HOME/packages/AffectiveTweets/lexicons/SentiStrength/english -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToLexiconFeatureVector -D -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.Reorder -R 3-last,2\"" -S 1 -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000
end=`date +%s`
runtime=$((end-start))
echo time = $runtime


echo "Linear model using  n-grams + Bing Liu's Lexicon + SentiStrength"
start=`date +%s`
java -Xmx4G -cp $WEKA_PATH/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier  -v -o -t dataset/twitter-train-B.arff -T dataset/twitter-test-gold-B.arff -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToSparseFeatureVector -E 5 -D 3 -I 0 -F -M 3 -R -G 0 -taggerFile $WEKA_HOME/packages/AffectiveTweets/resources/model.20120919 -wordClustFile $WEKA_HOME/packages/AffectiveTweets/resources/50mpaths2.txt.gz -Q 4 -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToSentiStrengthFeatureVector -L $WEKA_HOME/packages/AffectiveTweets/lexicons/SentiStrength/english -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToLexiconFeatureVector -D -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.Reorder -R 3-last,2\"" -S 1 -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000
end=`date +%s`
runtime=$((end-start))
echo time = $runtime


echo "Linear model using  n-grams + SentiStrength + all lexicons"
start=`date +%s`
java -Xmx4G -cp $WEKA_PATH/weka.jar weka.Run weka.classifiers.meta.FilteredClassifier -v -o -t dataset/twitter-train-B.arff -T  dataset/twitter-test-gold-B.arff -F "weka.filters.MultiFilter -F \"weka.filters.unsupervised.attribute.TweetToSparseFeatureVector -E 5 -D 3 -I 0 -F -M 3 -R -G 0 -taggerFile $WEKA_HOME/packages/AffectiveTweets/resources/model.20120919 -wordClustFile $WEKA_HOME/packages/AffectiveTweets/resources/50mpaths2.txt.gz -Q 4 -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToSentiStrengthFeatureVector -L $WEKA_HOME/packages/AffectiveTweets/lexicons/SentiStrength/english -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.TweetToLexiconFeatureVector -F -D -R -A -N -P -J -H -Q -red -stan -stemmer weka.core.stemmers.NullStemmer -stopwords-handler \\\"weka.core.stopwords.Null \\\" -I 1 -U -tokenizer \\\"weka.core.tokenizers.TweetNLPTokenizer \\\"\" -F \"weka.filters.unsupervised.attribute.Reorder -R 3-last,2\"" -S 1 -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000
end=`date +%s`
runtime=$((end-start))
echo time = $runtime










