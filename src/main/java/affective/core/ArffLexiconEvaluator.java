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
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;


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
public class ArffLexiconEvaluator extends LexiconEvaluator {

	/** for serialization */
	private static final long serialVersionUID = 8291541753405292438L;

	/** a mapping between words and the affective numeric scores */	
	protected Map<String, Map<String, Double>> numDict; 	

	/** a mapping between words and the affective nominal categories */	
	protected Map<String, Map<String, String>> nomDict; 	


	/** the word attribute index (starting from 1) */
	protected int wordIndex;

	/**
	 * initializes the Object
	 * 
	 * @param path the file with the lexicon
	 * @param name the prefix for all the attributes calculated from this lexicon
	 * @param index the attribute index (starting from 1) of the word 
	 */
	public ArffLexiconEvaluator(String path, String name, int index) {
		super(path, name);
		this.wordIndex=index;
		this.numDict = new HashMap<String, Map<String, Double>>();
		this.nomDict = new HashMap<String, Map<String,String>>();

		this.featureNames=new ArrayList<String>();
	}


	/* (non-Javadoc)
	 * @see affective.core.LexiconEvaluator#processDict()
	 */
	@Override
	public void processDict() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(this.path));
		Instances lex=new Instances(reader);

		List<Attribute> numericAttributes=new ArrayList<Attribute>();
		List<Attribute> nominalAttributes=new ArrayList<Attribute>();



		// checks all numeric and nominal attributes and discards the word attribute
		for(int i=0;i<lex.numAttributes();i++){
			
			if(i!=this.wordIndex-1){
				if(lex.attribute(i).isNumeric() ){
					numericAttributes.add(lex.attribute(i));	
					// adds the attribute name to the message-level features to be calculated
					this.featureNames.add(name+"-"+lex.attribute(i).name());
				}

				else if(lex.attribute(i).isNominal() ){
					nominalAttributes.add(lex.attribute(i));	
					// adds the attribute name together with the nominal value to the message-level features to be calculated
					int numValues=lex.attribute(i).numValues();
					for(int j=0;j<numValues;j++)
						this.featureNames.add(name+"-"+lex.attribute(i).name()+"-"+lex.attribute(i).value(j));

				}

			}

		}

	


		// Maps all words with their affective scores discarding missing values
		for(Instance inst:lex){
			if(inst.attribute(this.wordIndex-1).isString()){
				String word=inst.stringValue(this.wordIndex-1);

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


	/* (non-Javadoc)
	 * @see affective.core.LexiconEvaluator#evaluateTweet(java.util.List)
	 */	
	@Override
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
					scores.put(name+"-"+emo, scores.get(name+"-"+emo)+mapper.get(emo));
			}


			// count nominal associations
			if (this.nomDict.containsKey(word)) {
				Map<String,String> mapper=this.nomDict.get(word);
				for(String emo:mapper.keySet())
					scores.put(name+"-"+emo+"-"+mapper.get(emo), scores.get(name+"-"+emo+"-"+mapper.get(emo))+1);

			}

		}



		return scores;
	}


}
