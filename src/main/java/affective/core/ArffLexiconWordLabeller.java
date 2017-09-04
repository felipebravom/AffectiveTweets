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
 *    ArffLexiconWordLabeller.java
 *    Copyright (C) 1999-2017 University of Waikato, Hamilton, New Zealand
 *
 */

package affective.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.OptionMetadata;
import weka.core.WekaPackageManager;


/**
 *  <!-- globalinfo-start --> 
 *  This class is used for calculating scores for a tweet using a lexicons in arff format. 
 *  Numeric associations are added  and nominal ones are countes.
 *  <!-- globalinfo-end -->
 * 
 * 
 * @author Felipe Bravo-Marquez (fjb11@students.waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class ArffLexiconWordLabeller implements Serializable, OptionHandler {

	/** for serialization */
	private static final long serialVersionUID = 8291541753405292438L;


	/** A list with all the features provided by the lexicon evaluator */
	protected List<Attribute> attributes=new ArrayList<Attribute>();

	/** a mapping between words and Attribute-value pairs */	
	protected Map<String, Map<Attribute, Double>> attValMap = new HashMap<String, Map<Attribute, Double>>(); 	




	/** Default path to where lexicons are stored */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons"+ File.separator + "arff_lexicons";

	/** The path of the MetaLexLexicon lexicon */
	public static String METALEX_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"metaLexEmo.arff";


	/** the index of the word attribute in the given arff lexicon */
	protected int lexiconWordIndex=1;

	/** The input lexicon in arff format  */
	protected File m_lexiconFile = new File(METALEX_FILE_NAME);

	/** The lexicon name to be prefixed in all features */
	protected String lexiconName="MetaLexEmo";




	/**
	 * Processes  all the dictionary files.
	 * @throws IOException  an IOException will be raised if an invalid file is supplied
	 */
	public void processDict() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(this.m_lexiconFile));
		Instances lex=new Instances(reader);


		// checks all numeric and nominal attributes and discards the word attribute
		for(int i=0;i<lex.numAttributes();i++){

			if(i!=this.lexiconWordIndex-1){
				if(lex.attribute(i).isNumeric() || lex.attribute(i).isNominal()  ){
					this.attributes.add(lex.attribute(i));
				}

			}

		}


		// Maps all words with their affective scores discarding missing values
		for(Instance inst:lex){
			if(inst.attribute(this.lexiconWordIndex-1).isString()){
				String word=inst.stringValue(this.lexiconWordIndex-1);

				// map numeric scores
				if(!attributes.isEmpty()){
					Map<Attribute,Double> wordVals=new HashMap<Attribute,Double>();
					for(Attribute na:attributes){
						wordVals.put(na,inst.value(na));
					}
					this.attValMap.put(word, wordVals);					
				}



			}

		}




	}


	/**
	 * Calculates lexicon-based feature values from a list of tokens
	 * @param tokens a tokenized tweet
	 * @return a mapping between attribute names and their scores
	 */	
	public Map<Attribute, Double> evaluateWord(String word) {


		// Add numeric scores
		if (this.attValMap.containsKey(word)) {
			return this.attValMap.get(word);
		}
		else{
			Map<Attribute, Double> scores = new HashMap<Attribute, Double>();
			for(Attribute at:attributes){
				scores.put(at,weka.core.Utils.missingValue());					
			}
			return scores;


		}




	}





	/**
	 * Gets the feature names
	 * 
	 * @return the feature names.
	 */	
	public List<Attribute> getAttributes() {
		return attributes;
	}


	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */	
	public String globalInfo() {
		return "This object calculates features from a lexicon in arff format. \n";
	}



	/* (non-Javadoc)
	 * @see weka.filters.Filter#listOptions()
	 */
	public Enumeration<Option> listOptions() {
		return Option.listOptionsForClass(this.getClass()).elements();
	}


	/* (non-Javadoc)
	 * @see weka.filters.Filter#getOptions()
	 */
	public String[] getOptions() {		
		return Option.getOptions(this, this.getClass());
	}



	/* (non-Javadoc)
	 * @see weka.core.OptionHandler#setOptions(java.lang.String[])
	 */
	public void setOptions(String[] options) throws Exception {
		Option.setOptions(options, this, this.getClass());
	}



	@OptionMetadata(
			displayName = "lexicon file",
			description = "The arff file with the input lexicon.",
			commandLineParamName = "lexiconFile", commandLineParamSynopsis = "-lexiconFile <string>",
			displayOrder = 1)
	public File getLexiconFile() { return m_lexiconFile; }
	public void setLexiconFile(File lexiconFile) { m_lexiconFile = lexiconFile; }



	@OptionMetadata(displayName = "lexiconName",
			description = "The lexicon name to be prefixed in all features calculated from this lexicon.",
			commandLineParamName = "B", commandLineParamSynopsis = "-B",
			displayOrder = 1)   
	public String getLexiconName() {
		return lexiconName;
	}
	public void setLexiconName(String lexiconName) {
		this.lexiconName = lexiconName;
	}



	@OptionMetadata(displayName = "lexiconWordIndex",
			description = "The index of the word attribute in the given arff lexicon (starting from 1).", 
			commandLineParamName = "A", commandLineParamSynopsis = "-A",
			displayOrder = 1)	
	public int getLexiconWordIndex() {
		return lexiconWordIndex;
	}
	public void setLexiconWordIndex(int lexiconWordIndex) {
		this.lexiconWordIndex = lexiconWordIndex;
	}

}
