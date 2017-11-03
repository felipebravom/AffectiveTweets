
* Download the latest stable [version](http://www.cs.waikato.ac.nz/ml/weka/downloading.html) or the  developer [branch](http://www.cs.waikato.ac.nz/ml/weka/snapshots/weka_snapshots.html) of Weka.
You can also build the developer branch from the SVN repository: 

```bash
svn co https://svn.cms.waikato.ac.nz/svn/weka/trunk/weka/
ant -f weka/build.xml exejar
```

* Install AffectiveTweets using the [WekaPackageManager](http://weka.wikispaces.com/How+do+I+use+the+package+manager%3F): 

```bash
java -cp weka.jar weka.core.WekaPackageManager -install-package AffectiveTweets
```

* We recommend installing the newest version of the package (not officially released yet) as follows: 

```bash
# Uninstall the previous version of AffectiveTweets
java -cp weka.jar weka.core.WekaPackageManager -uninstall-package AffectiveTweets
# Install the newest development version:
java -cp weka.jar weka.core.WekaPackageManager -install-package https://github.com/felipebravom/AffectiveTweets/releases/download/1.0.1/AffectiveTweets1.0.1.zip
```

* (Optional) Install other useful packages for classification, regression and evaluation:

```bash
java -cp weka.jar weka.core.WekaPackageManager -install-package LibLINEAR
java -cp weka.jar weka.core.WekaPackageManager -install-package LibSVM
java -cp weka.jar weka.core.WekaPackageManager -install-package RankCorrelation
```