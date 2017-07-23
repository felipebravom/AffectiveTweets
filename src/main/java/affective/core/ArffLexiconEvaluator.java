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
 *    ArffLexiconEvaluator.java
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
public class ArffLexiconEvaluator implements Serializable, OptionHandler {

	/** for serialization */
	private static final long serialVersionUID = 8291541753405292438L;


	/** A list with all the features provided by the lexicon evaluator */
	protected List<String> featureNames=new ArrayList<String>(); 

	/** a mapping between words and the affective numeric scores */	
	protected Map<String, Map<String, Double>> numDict = new HashMap<String, Map<String, Double>>(); 	

	/** a mapping between words and the affective nominal categories */	
	protected Map<String, Map<String, String>> nomDict = new HashMap<String, Map<String,String>>(); 	


	/** Default path to where lexicons are stored */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons"+ File.separator + "arff_lexicons";

	/** The path of the MPQA lexicon */
	public static String NRC_AFFECT_INTENSITY_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"NRC-AffectIntensity-Lexicon.arff";


	/** the index of the word attribute in the given arff lexicon */
	protected int lexiconWordIndex=1;

	/** The input lexicon in arff format  */
	protected File m_lexiconFile = new File(NRC_AFFECT_INTENSITY_FILE_NAME);

	/** The lexicon name to be prefixed in all features */
	protected String lexiconName="NRC-Affect-Intensity";




	/**
	 * Processes  all the dictionary files.
	 * @throws IOException
	 */
	public void processDict() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(this.m_lexiconFile));
		Instances lex=new Instances(reader);

		List<Attribute> numericAttributes=new ArrayList<Attribute>();
		List<Attribute> nominalAttributes=new ArrayList<Attribute>();



		// checks all numeric and nominal attributes and discards the word attribute
		for(int i=0;i<lex.numAttributes();i++){

			if(i!=this.lexiconWordIndex-1){
				if(lex.attribute(i).isNumeric() ){
					numericAttributes.add(lex.attribute(i));	
					// adds the attribute name to the message-level features to be calculated
					this.featureNames.add(this.lexiconName+"-"+lex.attribute(i).name());
				}

				else if(lex.attribute(i).isNominal() ){
					nominalAttributes.add(lex.attribute(i));	
					// adds the attribute name together with the nominal value to the message-level features to be calculated
					int numValues=lex.attribute(i).numValues();
					for(int j=0;j<numValues;j++)
						this.featureNames.add(this.lexiconName+"-"+lex.attribute(i).name()+"-"+lex.attribute(i).value(j));

				}

			}

		}


		// Maps all words with their affective scores discarding missing values
		for(Instance inst:lex){
			if(inst.attribute(this.lexiconWordIndex-1).isString()){
				String word=inst.stringValue(this.lexiconWordIndex-1);

				// map numeric scores
				if(!numericAttributes.isEmpty()){
					Map<String,Double> wordVals=new HashMap<String,Double>();
					for(Attribute na:numericAttributes){
						if(!weka.core.Utils.isMissingValue(inst.value(na)))
							wordVals.put(na.name(),inst.value(na));
					}
					this.numDict.put(word, wordVals);					
				}

				// map nominal associations
				if(!nominalAttributes.isEmpty()){
					Map<String,String> wordCounts=new HashMap<String,String>();
					for(Attribute no:nominalAttributes){
						if(!weka.core.Utils.isMissingValue(inst.value(no))){	
							wordCounts.put(no.name(),no.value((int) inst.value(no)));
						}

						this.nomDict.put(word, wordCounts);

					}

				}				

			}

		}




	}


	/**
	 * Calculates lexicon-based feature values from a list of tokens
	 * @param tokens a tokenized tweet
	 * @return a mapping between attribute names and their scores
	 */	
	public Map<String, Double> evaluateTweet(List<String> tokens) {
		Map<String, Double> scores = new HashMap<String, Double>();
		for(String feat:this.featureNames){
			scores.put(feat, 0.0);
		}

		for (String word : tokens) {
			// Add numeric scores
			if (this.numDict.containsKey(word)) {
				Map<String,Double> mapper=this.numDict.get(word);
				for(String emo:mapper.keySet())
					scores.put(this.lexiconName+"-"+emo, scores.get(this.lexiconName+"-"+emo)+mapper.get(emo));
			}


			// count nominal associations
			if (this.nomDict.containsKey(word)) {
				Map<String,String> mapper=this.nomDict.get(word);
				for(String emo:mapper.keySet())
					scores.put(this.lexiconName+"-"+emo+"-"+mapper.get(emo), scores.get(this.lexiconName+"-"+emo+"-"+mapper.get(emo))+1);

			}

		}



		return scores;
	}


	/**
	 * Gets the feature names
	 * 
	 * @return the feature names.
	 */	
	public List<String> getFeatureNames() {
		return featureNames;
	}


	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */	
	public String globalInfo() {
		return "The object calculates features from a lexicon in arff format. \n";
	}



	/* (non-Javadoc)
	 * @see weka.filters.Filter#listOptions()
	 */
	@Override
	public Enumeration<Option> listOptions() {
		return Option.listOptionsForClass(this.getClass()).elements();
	}


	/* (non-Javadoc)
	 * @see weka.filters.Filter#getOptions()
	 */
	@Override
	public String[] getOptions() {		
		return Option.getOptions(this, this.getClass());
	}



	/* (non-Javadoc)
	 * @see weka.core.OptionHandler#setOptions(java.lang.String[])
	 */
	@Override
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
