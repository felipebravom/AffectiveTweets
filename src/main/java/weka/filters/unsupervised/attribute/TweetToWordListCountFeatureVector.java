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
