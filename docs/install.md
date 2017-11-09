
## Installing  Weka
Download the latest stable [version](http://www.cs.waikato.ac.nz/ml/weka/downloading.html) or the  developer [branch](http://www.cs.waikato.ac.nz/ml/weka/snapshots/weka_snapshots.html) of Weka.
You can also build the developer branch from the SVN repository: 

```bash
# checkout weka 
svn co https://svn.cms.waikato.ac.nz/svn/weka/trunk/weka/
# build weka using apache ant
ant -f weka/build.xml exejar
```

## Installing AffectiveTweets

Install AffectiveTweets1.0.0 using the [WekaPackageManager](http://weka.wikispaces.com/How+do+I+use+the+package+manager%3F): 

```bash
java -cp $WEKA_PATH/weka.jar weka.core.WekaPackageManager -install-package AffectiveTweets
```

In order to properly run our [examples](examples), we recommend installing the newest version of the package v.1.0.1 (not officially released yet) as follows: 

```bash
# Uninstall the previous version of AffectiveTweets
java -cp $WEKA_PATH/weka.jar weka.core.WekaPackageManager -uninstall-package AffectiveTweets
# Install the newest development version:
java -cp $WEKA_PATH/weka.jar weka.core.WekaPackageManager -install-package https://github.com/felipebravom/AffectiveTweets/releases/download/1.0.1/AffectiveTweets1.0.1.zip
```

## Building AffectiveTweets
You can also build the package from the repository's version to try the most recent features. This is very useful if you want to contribute.

```bash
# clone the repository
git clone https://github.com/felipebravom/AffectiveTweets.git
cd AffectiveTweets

# Download additional files
wget https://github.com/felipebravom/AffectiveTweets/releases/download/1.0.1/extra.zip
unzip extra.zip

# Build the package using apache ant
ant -f build_package.xml make_package

# Install the built package 
java -cp $WEKA_PATH/weka.jar weka.core.WekaPackageManager -install-package dist/AffectiveTweets.zip


```

## Other Useful Packages

We recommend installing other useful packages for classification, regression and evaluation:

* [LibLinear](https://www.csie.ntu.edu.tw/~cjlin/liblinear/): This package is required for running the [examples](examples).
```bash
java -cp $WEKA_PATH/weka.jar weka.core.WekaPackageManager -install-package LibLINEAR
```

* [LibSVM](https://www.csie.ntu.edu.tw/~cjlin/libsvm/)

```bash
java -cp $WEKA_PATH/weka.jar weka.core.WekaPackageManager -install-package LibSVM
```

* [RankCorrelation](https://github.com/felipebravom/RankCorrelation)

```bash
java -cp $WEKA_PATH/weka.jar weka.core.WekaPackageManager -install-package RankCorrelation
```

* [Snowball-stemmers](https://github.com/fracpete/snowball-stemmers-weka-package): This package allows using the Porter stemmer as well as other Snowball stemmers.
```bash
java -cp $WEKA_PATH/weka.jar weka.core.WekaPackageManager -install-package https://github.com/fracpete/snowball-stemmers-weka-package/releases/download/v1.0.1/snowball-stemmers-1.0.1.zip
```




* The [WekaDeepLearning4j](https://deeplearning.cms.waikato.ac.nz/) package can be installed for training deep neural networks and word embeddings. 


