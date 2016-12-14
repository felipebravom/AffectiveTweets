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
