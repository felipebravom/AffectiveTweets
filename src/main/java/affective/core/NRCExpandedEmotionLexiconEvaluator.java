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
 *    NRCExpandedEmotionEvaluator.java
 *    Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */


package affective.core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;


/**
 *  <!-- globalinfo-start --> 
 *  This class is used for evaluating the NRC-10 Expanded Emotion Lexicon.
 * <!-- globalinfo-end -->
 * 
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class NRCExpandedEmotionLexiconEvaluator extends LexiconEvaluator {


	/** For serialization. */
	private static final long serialVersionUID = -6019728417777495994L;

	/** A mapping between words and the affective scores. */	
	protected Map<String, Map<String, Double>> dict; 

	/**
	 * initializes the Object
	 * 
	 * @param path the file with the lexicon
	 * @param name the prefix for all the attributes calculated from this lexicon
	 */	
	public NRCExpandedEmotionLexiconEvaluator(String path,String name) {
		super(path,name);
		this.dict = new HashMap<String, Map<String, Double>>();

		this.featureNames=new ArrayList<String>();
		this.featureNames.add(name+"-anger");
		this.featureNames.add(name+"-anticipation");
		this.featureNames.add(name+"-disgust");
		this.featureNames.add(name+"-fear");
		this.featureNames.add(name+"-joy");
		this.featureNames.add(name+"-sadness");
		this.featureNames.add(name+"-surprise");
		this.featureNames.add(name+"-trust");
		this.featureNames.add(name+"-negative");
		this.featureNames.add(name+"-positive");



	}

	/**
	 * Gets the dictionary mapping the words to their emotion associations
	 * 
	 * @return the dictionary.
	 */		
	public Map<String, Map<String, Double>> getDict() {
		return this.dict;
	}

	/**
	 * Gets the emotions for a word
	 * 
	 * @param word the word
	 * @return the emotions
	 */		
	public Map<String, Double> getWord(String word) {
		if (this.dict.containsKey(word))
			return dict.get(word);
		else
			return null;
	}

	
	/* (non-Javadoc)
	 * @see affective.core.LexiconEvaluator#processDict()
	 */
	@Override
	public void processDict() throws IOException {

		FileInputStream fin = new FileInputStream(this.path);
		GZIPInputStream gzis = new GZIPInputStream(fin);
		InputStreamReader xover = new InputStreamReader(gzis);
		BufferedReader bf = new BufferedReader(xover);


		String firstLine=bf.readLine();
		String fieldNames[] = firstLine.split("\t");

		String line;
		while ((line = bf.readLine()) != null) {
			Map<String,Double> entry=new HashMap<String,Double>();

			String pair[] = line.split("\t");
			String word=pair[0];
			for(int i=1;i<pair.length;i++)
				entry.put(fieldNames[i],Double.parseDouble(pair[i]));


			this.dict.put(word, entry);

		}
		bf.close();

	}


	/* (non-Javadoc)
	 * @see affective.core.LexiconEvaluator#evaluateTweet(java.util.List)
	 */
	@Override
	public Map<String, Double> evaluateTweet(List<String> words) {

		Map<String, Double> emoCount = new HashMap<String, Double>();

		double anger = 0.0;
		double anticipation = 0.0;
		double disgust = 0.0;
		double fear = 0.0;
		double joy = 0.0;
		double sadness = 0.0;
		double surprise = 0.0;
		double trust = 0.0;
		double negative = 0.0;
		double positive = 0.0;

		for (String word : words) {
			// I retrieve the EmotionMap if the word match the lexicon
			if (this.getDict().containsKey(word)) {
				Map<String, Double> emotions = this.getDict().get(word);
				anger += emotions.get("anger");
				anticipation += emotions.get("anticipation");
				disgust += emotions.get("disgust");
				fear += emotions.get("fear");
				joy += emotions.get("joy");
				sadness += emotions.get("sadness");
				surprise += emotions.get("surprise");
				trust += emotions.get("trust");
				negative += emotions.get("negative");
				positive += emotions.get("positive");

			}
		}

		emoCount.put(name+"-anger", anger);
		emoCount.put(name+"-anticipation", anticipation);
		emoCount.put(name+"-disgust", disgust);
		emoCount.put(name+"-fear", fear);
		emoCount.put(name+"-joy", joy);
		emoCount.put(name+"-sadness", sadness);
		emoCount.put(name+"-surprise", surprise);
		emoCount.put(name+"-trust", trust);
		emoCount.put(name+"-negative", negative);
		emoCount.put(name+"-positive", positive);


		return emoCount;
	}



}
