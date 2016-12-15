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
 *    TweetToEmbeddingsFeatureVector.java
 *    Copyright (C) 1999-2016 University of Waikato, Hamilton, New Zealand
 *
 */

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
import affective.core.SWN3LexiconEvaluator;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.Utils;
import weka.core.WekaPackageManager;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Type;
import weka.filters.SimpleBatchFilter;

/**
 *  <!-- globalinfo-start --> An attribute filter that calculates lexicon-based features 
 *  for a tweet represented as a string attribute. Different lexicons are used. 
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;Article{BravoMarquez2014,
 * Title                    = {Meta-level sentiment models for big social data analysis },
 * Author                   = {Felipe Bravo-Marquez and Marcelo Mendoza and Barbara Poblete},
 * Journal                  = {Knowledge-Based Systems },
 * Year                     = {2014},
 * Number                   = {0},
 * Pages                    = {86 - 99},
 * Volume                   = {69},
 * Doi                      = {http://dx.doi.org/10.1016/j.knosys.2014.05.016},
 * ISSN                     = {0950-7051},
 * Keywords                 = {Sentiment classification},
 * Url                      = {http://www.sciencedirect.com/science/article/pii/S0950705114002068}
 * }
 * </pre>
 * <p/>
 <!-- technical-bibtex-end -->
 *  
 * 
 * @author Felipe Bravo-Marquez (fjb11@students.waikato.ac.nz)
 * @version $Revision: 1 $
 */


public class TweetToLexiconFeatureVector extends SimpleBatchFilter {

	/** For serialization  */
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
	
	
	/** The path of SentiWordnet */
	public static String SENTIWORDNET_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"SentiWordNet_3.0.0.txt.gz";
	
	
	
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


	/** True for calculating features from SentiWordnet */
	protected boolean useSentiWordnet=true;

	/** True for calculating features from the Emoticon list */
	protected boolean useEmoticons=true;

	/** True for calculating features from the Negation list */
	protected boolean useNegation=true;

	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;


	/** True if url, users, and repeated letters are cleaned */
	protected boolean cleanTokens=false;

	/** List of Lexicons to use */
	private List<LexiconEvaluator> lexicons=new ArrayList<LexiconEvaluator>();


	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */	
	@Override
	public String globalInfo() {
		return "A batch filter that calcuates attributes for a tweet using different lexical resources for sentiment analysis.\n"
				+ getTechnicalInformation().toString();
	}

