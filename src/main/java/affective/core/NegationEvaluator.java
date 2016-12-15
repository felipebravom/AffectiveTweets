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
 *    NegationEvaluator.java
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

// counts the number of negation words in a tweet

public class NegationEvaluator extends LexiconEvaluator {

	/** for serialization */
	private static final long serialVersionUID = 1331150082874408516L;

	protected Set<String> wordList;


	public NegationEvaluator(String path, String name) {
		super(path, name);
		this.wordList=new HashSet<String>();

		this.featureNames=new ArrayList<String>();
		this.featureNames.add(name+"-negationCount");

	}



	@Override
	public void processDict() throws IOException {
		FileInputStream fin = new FileInputStream(this.path);
		GZIPInputStream gzis = new GZIPInputStream(fin);
		InputStreamReader xover = new InputStreamReader(gzis);
		BufferedReader bf = new BufferedReader(xover);

		String line;
		while ((line = bf.readLine()) != null) {
			this.wordList.add(line);
		}
		bf.close();
		xover.close();
		gzis.close();
		fin.close();

	}

	@Override
	public Map<String, Double> evaluateTweet(List<String> tokens) {
		Map<String, Double> negCountsFeat = new HashMap<String, Double>();

		double neationCount = 0.0;


		// counts the number of negation words
		for (String w : tokens) {
			if(this.wordList.contains(w))
				neationCount++;
		}


		negCountsFeat.put(name+"-negationCount", neationCount);

		return negCountsFeat;

	}
	
	public Set<String> getWordList() {
		return wordList;
	}



	public void setWordList(Set<String> wordList) {
		this.wordList = wordList;
	}


}
