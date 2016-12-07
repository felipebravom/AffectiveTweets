package weka.filters.unsupervised.attribute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import affective.core.IntensityLexiconEvaluator;
import affective.core.LexiconEvaluator;
import affective.core.NRCEmotionLexiconEvaluator;
import affective.core.NRCExpandedEmotionLexiconEvaluator;
import affective.core.NRCHashtagEmotionLexiconEvaluator;
import affective.core.NegationEvaluator;
import affective.core.PolarityLexiconEvaluator;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.core.WekaPackageManager;
import weka.core.Capabilities.Capability;
import weka.filters.SimpleBatchFilter;

public class TweetToLexiconFeatureVector extends SimpleBatchFilter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4983739424598292130L;



	/** Default path to where lexicons are stored */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons";

	/** The path of the MPQA lexicon */
	public static String MPQA_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"mpqa.txt.gz";

	/** The path of the BingLiu lexicon */
	public static String BING_LIU_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"BingLiu.csv.gz";

	/** The path of the AFINN lexicon */
	public static String AFINN_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"AFINN-en-165.txt.gz";

	/** The path of the S140 lexicon */
	public static String S140_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"Sentiment140-Lexicon-v0.1"+java.io.File.separator+"unigrams-pmilexicon.txt.gz";

	/** The path of the NRC-Hashtag-Sentiment lexicon */
	public static String NRC_HASH_SENT_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"NRC-Hashtag-Sentiment-Lexicon-v0.1"+java.io.File.separator+"unigrams-pmilexicon.txt.gz";

	/** The path of the NRC-emotion lexicon */
	public static String NRC10_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"NRC-emotion-lexicon-wordlevel-v0.92.txt.gz";

	/** The path of the NRC-10-Expanded lexicon */
	public static String NRC10_EXPANDED_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"w2v-dp-BCC-Lex.csv.gz";


	/** The path of the NRC Hashtag Emotion lexicon */
	public static String NRC_HASH_EMO_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"NRC-Hashtag-Emotion-Lexicon-v0.2.txt.gz";



	/** The path of the emoticon list */
	public static String EMOTICON_LIST_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"AFINN-emoticon-8.txt.gz";


	/** The path of the negation list */
	public static String NEGATION_LIST_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"NegatingWordList.txt.gz";

	/** the index of the string attribute to be processed */
	protected int textIndex=1; 

	/** True for calculating features from the MPQA lexicon */
	protected boolean useMpqa=true;

	/** True for calculating features from the BingLiu lexicon */
	protected boolean useBingLiu=true;

	/** True for calculating features from the AFINN lexicon */
	protected boolean useAfinn=true;


	/** True for calculating features from the S140 lexicon */
	protected boolean useS140=true;


	/** True for calculating features from the NRC-Hashtag-Sentiment lexicon */
	protected boolean useNrcHashSent=true;


	/** True for calculating features from the NRC-emotion lexicon */
	protected boolean useNrc10=true;


	/** True for calculating features from the NRC-10-Expanded lexicon */
	protected boolean useNrc10Expanded=true;


	/** True for calculating features from the NRC Hashtag Emotion Lexicon */
	protected boolean useNrcHashEmo=true;


	/** True for calculating features from the Emoticon list */
	protected boolean useEmoticons=true;

	/** True for calculating features from the Negation list */
	protected boolean useNegation=true;

	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;


	/** True if url, users, and repeated letters are cleaned */
	protected boolean cleanTokens=false;


	private List<LexiconEvaluator> lexicons=new ArrayList<LexiconEvaluator>();

	@Override
	public String globalInfo() {
		return "A batch filter that calcuates attributes from different lexical resources for Sentiment Analysis ";

	}


	@Override
	public Capabilities getCapabilities() {

		Capabilities result = new Capabilities(this);
		result.disableAll();



		// attributes
		result.enableAllAttributes();
		result.enable(Capability.MISSING_VALUES);

		// class
		result.enableAllClasses();
		result.enable(Capability.MISSING_CLASS_VALUES);
		result.enable(Capability.NO_CLASS);

		result.setMinimumNumberInstances(0);

		return result;
	}

	

	@Override
	public Enumeration<Option> listOptions() {
		Vector<Option> result = new Vector<Option>();

		result.addElement(new Option("\t Index of string attribute.\n"
				+ "\t(default: " + this.textIndex + ")", "I", 1, "-I"));


		result.addElement(new Option(
				"\t use MPQA Lexicon\n"
						+ "\t(default:"+this.useMpqa+")", "A", 0, "-A"));


		result.addElement(new Option(
				"\t use Bing Liu Lexicon\n"
						+ "\t(default:"+this.useBingLiu+")", "D", 0, "-D"));


		result.addElement(new Option(
				"\t use AFINN Lexicon\n"
						+ "\t(default:"+this.useAfinn+")", "F", 0, "-F"));


		result.addElement(new Option(
				"\t use S140 Lexicon\n"
						+ "\t(default:"+this.useS140+")", "H", 0, "-H"));


		result.addElement(new Option(
				"\t use NRC-Hash-Sent Lexicon\n"
						+ "\t(default:"+this.useNrcHashSent+")", "J", 0, "-J"));


		result.addElement(new Option(
				"\t use NRC-10 Emotion Lexicon\n"
						+ "\t(default:"+this.useNrc10+")", "L", 0, "-L"));


		result.addElement(new Option(
				"\t use NRC-10-Expanded Emotion Lexicon\n"
						+ "\t(default:"+this.useNrc10Expanded+")", "N", 0, "-N"));



		result.addElement(new Option(
				"\t use NRC Hashtag Emotion Lexicon\n"
						+ "\t(default:"+this.useNrcHashEmo+")", "P", 0, "-P"));

		result.addElement(new Option(
				"\t use Emoticon List\n"
						+ "\t(default:"+this.useEmoticons+")", "R", 0, "-R"));


		result.addElement(new Option(
				"\t use Negation List\n"
						+ "\t(default:"+this.useNegation+")", "T", 0, "-T"));

		result.addElement(new Option("\t Lowercase content.\n"
				+ "\t(default: " + this.toLowerCase + ")", "U", 0, "-U"));

		result.addElement(new Option("\t Clean tokens (replace goood by good, standarise URLs and @users).\n"
				+ "\t(default: " + this.cleanTokens + ")", "O", 0, "-O"));	


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

		if(this.isUseMpqa())
			result.add("-A");


		if(this.isUseBingLiu())
			result.add("-D");


		if(this.isUseAfinn())
			result.add("-F");


		if(this.isUseS140())
			result.add("-H");


		if(this.isUseNrcHashSent())
			result.add("-J");


		if(this.isUseNrc10())
			result.add("-L");


		if(this.isUseNrc10Expanded())
			result.add("-N");


		if(this.isUseNrcHashEmo())
			result.add("-P");


		if(this.isUseEmoticons())
			result.add("-R");


		if(this.isUseNegation())
			result.add("-T");

		if(this.toLowerCase)
			result.add("-U");

		if(this.cleanTokens)
			result.add("-O");


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

		this.useMpqa=Utils.getFlag('A', options);

		this.useBingLiu=Utils.getFlag('D', options);


		this.useAfinn=Utils.getFlag('F', options);


		this.useS140=Utils.getFlag('H', options);

		this.useNrcHashSent=Utils.getFlag('J', options);


		this.useNrc10=Utils.getFlag('L', options);


		this.useNrc10Expanded=Utils.getFlag('N', options);


		this.useNrcHashEmo=Utils.getFlag('P', options);


		this.useEmoticons=Utils.getFlag('R', options);


		this.useNegation=Utils.getFlag('T', options);


		this.toLowerCase=Utils.getFlag('U', options);

		this.cleanTokens=Utils.getFlag('O', options);

		super.setOptions(options);

		Utils.checkForRemainingOptions(options);


	}

	// Initialize the dictionaries in the first Batch
	protected void initializeDicts() {

		if(this.useMpqa){
			LexiconEvaluator mpqaLex = new PolarityLexiconEvaluator(
					MPQA_FILE_NAME,"mpqa");
			try {
				mpqaLex.processDict();
				this.lexicons.add(mpqaLex);
			} catch (IOException e) {
				this.useMpqa=false;
			}			

		}

		if(this.useBingLiu){
			LexiconEvaluator bingLiuLex = new PolarityLexiconEvaluator(
					BING_LIU_FILE_NAME,"BingLiu");
			try{
				bingLiuLex.processDict();				
				this.lexicons.add(bingLiuLex);				
			}
			catch (IOException e) {
				this.useBingLiu=false;
			}	
		}



		if(this.useAfinn){
			LexiconEvaluator afinnLex = new IntensityLexiconEvaluator(
					AFINN_FILE_NAME,"AFINN");
			try {
				afinnLex.processDict();
				this.lexicons.add(afinnLex);
			} catch (IOException e) {
				this.useAfinn=false;
			}

		}


		if(this.useS140){
			LexiconEvaluator s140Lex = new IntensityLexiconEvaluator(
					S140_FILE_NAME,"S140");
			try {
				s140Lex.processDict();
				this.lexicons.add(s140Lex);
			} catch (IOException e) {
				this.useS140=false;
			}

		}


		if(this.useNrcHashSent){
			LexiconEvaluator nrcHashSentLex = new IntensityLexiconEvaluator(
					NRC_HASH_SENT_FILE_NAME,"NRC-Hash-Sent");
			try {
				nrcHashSentLex.processDict();
				this.lexicons.add(nrcHashSentLex);
			} catch (IOException e) {
				this.useNrcHashSent=false;
			}		

		}


		if(this.useNrc10){
			LexiconEvaluator nrc10Lex = new NRCEmotionLexiconEvaluator(
					NRC10_FILE_NAME,"NRC-10");
			try {
				nrc10Lex.processDict();
				this.lexicons.add(nrc10Lex);
			} catch (IOException e) {
				this.useNrc10=false;
			}
		}


		if(this.useNrc10Expanded){
			LexiconEvaluator nrcExpandedEmoLex = new NRCExpandedEmotionLexiconEvaluator(
					NRC10_EXPANDED_FILE_NAME,"NRC-10-Expanded");
			try {
				nrcExpandedEmoLex.processDict();
				this.lexicons.add(nrcExpandedEmoLex);
			} catch (IOException e) {
				this.useNrc10Expanded=false;
			}		

		}


		if(this.useNrcHashEmo){
			LexiconEvaluator nrcHashtagEmoLex = new NRCHashtagEmotionLexiconEvaluator(
					NRC_HASH_EMO_FILE_NAME,"NRC-Hash-Emo");
			try {
				nrcHashtagEmoLex.processDict();
				this.lexicons.add(nrcHashtagEmoLex);
			} catch (IOException e) {
				this.useNrcHashEmo=false;

			}		


		}


		if(this.useEmoticons){
			LexiconEvaluator emoticonLex = new IntensityLexiconEvaluator(
					EMOTICON_LIST_FILE_NAME,"Emoticons");
			try {
				emoticonLex.processDict();
				this.lexicons.add(emoticonLex);
			} catch (IOException e) {
				this.useEmoticons=false;
			}		

		}



		if(this.useNegation){
			LexiconEvaluator negationLex = new NegationEvaluator(
					NEGATION_LIST_FILE_NAME,"Negation");
			try {
				negationLex.processDict();
				this.lexicons.add(negationLex);
			} catch (IOException e) {
				this.useNegation=false;
			}		

		}



	}


	@Override
	protected Instances determineOutputFormat(Instances inputFormat)
			throws Exception {

		ArrayList<Attribute> att = new ArrayList<Attribute>();

		// Adds all attributes of the inputformat
		for (int i = 0; i < inputFormat.numAttributes(); i++) {
			att.add(inputFormat.attribute(i));
		}


		// The dictionaries of the lexicons are intialized only in the first batch
		if(!this.isFirstBatchDone())
			this.initializeDicts();


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
			List<String> words = affective.core.Utils.tokenize(content, this.toLowerCase, this.cleanTokens);

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


	public boolean isUseMpqa() {
		return useMpqa;
	}


	public void setUseMpqa(boolean useMpqa) {
		this.useMpqa = useMpqa;
	}


	public boolean isUseBingLiu() {
		return useBingLiu;
	}


	public void setUseBingLiu(boolean useBingLiu) {
		this.useBingLiu = useBingLiu;
	}



	public boolean isUseAfinn() {
		return useAfinn;
	}


	public void setUseAfinn(boolean useAfinn) {
		this.useAfinn = useAfinn;
	}



	public boolean isUseS140() {
		return useS140;
	}


	public void setUseS140(boolean useS140) {
		this.useS140 = useS140;
	}




	public boolean isUseNrcHashSent() {
		return useNrcHashSent;
	}


	public void setUseNrcHashSent(boolean useNrcHashSent) {
		this.useNrcHashSent = useNrcHashSent;
	}



	public boolean isUseNrc10() {
		return useNrc10;
	}


	public void setUseNrc10(boolean useNrc10) {
		this.useNrc10 = useNrc10;
	}




	public boolean isUseNrc10Expanded() {
		return useNrc10Expanded;
	}


	public void setUseNrc10Expanded(boolean useNrc10Expanded) {
		this.useNrc10Expanded = useNrc10Expanded;
	}


	public boolean isUseNrcHashEmo() {
		return useNrcHashEmo;
	}


	public void setUseNrcHashEmo(boolean useNrcHashEmo) {
		this.useNrcHashEmo = useNrcHashEmo;
	}

	public boolean isUseEmoticons() {
		return useEmoticons;
	}


	public void setUseEmoticons(boolean useEmoticons) {
		this.useEmoticons = useEmoticons;
	}



	public boolean isUseNegation() {
		return useNegation;
	}


	public void setUseNegation(boolean useNegation) {
		this.useNegation = useNegation;
	}


	public boolean isToLowerCase() {
		return toLowerCase;
	}


	public void setToLowerCase(boolean toLowerCase) {
		this.toLowerCase = toLowerCase;
	}


	public boolean isCleanTokens() {
		return cleanTokens;
	}


	public void setCleanTokens(boolean cleanTokens) {
		this.cleanTokens = cleanTokens;
	}



}
