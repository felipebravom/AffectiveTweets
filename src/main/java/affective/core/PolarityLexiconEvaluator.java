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
 *    PolarityLexiconEvaluator.java
 *    Copyright (C) 1999-2016 University of Waikato, Hamilton, New Zealand
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
 *  This class is used for evaluating the polarity lexicons with positive and negative
 *  nominal entries.
 * <!-- globalinfo-end -->
 * 
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class PolarityLexiconEvaluator extends LexiconEvaluator {

	/** For serialization. */
	private static final long serialVersionUID = 5921580335557644894L;

	/** A mapping between words and the sentiment label. */	
	protected Map<String, String> dict;


	/**
	 * initializes the Object
	 * 
	 * @param file the file with the lexicon
	 * @param name the prefix for all the attributes calculated from this lexicon
	 */	
	public PolarityLexiconEvaluator(String file,String name) {
		super(file,name);
		this.dict = new HashMap<String, String>();	

		this.featureNames=new ArrayList<String>();
		this.featureNames.add(name+"-posCount");
		this.featureNames.add(name+"-negCount");

	}

	
	/* (non-Javadoc)
	 * @see affective.core.LexiconEvaluator#processDict()
	 */
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
	 * returns the sentiment associated with a word
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
		Map<String, Double> sentCount = new HashMap<String, Double>();

		double negCount = 0.0;
		double posCount = 0.0;

		for (String w : tokens) {
			String pol = this.retrieveValue(w);
			if (pol.equals("positive")) {
				posCount++;
			} else if (pol.equals("negative")) {
				negCount++;
			}
		}

		sentCount.put(this.name+"-posCount", posCount);
		sentCount.put(this.name+"-negCount", negCount);

		return sentCount;
	}
	
	
	/**
	 * Gets the dictionary mapping the words to their sentiment
	 * 
	 * @return the dictionary.
	 */	
	public Map<String, String> getDict() {
		return this.dict;
	}	
	
}
