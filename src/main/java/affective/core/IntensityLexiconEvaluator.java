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
 *    IntensityLexiconEvaluator.java
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */



package affective.core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;


/**
 *  <!-- globalinfo-start --> 
 *  This class is used for evaluating lexicons with numerical sentiment scores.
 *  <!-- globalinfo-end -->
 * 
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class IntensityLexiconEvaluator extends LexiconEvaluator  {


	/** For serialization. */
	private static final long serialVersionUID = -2094228012480778199L;

	/** The dictionary. */
	protected Map<String, String> dict;

	/**
	 * Initializes the Object
	 * 
	 * @param file the file with the lexicon
	 * @param name the prefix for all the attributes calculated from this lexicon
	 */
	public IntensityLexiconEvaluator(String file,String name) {
		super(file,name);
		this.dict = new HashMap<String, String>();	

		this.featureNames=new ArrayList<String>();
		this.featureNames.add(name+"-posScore");
		this.featureNames.add(name+"-negScore");

	}


	/* (non-Javadoc)
	 * @see affective.core.LexiconEvaluator#processDict()
	 */
	@Override
	public void processDict() throws IOException  {
		// first, we open the file
		FileInputStream fin = new FileInputStream(this.path);
		GZIPInputStream gzis = new GZIPInputStream(fin);
		InputStreamReader xover = new InputStreamReader(gzis);
		BufferedReader bf = new BufferedReader(xover);

		String line;
		while ((line = bf.readLine()) != null) {
			String pair[] = line.split("\t");
			this.dict.put(pair[0], pair[1]);

		}
		bf.close();
		xover.close();
		gzis.close();
		fin.close();

	}

	/**
	 * returns the score associated with a word
	 * 
	 * @param word the input word
	 * @return the value for the word 
	 */
	public String retrieveValue(String word) {
		if (!this.dict.containsKey(word)) {
			return "not_found";
		} else {
			return this.dict.get(word);
		}

	}
	

	/* (non-Javadoc)
	 * @see affective.core.LexiconEvaluator#evaluateTweet(java.util.List)
	 */
	@Override
	public Map<String, Double> evaluateTweet(List<String> tokens) {
		Map<String, Double> strengthScores = new HashMap<String, Double>();
		double posScore = 0;
		double negScore = 0;
		for (String w : tokens) {
			String pol = this.retrieveValue(w);
			if (!pol.equals("not_found")) {
				double value = Double.parseDouble(pol);
				if (value > 0) {
					posScore += value;
				} else {
					negScore += value;
				}
			}
		}
		strengthScores.put(name+"-posScore", posScore);
		strengthScores.put(name+"-negScore", negScore);

		return strengthScores;
	}

	/**
	 * Gets the dictionary mapping the words to their vectors
	 * 
	 * @return the dictionary.
	 */
	public Map<String, String> getDict() {
		return this.dict;
	}


}
