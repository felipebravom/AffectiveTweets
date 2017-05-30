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
 *  This class is used for evaluating lexicons in arff format.  
 *  <!-- globalinfo-end -->
 * 
 * 
 * @author Felipe Bravo-Marquez (fjb11@students.waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class ArffLexiconEvaluator extends LexiconEvaluator {

	/** for serialization */
	private static final long serialVersionUID = 8291541753405292438L;

	/** a mapping between words and the affective scores */	
	protected Map<String, Map<String, Double>> dict; 	

	/** the word attribute index (starting from 1) */
	protected int wordIndex;

	/**
	 * initializes the Object
	 * 
	 * @param file the file with the lexicon
	 * @param name the prefix for all the attributes calculated from this lexicon
	 * @param index the attribute index (starting from 1) of the word 
	 */
	public ArffLexiconEvaluator(String path, String name, int index) {
		super(path, name);
		this.wordIndex=index;
		this.dict = new HashMap<String, Map<String, Double>>();
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
		// checks all numeric attributes and discards the word attribute
		for(int i=0;i<lex.numAttributes();i++){
			if(lex.attribute(i).isNumeric() && i!=this.wordIndex-1){
				numericAttributes.add(lex.attribute(i));	

				// adds the attribute name to the message-level features to be calculated
				this.featureNames.add(name+"-"+lex.attribute(i).name());

			}

		}


		// Maps all words with their affective scores discarding missing values
		for(Instance inst:lex){
			if(inst.attribute(this.wordIndex-1).isString()){
				String word=inst.stringValue(this.wordIndex-1);
				Map<String,Double> wordVals=new HashMap<String,Double>();
				for(Attribute na:numericAttributes){
					if(!weka.core.Utils.isMissingValue(inst.value(na)))
						wordVals.put(na.name(),inst.value(na));
				}
				this.dict.put(word, wordVals);

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
			// I retrieve the EmotionMap if the word match the lexicon
			if (this.dict.containsKey(word)) {
				Map<String,Double> mapper=this.dict.get(word);
				for(String emo:mapper.keySet())
					scores.put(name+"-"+emo, scores.get(name+"-"+emo)+mapper.get(emo));
			}

		}



		return scores;
	}


}