	/**
	 * Returns an instance of a TechnicalInformation object, containing
	 * detailed information about the technical background of this class,
	 * e.g., paper reference or book this class is based on.
	 *
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;
		result = new TechnicalInformation(Type.ARTICLE);
		result.setValue(TechnicalInformation.Field.AUTHOR, "Felipe Bravo-Marquez and Marcelo Mendoza and Barbara Poblete");
		result.setValue(TechnicalInformation.Field.TITLE, "Meta-level sentiment models for big social data analysis");
		result.setValue(TechnicalInformation.Field.YEAR, "2014");
		result.setValue(TechnicalInformation.Field.JOURNAL, "Knowledge-Based Systems");
		result.setValue(TechnicalInformation.Field.URL, "http://www.sciencedirect.com/science/article/pii/S0950705114002068");

		return result;
	}


	/**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 */
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
				"\t use SentiWordnet\n"
						+ "\t(default:"+this.useSentiWordnet+")", "Q", 0, "-Q"));


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
		
		if(this.isUseSentiWordnet())
			result.add("-Q");

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
	 * <!-- options-start --> 
	 * <pre> 
	 *-I &lt;col&gt;
	 *  Index of string attribute (default: 1)
	 * </pre>
	 * <pre>
	 *-A 
	 *  use MPQA Lexicon (default:false)
	 * </pre>
	 * <pre>
	 *-D 
	 *	 use Bing Liu Lexicon (default:false)
	 * </pre>
	 * <pre>
	 *-F 
	 *	 use AFINN Lexicon (default:false)
	 * </pre>
	 * <pre>
	 *-H 
	 *	 use S140 Lexicon (default:false)
	 * </pre>
	 * <pre>
	 *-J 
	 *	 use NRC-Hash-Sent Lexicon (default:false)
	 * </pre>
	 * <pre>
	 *-L 
	 *	 use NRC-10 Emotion Lexicon	(default:false)
	 * </pre>
	 * <pre>
	 *-N 
	 *	 use NRC-10-Expanded Emotion Lexicon	(default:false)
	 * </pre>
	 * <pre>
	 *-P 
	 *	 use NRC Hashtag Emotion Lexicon (default:false)
	 * </pre>
	 *<pre>
	 *-Q 
	 *	 use SentiWordNet (default:false)
	 * </pre>
	 * <pre>
	 *-R 
	 *	 use Emoticon List (default:false)
	 * </pre>
	 * <pre>
	 *-T  
	 *	 use Negation List (default:false)
	 * </pre>
	 * <pre>
	 *-U 
	 *	 Lowercase content	(default: false)
	 * </pre>
	 * <pre>
	 *-O 
	 *	 Clean tokens (replace goood by good, standarise URLs and @users) 	(default: false)
	 *</pre> 
	 *  
	 * <!-- options-end -->
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
		
		this.useSentiWordnet=Utils.getFlag('Q', options);


		this.useEmoticons=Utils.getFlag('R', options);


		this.useNegation=Utils.getFlag('T', options);


		this.toLowerCase=Utils.getFlag('U', options);

		this.cleanTokens=Utils.getFlag('O', options);

		super.setOptions(options);

		Utils.checkForRemainingOptions(options);


	}

	/**
	 * Returns the Capabilities of this filter.
	 * 
	 * @return the capabilities of this object
	 * @see Capabilities
	 */
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




	/**
	 * Initializes the dictionaries of all the lexicons to use. 
	 */
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
		
		

		if(this.useSentiWordnet){
			LexiconEvaluator sentiWordEvaluator = new SWN3LexiconEvaluator(
					SENTIWORDNET_FILE_NAME,"SentiWordnet");
			try {
				sentiWordEvaluator.processDict();
				this.lexicons.add(sentiWordEvaluator);
			} catch (IOException e) {
				this.useSentiWordnet=false;

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

	/**
	 * Determines the output format based on the input format and returns this. In
	 * case the output format cannot be returned immediately, i.e.,
	 * immediateOutputFormat() returns false, then this method will be called from
	 * batchFinished().
	 * 
	 * @param inputFormat the input format to base the output format on
	 * @return the output format
	 * @throws Exception in case the determination goes wrong
	 * @see #hasImmediateOutputFormat()
	 * @see #batchFinished()
	 */
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


	/**
	 * Processes the given data (may change the provided dataset) and returns the
	 * modified version. This method is called in batchFinished().
	 * 
	 * @param instances the data to process
	 * @return the modified data
	 * @throws Exception in case the processing goes wrong
	 * @see #batchFinished()
	 */
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


	/**
	 * Get the position of the target string.
	 * 
	 * @return the index of the target string
	 */	
	public int getTextIndex() {
		return textIndex;
	}


	/**
	 * Set the attribute's index with the string to process.
	 * 
	 * @param textIndex the index value name
	 */
	public void setTextIndex(int textIndex) {
		this.textIndex = textIndex;
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String textIndexTipText() {

		return "The index (starting from 1) of the target string attribute." ;
	}


	/**
	 * Get the isUseMpqa value.
	 *
	 * @return the isUseMpqa  value.
	 */	
	public boolean isUseMpqa() {
		return useMpqa;
	}


	/**
	 * Set the useMpqa value.
	 *
	 * @param useMpqa The useMpqa value.
	 */	
	public void setUseMpqa(boolean useMpqa) {
		this.useMpqa = useMpqa;
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String useMpqaIndexTipText() {

		return "Counts the number of positive and negative words from the MPQA subjectivity lexicon.\n"
				+ "More info at: http://mpqa.cs.pitt.edu/lexicons/subj_lexicon/ \n"
				+ "Publication: Theresa Wilson, Janyce Wiebe, and Paul Hoffmann, Recognizing Contextual Polarity in Phrase-Level Sentiment Analysis." ;
	}

	/**
	 * Get the isUseBingLiu value.
	 *
	 * @return the isUseBingLiu  value.
	 */	
	public boolean isUseBingLiu() {
		return useBingLiu;
	}

	/**
	 * Set the useBingLiu value.
	 *
	 * @param useBingLiu The useBingLiu value.
	 */	
	public void setUseBingLiu(boolean useBingLiu) {
		this.useBingLiu = useBingLiu;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String useBingLiuTipText() {

		return "Counts the number of positive and negative words from the Bing Liu lexicon.\n"
				+ "More info at: https://www.cs.uic.edu/~liub/FBS/sentiment-analysis.html#lexicon \n"
				+ "Publication: Minqing Hu and Bing Liu, Mining and summarizing customer reviews.";
	}	


	/**
	 * Get the isUseAfinn value.
	 *
	 * @return the isUseAfinn  value.
	 */	
	public boolean isUseAfinn() {
		return useAfinn;
	}

	/**
	 * Set the useAfinn value.
	 *
	 * @param useAfinn The useAfinn value.
	 */	
	public void setUseAfinn(boolean useAfinn) {
		this.useAfinn = useAfinn;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String useAfinnTipText() {

		return "Calculates a positive and negative score by aggregating the word associations provided by the AFINN lexicon.\n"
				+ "More info at: http://www2.imm.dtu.dk/pubdb/views/publication_details.php?id=6010 \n"
				+ "Publication: Finn Arup Nielsen, A new ANEW: Evaluation of a word list for sentiment analysis in microblogs";
	}	


	/**
	 * Get the isUseS140 value.
	 *
	 * @return the isUseS140  value.
	 */	
	public boolean isUseS140() {
		return useS140;
	}

	/**
	 * Set the useS140 value.
	 *
	 * @param useS140 The useS140 value.
	 */	
	public void setUseS140(boolean useS140) {
		this.useS140 = useS140;
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String useS140TipText() {

		return "Calculates a positive and negative score by aggregating the word associations provided by the S140 lexicon.\n"
				+ "More info at: http://saifmohammad.com/WebPages/lexicons.html \n"
				+ "Publication: Svetlana Kiritchenko, Xiaodan Zhu and Saif Mohammad, Sentiment Analysis of Short Informal Texts." ;
	}

	/**
	 * Get the isUseNrcHashSent value.
	 *
	 * @return the isUseNrcHashSent  value.
	 */	
	public boolean isUseNrcHashSent() {
		return useNrcHashSent;
	}

	/**
	 * Set the useNrcHashSent value.
	 *
	 * @param useNrcHashSent The useNrcHashSent value.
	 */	
	public void setUseNrcHashSent(boolean useNrcHashSent) {
		this.useNrcHashSent = useNrcHashSent;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String useNrcHashSentTipText() {

		return "Calculates a positive and negative score by aggregating the word associations provided by the NRC Hashtag Sentiment lexicon.\n"
				+ "More info at: http://saifmohammad.com/WebPages/lexicons.html \n"
				+ "Publication: Svetlana Kiritchenko, Xiaodan Zhu and Saif Mohammad, Sentiment Analysis of Short Informal Texts." ;
	}	


	/**
	 * Get the isUseNrc10 value.
	 *
	 * @return the isUseNrc10  value.
	 */	
	public boolean isUseNrc10() {
		return useNrc10;
	}

	/**
	 * Set the useNrc10 value.
	 *
	 * @param useNrc10 The useNrc10 value.
	 */	
	public void setUseNrc10(boolean useNrc10) {
		this.useNrc10 = useNrc10;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String useNrc10TipText() {

		return "Counts the number of words matching each emotion from the NRC Word-Emotion Association Lexicon.\n"
				+ "More info at: http://saifmohammad.com/WebPages/NRC-Emotion-Lexicon.htm\n"
				+ "Publication: Saif Mohammad and Peter Turney, Crowdsourcing a Word-Emotion Association Lexicon." ;
	}	


	/**
	 * Get the isUseNrc10Expanded value.
	 *
	 * @return the isUseNrc10Expanded value.
	 */	
	public boolean isUseNrc10Expanded() {
		return useNrc10Expanded;
	}

	/**
	 * Set the useNrc10Expanded value.
	 *
	 * @param useNrc10Expanded The useNrc10Expanded value.
	 */	
	public void setUseNrc10Expanded(boolean useNrc10Expanded) {
		this.useNrc10Expanded = useNrc10Expanded;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String useNrc10ExpandedTipText() {

		return "Sums the emotion associations of the words matching the Twitter Specific expansion of the NRC Word-Emotion Association Lexicon.\n"
				+ "More info at: http://www.cs.waikato.ac.nz/ml/sa/lex.html#emolextwitter\n"
				+ "Publication: F. Bravo-Marquez, E. Frank, S. M. Mohammad, and B. Pfahringer, Determining Word--Emotion Associations from Tweets by Multi-Label Classification.";
	}	


	/**
	 * Get the useNrcHashEmo value.
	 *
	 * @return the useNrcHashEmo value.
	 */		
	public boolean isUseNrcHashEmo() {
		return useNrcHashEmo;
	}

	/**
	 * Set the useNrcHashEmo value.
	 *
	 * @param useNrcHashEmo The useNrcHashEmo value.
	 */	
	public void setUseNrcHashEmo(boolean useNrcHashEmo) {
		this.useNrcHashEmo = useNrcHashEmo;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String useNrcHashEmoTipText() {
		return "Sums the emotion associations of the words matching the NRC Hashtag Emotion Association Lexicon.\n"
				+ "More info at: http://saifmohammad.com/WebPages/lexicons.html\n"
				+ "Publication: Saif M. Mohammad, Svetlana Kiritchenko, Using Hashtags to Capture Fine Emotion Categories from Tweets.";
	}		


	/**
	 * Get the useSentiWordnet value.
	 *
	 * @return the useSentiWordnet value.
	 */		
	public boolean isUseSentiWordnet() {
		return useSentiWordnet;
	}

	
	/**
	 * Set the uuseSentiWordnet value.
	 *
	 * @param useSentiWordnet The useNrcHashEmo value.
	 */		
	public void setUseSentiWordnet(boolean useSentiWordnet) {
		this.useSentiWordnet = useSentiWordnet;
	}
	
	
	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String useSentiWordnetTipText() {
		return "Calculates positive and negative scores using SentiWordnet. We calculate a weighted average of the sentiment distributions of the synsets in which a "
				+ "word occurs in order to obtain a single sentiment distribution for it.  The weights correspond to the reciprocal ranks of the senses in order to give "
				+ "higher weights to the most popular senses of a word. \n"
				+ "More info at: http://sentiwordnet.isti.cnr.it/\n"
				+ "Publication: Stefano Baccianella, Andrea Esuli, and Fabrizio Sebastiani, SENTIWORDNET 3.0: An Enhanced Lexical Resource for Sentiment Analysis"
				+ " and Opinion Mining.";
	}	
	
	
	

	/**
	 * Get the isUseEmoticons value.
	 *
	 * @return the isUseEmoticons  value.
	 */		
	public boolean isUseEmoticons() {
		return useEmoticons;
	}

	/**
	 * Set the useEmoticons value.
	 *
	 * @param useEmoticons The useEmoticons value.
	 */	
	public void setUseEmoticons(boolean useEmoticons) {
		this.useEmoticons = useEmoticons;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String useEmoticonsTipText() {

		return "Calculates a positive and negative score by aggregating the word associations provided by a list of emoticons.\n"
				+ "The list is taken from the AFINN project.\n"
				+ "More info at: https://github.com/fnielsen/afinn \n";
	}



	/**
	 * Get the isUseNegation value.
	 *
	 * @return the isUseNegation  value.
	 */		
	public boolean isUseNegation() {
		return useNegation;
	}

	/**
	 * Set the useNegation value.
	 *
	 * @param useNegation The useNegation value.
	 */	
	public void setUseNegation(boolean useNegation) {
		this.useNegation = useNegation;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String useNegationTipText() {

		return "Counts the number of negating words in the tweet." ;
	}



	/**
	 * Gets the value of the lowercase flag.
	 * 
	 * @return the value of the flag.
	 */
	public boolean isToLowerCase() {
		return toLowerCase;
	}

	/**
	 * Sets the value of the lowercase flag.
	 * 
	 * @param toLowerCase the value of the flag.
	 * 
	 */
	public void setToLowerCase(boolean toLowerCase) {
		this.toLowerCase = toLowerCase;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String lowerCaseTipText() {
		return "Lowercase the tweet's content.";
	}


	/**
	 * Gets the value of the cleanTokens option.
	 * 
	 * @return the value of the flag.
	 */
	public boolean isCleanTokens() {
		return cleanTokens;
	}

	/**
	 * Sets the value of the cleanTokens flag.
	 * 
	 * @param cleanTokens the value of the flag.
	 * 
	 */
	public void setCleanTokens(boolean cleanTokens) {
		this.cleanTokens = cleanTokens;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String cleanTokensTipText() {
		return "Reduce the attribute space by replacing sequences of letters occurring more than two "
				+ "times in a row with two occurrences of them (e.g., huuungry is reduced to huungry, loooove to loove), "
				+ "and replacing user mentions and URLs with generic tokens.";		
	}




}
