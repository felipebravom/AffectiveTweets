echo "Linear model using  n-grams  (n=1,2,3,4)." 
start=`date +%s`
python nltk_scikit_ngram.py 
end=`date +%s`
runtime=$((end-start))
echo time = $runtime


echo "Linear model using  n-grams + Bing Liu's Lexicon" 
start=`date +%s`
python nltk_scikit_ngram_liu.py 
end=`date +%s`
runtime=$((end-start))
echo time = $runtime


echo "Linear model using Bing Liu's Lexicon + Vader" 
start=`date +%s`
python nltk_scikit_liu_vader.py 
end=`date +%s`
runtime=$((end-start))
echo time = $runtime


echo "Linear model using n+grams + Bing Liu's Lexicon + Vader" 
start=`date +%s`
python nltk_scikit_ngram_liu_vader.py 
end=`date +%s`
runtime=$((end-start))
echo time = $runtime
