

# Contributing Guidelines

New contributors are more than welcome. If you want to contribute just fork the project and send a pull request with your changes. 

## Weka Filter

AffectiveTweets methods extend the Weka [Filter](https://github.com/Waikato/weka-trunk/blob/master/weka/src/main/java/weka/filters/Filter.java) class, particularly the [SimpleBatchFilter](https://github.com/Waikato/weka-trunk/blob/master/weka/src/main/java/weka/filters/SimpleBatchFilter.java) class.   

Please read the instructions for implementing a Weka filter from [here](https://waikato.github.io/weka-wiki/writing_filter/) before continuing. 



##  Implementing a new AffectiveTweets Filter



We will show how to implement a simple filter that adds a new numeric attribute to the given dataset. This attribute will count the number of times the words from a given list occur in a given tweet. The list is given as comma separated string.

New filters can extend the [TweetToFeatureVector](https://github.com/felipebravom/AffectiveTweets/blob/master/src/main/java/weka/filters/unsupervised/attribute/TweetToFeatureVector.java) abstract class to inherit tokenization and many other preprocessing functionalities useful for sentiment analysis of tweets (e.g., reduce repeated letters, standardize URLs) . 



```java
/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *    TweetToWordListCountFeatureVector.java
 *    Copyright (C) 1999-2019 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute;



import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.Arrays;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionMetadata;
import weka.core.SparseInstance;



/**
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 */


public class TweetToWordListCountFeatureVector extends TweetToFeatureVector {

	/** For serialization.  */
	private static final long serialVersionUID = -573366510055859430L;

	/** The given word list as a comma separated string. */
	public  String wordList = "love,happy,great";



	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */	
	@Override
	public String globalInfo() {
		return "A simple filter that counts occurrences of words from a given list.";
	}




	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#determineOutputFormat(weka.core.Instances)
	 */
	@Override
	protected Instances determineOutputFormat(Instances inputFormat)
			throws Exception {

		ArrayList<Attribute> att = new ArrayList<Attribute>();

		// Adds all attributes of the inputformat
		for (int i = 0; i < inputFormat.numAttributes(); i++) {
			att.add(inputFormat.attribute(i));
		}

		// adds the new attribute
		att.add(new Attribute("wordListCount"));
		
		Instances result = new Instances(inputFormat.relationName(), att, 0);

		// set the class index
		result.setClassIndex(inputFormat.classIndex());

		return result;
	}



	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#process(weka.core.Instances)
	 */
	@Override
	protected Instances process(Instances instances) throws Exception {


		// set upper value for text index
		m_textIndex.setUpper(instances.numAttributes() - 1);

		Instances result = getOutputFormat();


		// reference to the content of the message, users index start from zero
		Attribute attrCont = instances.attribute(this.m_textIndex.getIndex());



		for (int i = 0; i < instances.numInstances(); i++) {	

			// copy all attribute values from the original dataset
			double[] values = new double[result.numAttributes()];
			for (int n = 0; n < instances.numAttributes(); n++)
				values[n] = instances.instance(i).value(n);

			
			String content = instances.instance(i).stringValue(attrCont);
			// tokenize the content
			List<String> words = affective.core.Utils.tokenize(content, this.toLowerCase, this.standarizeUrlsUsers, this.reduceRepeatedLetters, this.m_tokenizer,this.m_stemmer,this.m_stopwordsHandler);

			// convert the list of words into a HashSet
			Set<String> wordSet = new HashSet<String>(Arrays.asList(wordList.split(",")));
			
			// count all the occurrences of words from the list
			int wordCounter = 0;			
			for(String word:words){
				if(wordSet.contains(word))
					wordCounter++;
			}
			
			
			// add the value to the last attribute
			values[values.length - 1] = wordCounter;
			

			Instance inst = new SparseInstance(1, values);

			inst.setDataset(result);

			// copy possible strings, relational values...
			copyValues(inst, false, instances, result);

			result.add(inst);

		}

		return result;
	}




	/**
	 * Main method for testing this class.
	 *
	 * @param args should contain arguments to the filter: use -h for help
	 */		
	public static void main(String[] args) {
		runFilter(new TweetToWordListCountFeatureVector(), args);
	}

	
	// OptionMetada allows setting parameters from within the command-line interface
	@OptionMetadata(displayName = "wordlist",
			description = "The list with the words to count separated by a comma symbol.",
			commandLineParamName = "wordlist", commandLineParamSynopsis = "-wordlist <string>",
			displayOrder = 6)
	public String getWordList() {
		return wordList;
	}
	public void setWordList(String wordList) {
		this.wordList = wordList;
	}



}



```



One way to use this new filter class from within Weka, assuming the  source code of the class is in the appropriate subfolder of the [src](https://github.com/felipebravom/AffectiveTweets/tree/master/src)  folder of the AffectiveTweets project, is to [rebuild](../install/#building-affectivetweets) and reinstall the  AffectiveTweets package by using the project’s [build-package.xml](https://github.com/felipebravom/AffectiveTweets/blob/master/build_package.xml) file  with the [ant](http://ant.apache.org/) build tool.







### Implementing a JUnit Test

To test the new filter we need to implement a [JUnit](https://junit.org/) test.  A new filter test can extend  [AbstractFilterTest](https://github.com/Waikato/weka-trunk/blob/master/weka/src/test/java/weka/filters/AbstractFilterTest.java), which can be found in the Weka source code repository, as shown below: 





```java
/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright (C) 2019 University of Waikato, Hamilton, New Zealand
 */

package weka.filters.unsupervised.attribute;

import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.filters.AbstractFilterTest;
import weka.filters.Filter;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;

/**
 * Tests TweetToWordListCountFeatureVectorTest. Run from the command line with: <p/>
 * java weka.filters.unsupervised.attribute.TweetToWordListCountFeatureVectorTest
 * <p> 
 * AffectiveTweets package must either be installed or
 * JVM must be started in AffectiveTweets directory.
 * <p>
 * @author FracPete and eibe
 * @version $Revision: 9568 $
 */
public class TweetToWordListCountFeatureVectorTest extends AbstractFilterTest {

    public TweetToWordListCountFeatureVectorTest(String name) {
        super(name);
    }

    /** Creates a default TweetToSentiStrengthFeatureVector filter */
    public Filter getFilter() {
	Filter f = null;

	// Check to see if the test is run from directory containing build_package.xml
	if ((new File(".." + File.separator + "AffectiveTweets" + File.separator + "build_package.xml")).exists()) {
	    File backup = weka.core.WekaPackageManager.PACKAGES_DIR;
	    weka.core.WekaPackageManager.PACKAGES_DIR = new java.io.File(".."); // So that default lexicon, etc., is found.
	    f = new TweetToWordListCountFeatureVector();
	    weka.core.WekaPackageManager.PACKAGES_DIR = backup;
	} else {
	    f = new TweetToWordListCountFeatureVector(); // Hope that the package is installed.
	}
	return f;
    }

    /**
     * Test for the FilteredClassifier used with this filter.
     *
     * @return the configured FilteredClassifier
     */
    protected FilteredClassifier getFilteredClassifier() {
        FilteredClassifier	result;

        result = new FilteredClassifier();

	weka.filters.MultiFilter mf = new weka.filters.MultiFilter();
	Filter[] filters = new Filter[2];
	filters[0] = getFilter();
	weka.filters.unsupervised.attribute.RemoveType rt = new weka.filters.unsupervised.attribute.RemoveType(); // Need to remove string attributes because they are kept by this filter.
	filters[1] = rt;
	mf.setFilters(filters);
	result.setFilter(mf);
        result.setClassifier(new weka.classifiers.functions.SMO());

        return result;
    }

    /**
     * Data to be used for FilteredClassifier test.
     *
     * @return the configured FilteredClassifier
     */
    protected Instances getFilteredClassifierData() throws Exception {
        Instances result;

	// Check to see if the test is run from directory containing build_package.xml
	if ((new File(".." + File.separator + "AffectiveTweets" + File.separator + "build_package.xml")).exists()) {
	    result = (new weka.core.converters.ConverterUtils.DataSource("data" + File.separator + "sent140test.arff.gz")).getDataSet();
	} else { // Hope that package is installed.
	    result = (new weka.core.converters.ConverterUtils.DataSource(weka.core.WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "data" + File.separator + "sent140test.arff.gz")).getDataSet();
	}

	result.setClassIndex(result.numAttributes() - 1);

        return result;
    }

    /**
     * Called by JUnit before each test method. Sets up the Instances object to use based on 
     * one of the datasets that comes with the package.
     *
     * @throws Exception if an error occurs reading the example instances.
     */
    protected void setUp() throws Exception {
        super.setUp();

	// Check to see if the test is run from directory containing build_package.xml
	if ((new File(".." + File.separator + "AffectiveTweets" + File.separator + "build_package.xml")).exists()) {
	    m_Instances = (new weka.core.converters.ConverterUtils.DataSource("data" + File.separator + "sent140test.arff.gz")).getDataSet();
	} else { // Hope that package is installed.
	    m_Instances = (new weka.core.converters.ConverterUtils.DataSource(weka.core.WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "data" + File.separator + "sent140test.arff.gz")).getDataSet();
	}

	m_Instances.setClassIndex(m_Instances.numAttributes() - 1);
    }

    public static Test suite() {
        return new TestSuite(TweetToWordListCountFeatureVectorTest.class);
    }

    public static void main(String[] args){
        junit.textui.TestRunner.run(suite());
    }
}

```



