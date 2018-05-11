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
 *    LexiconEvaluator.java
 *    Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */

package affective.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *  <!-- globalinfo-start --> 
 *  This abstract class provides the structure for all classes calculating attributes from lexicons.
 * <!-- globalinfo-end -->
 * 
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public abstract class LexiconEvaluator implements Serializable {

	/** for serialization */
	private static final long serialVersionUID = 1L;
	
	/** The lexicon file */
	protected String path;
	
	/** The name of the lexicon */
	protected String name; 
	
	/** A list with all the features provided by the lexicon evaluator */
	protected List<String> featureNames; 
	
	
	/**
	 * initializes the Object
	 * 
	 * @param path the file with the lexicon
	 * @param name the prefix for all the attributes calculated from this lexicon
	 */	
	public LexiconEvaluator(String path,String name){
		this.path=path;
		this.name=name;
	}
	
	/**
	 * initializes the dictionary
	 * @throws IOException in case of wrong file
	 */	
	public abstract void processDict()  throws IOException;
	

	/**
	 * Calculates lexicon-based feature values from a list of tokens
	 * @param tokens a tokenized tweet
	 * @return a mapping between attribute names and their scores
	 */	
	public abstract Map<String,Double> evaluateTweet(List<String> tokens);

	/**
	 * Gets the feature names
	 * 
	 * @return the feature names.
	 */	
	public List<String> getFeatureNames() {
		return featureNames;
	}

	
	
}
