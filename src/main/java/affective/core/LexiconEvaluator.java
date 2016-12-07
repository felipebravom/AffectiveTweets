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
 *    Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 */

package affective.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;


public abstract class LexiconEvaluator implements Serializable {

	/** for serialization */
	private static final long serialVersionUID = 1L;
	
	
	protected String path;
	protected String name; // The lexicon's name
	protected List<String> featureNames; // list with all the features provided by the lexicon evaluator
	
	public LexiconEvaluator(String path,String name){
		this.path=path;
		this.name=name;
	}
	
	/* Process the dictionary */
	public abstract void processDict()  throws IOException;
	
	/* Calculates lexicon-based feature values from a string of tokens */
	public abstract Map<String,Double> evaluateTweet(List<String> tokens);

	public List<String> getFeatureNames() {
		return featureNames;
	}

	public void setFeatureNames(List<String> featureNames) {
		this.featureNames = featureNames;
	}
	
	
}
