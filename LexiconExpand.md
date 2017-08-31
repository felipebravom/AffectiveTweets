# Lexicon Expansion Tutorial

1. Open a file.

2. Train Tweet Centroids


3. Label Centroids using a Lexicon


4. Train a classifier on labelled words and add predictions as new attributes

 ```bash
weka.filters.supervised.attribute.AddClassification -distribution -W "weka.classifiers.meta.FilteredClassifier -F \"weka.filters.unsupervised.attribute.RemoveType -T string\" -W weka.classifiers.functions.LibLINEAR -- -S 7 -C 1.0 -E 0.001 -B 1.0 -P -L 0.1 -I 1000"
'''

5. Remove useless attributes

weka.filters.unsupervised.attribute.Reorder -R 1476,1478,1479

6. Save Lexicon as ARFF