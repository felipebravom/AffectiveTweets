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
 * 
 * @author fbravo Evaluates a lexicon from a csv file
 */
public class PolarityLexiconEvaluator extends LexiconEvaluator {

	protected Map<String, String> dict;

	public PolarityLexiconEvaluator(String file,String name) {
		super(file,name);
		this.dict = new HashMap<String, String>();	
		
		this.featureNames=new ArrayList<String>();
		this.featureNames.add(name+"-posCount");
		this.featureNames.add(name+"-negCount");

	}

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

	// returns the score associated to a word
	public String retrieveValue(String word) {
		if (!this.dict.containsKey(word)) {
			return "not_found";
		} else {
			return this.dict.get(word);
		}

	}

	public Map<String, String> getDict() {
		return this.dict;
	}

	// counts positive and negative words from a polarity-oriented lexicon
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
}
