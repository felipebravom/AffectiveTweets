package weka.filters.unsupervised.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import affective.core.IntensityLexiconEvaluator;
import affective.core.LexiconEvaluator;
import affective.core.NRCEmotionLexiconEvaluator;
import affective.core.PolarityLexiconEvaluator;
import cmu.arktweetnlp.Twokenize;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.filters.SimpleBatchFilter;

public class TweetToLexiconFeatureVector extends SimpleBatchFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4983739424598292130L;
	
	/** the index of the string attribute to be processed */
	protected int textIndex=1; 
	
	
	private List<LexiconEvaluator> lexicons=new ArrayList<LexiconEvaluator>();
	
	@Override
	public String globalInfo() {
		return "A batch filter that calcuates attributes from different lexical resources for Sentiment Analysis ";

	}
	
	
	@Override
	public Enumeration<Option> listOptions() {
		Vector<Option> result = new Vector<Option>();

		result.addElement(new Option("\t Index of string attribute.\n"
				+ "\t(default: " + this.textIndex + ")", "I", 1, "-I"));		
	
		result.addAll(Collections.list(super.listOptions()));

		return result.elements();
	}


	/**
	 * returns the options of the current setup
	 * 
	 * @return the current options
	 */
	@Override
	public String[] getOptions() {

		Vector<String> result = new Vector<String>();

		result.add("-I");
		result.add("" + this.getTextIndex());

	
		Collections.addAll(result, super.getOptions());

		return result.toArray(new String[result.size()]);
	}


	/**
	 * Parses the options for this object.
	 * <p/>
	 * 
	 * <!-- options-start --> <!-- options-end -->
	 * 
	 * @param options
	 *            the options to use
	 * @throws Exception
	 *             if setting of options fails
	 */
	@Override
	public void setOptions(String[] options) throws Exception {


		String textIndexOption = Utils.getOption('I', options);
		if (textIndexOption.length() > 0) {
			String[] textIndexSpec = Utils.splitOptions(textIndexOption);
			if (textIndexSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid index");
			}
			int index = Integer.parseInt(textIndexSpec[0]);
			this.setTextIndex(index);

		}
	
		
		super.setOptions(options);

		Utils.checkForRemainingOptions(options);


	}


	@Override
	protected Instances determineOutputFormat(Instances inputFormat)
			throws Exception {

		ArrayList<Attribute> att = new ArrayList<Attribute>();

		// Adds all attributes of the inputformat
		for (int i = 0; i < inputFormat.numAttributes(); i++) {
			att.add(inputFormat.attribute(i));
		}
		
		LexiconEvaluator opFinderLex = new PolarityLexiconEvaluator(
				"lexicons/opinion-finder.txt.gz","opFinder");
		opFinderLex.processDict();
		
		this.lexicons.add(opFinderLex);
		
		LexiconEvaluator bingLiuLex = new PolarityLexiconEvaluator(
				"lexicons/BingLiu.csv.gz","BingLiu");
		bingLiuLex.processDict();
		
		this.lexicons.add(bingLiuLex);
		
		
		LexiconEvaluator afinnLex = new IntensityLexiconEvaluator(
				"lexicons/AFINN-111.txt.gz","AFINN");
		afinnLex.processDict();

		this.lexicons.add(afinnLex);
		
		LexiconEvaluator s140Lex = new IntensityLexiconEvaluator(
				"lexicons/Sentiment140-Lexicon-v0.1/unigrams-pmilexicon.txt.gz","S140");
		s140Lex.processDict();
		
		this.lexicons.add(s140Lex);

		LexiconEvaluator nrcHashLex = new IntensityLexiconEvaluator(
				"lexicons/NRC-Hashtag-Sentiment-Lexicon-v0.1/unigrams-pmilexicon.txt.gz","NRCHASH");
		nrcHashLex.processDict();
		
		this.lexicons.add(nrcHashLex);
		
		
		LexiconEvaluator nrcEmoLex = new NRCEmotionLexiconEvaluator(
				"lexicons/NRC-emotion-lexicon-wordlevel-v0.92.txt.gz","NRC-10");
		nrcEmoLex.processDict();
		
		this.lexicons.add(nrcEmoLex);
		
		
		
		for(LexiconEvaluator le:this.lexicons){
			for(String attName:le.getFeatureNames())
				att.add(new Attribute(attName));			
		}


		Instances result = new Instances(inputFormat.relationName(), att, 0);

		// set the class index
		result.setClassIndex(inputFormat.classIndex());

		return result;
	}

	@Override
	protected Instances process(Instances instances) throws Exception {
		// Instances result = new Instances(determineOutputFormat(instances),
		// 0);

		Instances result = getOutputFormat();

		
		// reference to the content of the message, users index start from zero
		Attribute attrCont = instances.attribute(this.textIndex-1);


		for (int i = 0; i < instances.numInstances(); i++) {
			double[] values = new double[result.numAttributes()];
			for (int n = 0; n < instances.numAttributes(); n++)
				values[n] = instances.instance(i).value(n);

			String content = instances.instance(i).stringValue(attrCont);
			List<String> words = Twokenize.tokenizeRawTweetText(content);

			for(LexiconEvaluator le:this.lexicons){
				Map<String,Double> featuresForLex=le.evaluateTweet(words);
				for(String featName:featuresForLex.keySet()){
					values[result.attribute(featName).index()] = featuresForLex.get(featName);
				}
			}
			
	
			Instance inst = new SparseInstance(1, values);

			inst.setDataset(result);

			// copy possible strings, relational values...
			copyValues(inst, false, instances, result);

			result.add(inst);

		}

		return result;
	}
	
	
	public int getTextIndex() {
		return textIndex;
	}

	public void setTextIndex(int textIndex) {
		this.textIndex = textIndex;
	}

}
