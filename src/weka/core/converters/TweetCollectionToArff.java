


package weka.core.converters;

import weka.core.Instances;


/**
* Builds an arff dataset from a collection of tweets in a given file.
* @author Felipe Bravo-Marquez (fjb11 at students.waikato.ac.nz)
* @version 1.0
*/

public abstract class TweetCollectionToArff {
	
	
	public abstract Instances createDataset(String collectionPath) throws Exception;
}
