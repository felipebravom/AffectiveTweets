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

public class ArffLexiconEvaluator extends LexiconEvaluator {

	/** for serialization */
	private static final long serialVersionUID = 8291541753405292438L;

	/** a mapping between words and the affective scores */	
	protected Map<String, Map<String, Double>> dict; 	

	protected int wordIndex;


	public ArffLexiconEvaluator(String path, String name, int index) {
		super(path, name);
		this.wordIndex=index;
		this.dict = new HashMap<String, Map<String, Double>>();
		this.featureNames=new ArrayList<String>();
	}



	@Override
	public void processDict() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(this.path));
		Instances lex=new Instances(reader);

		List<Attribute> numericAttributes=new ArrayList<Attribute>();
		// checks all numeric attributes
		for(int i=0;i<lex.numAttributes();i++){
			if(lex.attribute(i).isNumeric() && i!=this.wordIndex-1){
				numericAttributes.add(lex.attribute(i));	

				// adds the attribute name to the message-level features to be calculated
				this.featureNames.add(name+"-"+lex.attribute(i).name());

			}


		}



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


	static public void main(String args[]) throws IOException{
		ArffLexiconEvaluator al=new ArffLexiconEvaluator("lexicons/NRC-AffectIntensity-Lexicon.arff","NRC-Affect",1);

		al.processDict();
		String tweet="i hate tear lovee";
		List<String> tokens=Utils.tokenize(tweet, true, true);
		Map<String,Double> scores=al.evaluateTweet(tokens);
		for(String val:scores.keySet())
			System.out.println(val+" "+scores.get(val));




	}

}
