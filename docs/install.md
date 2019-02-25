
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

Install AffectiveTweets1.0.2 using the [WekaPackageManager](https://waikato.github.io/weka-wiki/packages/manager/): 

```bash
java -cp $WEKA_PATH/weka.jar weka.core.WekaPackageManager -install-package AffectiveTweets
```

Make sure you have installed version 1.0.2 to run the [examples](examples).

In case of having problems with the Weka packages repository, install the package as follows:

```bash
java -cp $WEKA_PATH/weka.jar weka.core.WekaPackageManager -install-package https://github.com/felipebravom/AffectiveTweets/releases/download/1.0.2/AffectiveTweets1.0.2.zip
```

## Building AffectiveTweets
You can also build the package from the Github version using the project’s [build-package.xml](https://github.com/felipebravom/AffectiveTweets/blob/master/build_package.xml) file  with the [ant](http://ant.apache.org/) build tool. This is very useful if you want to modify the code or [contribute](../contribute) with a new feature. 



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

## Testing AffectiveTweets
The software can be tested using Junit test cases.  The package must either be installed or  JVM must be started in AffectiveTweets directory.

```bash
# run all tests
ant -f build_package.xml run_tests_all 
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
java -cp $WEKA_PATH/weka.jar weka.core.WekaPackageManager -install-package snowball-stemmers
```




* The [WekaDeepLearning4j](https://deeplearning.cms.waikato.ac.nz/) package can be installed for training deep neural networks and word embeddings. 


