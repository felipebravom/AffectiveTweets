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
 *    TweetToLexiconFeatureVector.java
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import affective.core.IntensityLexiconEvaluator;
import affective.core.LexiconEvaluator;
import affective.core.NRCEmotionLexiconEvaluator;
import affective.core.NRCExpandedEmotionLexiconEvaluator;
import affective.core.NRCHashtagEmotionLexiconEvaluator;
import affective.core.NegationEvaluator;
import affective.core.PolarityLexiconEvaluator;
import affective.core.SWN3LexiconEvaluator;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionMetadata;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.WekaPackageManager;
import weka.core.TechnicalInformation.Type;

/**
 *  <!-- globalinfo-start --> An attribute filter that calculates lexicon-based features 
 *  for a tweet represented as a string attribute. Different lexicons are used. 
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
 <!-- technical-bibtex-end -->
 *  
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 2 $
 */


public class TweetToLexiconFeatureVector extends TweetToFeatureVector {

	/** For serialization.  */
	private static final long serialVersionUID = 4983739424598292130L;

	/** Default path to where lexicons are stored. */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons";

	/** The path of the MPQA lexicon. */
	public static String MPQA_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"mpqa.txt.gz";

	/** The path of the BingLiu lexicon. */
	public static String BING_LIU_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"BingLiu.csv.gz";

	/** The path of the AFINN lexicon. */
	public static String AFINN_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"AFINN-en-165.txt.gz";

	/** The path of the S140 lexicon. */
	public static String S140_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"Sentiment140-Lexicon-v0.1"+java.io.File.separator+"unigrams-pmilexicon.txt.gz";

	/** The path of the NRC-Hashtag-Sentiment lexicon. */
	public static String NRC_HASH_SENT_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"NRC-Hashtag-Sentiment-Lexicon-v0.1"+java.io.File.separator+"unigrams-pmilexicon.txt.gz";

	/** The path of the NRC-emotion lexicon. */
	public static String NRC10_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"NRC-emotion-lexicon-wordlevel-v0.92.txt.gz";

	/** The path of the NRC-10-Expanded lexicon. */
	public static String NRC10_EXPANDED_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"w2v-dp-BCC-Lex.csv.gz";


	/** The path of the NRC Hashtag Emotion lexicon. */
	public static String NRC_HASH_EMO_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"NRC-Hashtag-Emotion-Lexicon-v0.2.txt.gz";


	/** The path of SentiWordnet. */
	public static String SENTIWORDNET_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"SentiWordNet_3.0.0.txt.gz";



	/** The path of the emoticon list. */
	public static String EMOTICON_LIST_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"AFINN-emoticon-8.txt.gz";


	/** The path of the negation list. */
	public static String NEGATION_LIST_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"NegatingWordList.txt.gz";


	/** True for calculating features from the MPQA lexicon. */
	protected boolean useMpqa=true;

	/** True for calculating features from the BingLiu lexicon. */
	protected boolean useBingLiu=true;

	/** True for calculating features from the AFINN lexicon. */
	protected boolean useAfinn=true;


	/** True for calculating features from the S140 lexicon. */
	protected boolean useS140=true;


	/** True for calculating features from the NRC-Hashtag-Sentiment lexicon. */
	protected boolean useNrcHashSent=true;


	/** True for calculating features from the NRC-emotion lexicon. */
	protected boolean useNrc10=true;


	/** True for calculating features from the NRC-10-Expanded lexicon. */
	protected boolean useNrc10Expanded=true;


	/** True for calculating features from the NRC Hashtag Emotion Lexicon. */
	protected boolean useNrcHashEmo=true;


	/** True for calculating features from SentiWordnet. */
	protected boolean useSentiWordnet=true;

	/** True for calculating features from the Emoticon list. */
	protected boolean useEmoticons=true;

	/** True for calculating features from the Negation list. */
	protected boolean useNegation=true;


	/** List of Lexicons to use. */
	private List<LexiconEvaluator> lexicons=new ArrayList<LexiconEvaluator>();


	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */	
	@Override
	public String globalInfo() {
		return "A batch filter that calcuates attributes for a tweet using multiple affective lexicons.\n"
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


	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#determineOutputFormat(weka.core.Instances)
	 */
	@Override
	protected Instances determineOutputFormat(Instances inputFormat)
			throws Exception {

		// set upper value for text index
		m_textIndex.setUpper(inputFormat.numAttributes() - 1);

		ArrayList<Attribute> att = new ArrayList<Attribute>();

		// Adds all attributes of the inputformat
		for (int i = 0; i < inputFormat.numAttributes(); i++) {
			att.add(inputFormat.attribute(i));
		}


		// The dictionaries of the lexicons are initialized only in the first batch
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



	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#process(weka.core.Instances)
	 */
	@Override
	protected Instances process(Instances instances) throws Exception {


		Instances result = getOutputFormat();


		// reference to the content of the message
		Attribute attrCont = instances.attribute(this.m_textIndex.getIndex());


		for (int i = 0; i < instances.numInstances(); i++) {
			double[] values = new double[result.numAttributes()];
			for (int n = 0; n < instances.numAttributes(); n++)
				values[n] = instances.instance(i).value(n);

			String content = instances.instance(i).stringValue(attrCont);
			List<String> words = affective.core.Utils.tokenize(content, this.toLowerCase, this.standarizeUrlsUsers, this.reduceRepeatedLetters, this.m_tokenizer,this.m_stemmer,this.m_stopwordsHandler);


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


	@OptionMetadata(displayName = "useMpqa",
			description = "Counts the number of positive and negative words from the MPQA subjectivity lexicon.\n"
					+ "More info at: http://mpqa.cs.pitt.edu/lexicons/subj_lexicon/ \n"
					+ "Publication: Theresa Wilson, Janyce Wiebe, and Paul Hoffmann, Recognizing Contextual Polarity in Phrase-Level Sentiment Analysis.",
					commandLineParamIsFlag = true, 
					commandLineParamName = "A", 
					commandLineParamSynopsis = "-A",
					displayOrder = 6)		
	public boolean isUseMpqa() {
		return useMpqa;
	}
	public void setUseMpqa(boolean useMpqa) {
		this.useMpqa = useMpqa;
	}


	@OptionMetadata(displayName = "useBingLiu",
			description = "Counts the number of positive and negative words from the Bing Liu lexicon.\n"
					+ "More info at: https://www.cs.uic.edu/~liub/FBS/sentiment-analysis.html#lexicon \n"
					+ "Publication: Minqing Hu and Bing Liu, Mining and summarizing customer reviews.",
					commandLineParamIsFlag = true, 
					commandLineParamName = "D", 
					commandLineParamSynopsis = "-D",
					displayOrder = 7)			
	public boolean isUseBingLiu() {
		return useBingLiu;
	}
	public void setUseBingLiu(boolean useBingLiu) {
		this.useBingLiu = useBingLiu;
	}


	@OptionMetadata(displayName = "useAfinn",
			description = "Calculates a positive and negative score by aggregating the word associations provided by the AFINN lexicon.\n"
					+ "More info at: http://www2.imm.dtu.dk/pubdb/views/publication_details.php?id=6010 \n"
					+ "Publication: Finn Arup Nielsen, A new ANEW: Evaluation of a word list for sentiment analysis in microblogs.",
					commandLineParamIsFlag = true, 
					commandLineParamName = "F", 
					commandLineParamSynopsis = "-F",
					displayOrder = 8)			
	public boolean isUseAfinn() {
		return useAfinn;
	}
	public void setUseAfinn(boolean useAfinn) {
		this.useAfinn = useAfinn;
	}


	@OptionMetadata(displayName = "useS140",
			description = "Calculates a positive and negative score by aggregating the word associations provided by the S140 lexicon.\n"
					+ "More info at: http://saifmohammad.com/WebPages/lexicons.html \n"
					+ "Publication: Svetlana Kiritchenko, Xiaodan Zhu and Saif Mohammad, Sentiment Analysis of Short Informal Texts.",
					commandLineParamIsFlag = true, 
					commandLineParamName = "H", 
					commandLineParamSynopsis = "-H",
					displayOrder = 9)			
	public boolean isUseS140() {
		return useS140;
	}
	public void setUseS140(boolean useS140) {
		this.useS140 = useS140;
	}

	@OptionMetadata(displayName = "useNrcHashSent",
			description = "Calculates a positive and negative score by aggregating the word associations provided by the NRC Hashtag Sentiment lexicon.\n"
					+ "More info at: http://saifmohammad.com/WebPages/lexicons.html \n"
					+ "Publication: Svetlana Kiritchenko, Xiaodan Zhu and Saif Mohammad, Sentiment Analysis of Short Informal Texts.",
					commandLineParamIsFlag = true, 
					commandLineParamName = "J", 
					commandLineParamSynopsis = "-J",
					displayOrder = 10)			
	public boolean isUseNrcHashSent() {
		return useNrcHashSent;
	}
	public void setUseNrcHashSent(boolean useNrcHashSent) {
		this.useNrcHashSent = useNrcHashSent;
	}


	@OptionMetadata(displayName = "useNrc10",
			description = "Counts the number of words matching each emotion from the NRC Word-Emotion Association Lexicon.\n"
					+ "More info at: http://saifmohammad.com/WebPages/NRC-Emotion-Lexicon.htm\n"
					+ "Publication: Saif Mohammad and Peter Turney, Crowdsourcing a Word-Emotion Association Lexicon.",
					commandLineParamIsFlag = true, 
					commandLineParamName = "L", 
					commandLineParamSynopsis = "-L",
					displayOrder = 11)		
	public boolean isUseNrc10() {
		return useNrc10;
	}
	public void setUseNrc10(boolean useNrc10) {
		this.useNrc10 = useNrc10;
	}


	@OptionMetadata(displayName = "useNrc10Expanded",
			description = "Adds the emotion associations of the words matching the Twitter Specific expansion of the NRC Word-Emotion Association Lexicon.\n"
					+ "More info at: http://www.cs.waikato.ac.nz/ml/sa/lex.html#emolextwitter\n"
					+ "Publication: F. Bravo-Marquez, E. Frank, S. M. Mohammad, and B. Pfahringer, Determining Word--Emotion Associations from Tweets by Multi-Label Classification.",
					commandLineParamIsFlag = true, 
					commandLineParamName = "N", 
					commandLineParamSynopsis = "-N",
					displayOrder = 12)			
	public boolean isUseNrc10Expanded() {
		return useNrc10Expanded;
	}
	public void setUseNrc10Expanded(boolean useNrc10Expanded) {
		this.useNrc10Expanded = useNrc10Expanded;
	}


	@OptionMetadata(displayName = "useNrcHashEmo",
			description = "Adds the emotion associations of the words matching the NRC Hashtag Emotion Association Lexicon.\n"
					+ "More info at: http://saifmohammad.com/WebPages/lexicons.html\n"
					+ "Publication: Saif M. Mohammad, Svetlana Kiritchenko, Using Hashtags to Capture Fine Emotion Categories from Tweets.",
					commandLineParamIsFlag = true, 
					commandLineParamName = "P", 
					commandLineParamSynopsis = "-P",
					displayOrder = 13)			
	public boolean isUseNrcHashEmo() {
		return useNrcHashEmo;
	}
	public void setUseNrcHashEmo(boolean useNrcHashEmo) {
		this.useNrcHashEmo = useNrcHashEmo;
	}



	@OptionMetadata(displayName = "useSentiWordnet",
			description = "Calculates positive and negative scores using SentiWordnet. We calculate a weighted average of the sentiment distributions of the synsets for "
					+ "word occuring in multiple synsets. The weights correspond to the reciprocal ranks of the senses in order to give "
					+ "higher weights to most popular senses. \n"
					+ "More info at: http://sentiwordnet.isti.cnr.it/\n"
					+ "Publication: Stefano Baccianella, Andrea Esuli, and Fabrizio Sebastiani, SENTIWORDNET 3.0: An Enhanced Lexical Resource for Sentiment Analysis"
					+ " and Opinion Mining.",
					commandLineParamIsFlag = true, 
					commandLineParamName = "Q", 
					commandLineParamSynopsis = "-Q",
					displayOrder = 14)			
	public boolean isUseSentiWordnet() {
		return useSentiWordnet;
	}
	public void setUseSentiWordnet(boolean useSentiWordnet) {
		this.useSentiWordnet = useSentiWordnet;
	}


	@OptionMetadata(displayName = "useEmoticons",
			description = "Calculates a positive and a negative score by aggregating the word associations provided by a list of emoticons.\n"
					+ "The list is taken from the AFINN project.\n"
					+ "More info at: https://github.com/fnielsen/afinn \n",
					commandLineParamIsFlag = true, 
					commandLineParamName = "R", 
					commandLineParamSynopsis = "-R",
					displayOrder = 15)			
	public boolean isUseEmoticons() {
		return useEmoticons;
	}
	public void setUseEmoticons(boolean useEmoticons) {
		this.useEmoticons = useEmoticons;
	}


	@OptionMetadata(displayName = "useNegation",
			description = "Counts the number of negating words in the tweet.",
			commandLineParamIsFlag = true, 
			commandLineParamName = "T", 
			commandLineParamSynopsis = "-T",
			displayOrder = 16)			
	public boolean isUseNegation() {
		return useNegation;
	}
	public void setUseNegation(boolean useNegation) {
		this.useNegation = useNegation;
	}



	/**
	 * Main method for testing this class.
	 *
	 * @param args should contain arguments to the filter: use -h for help
	 */		
	public static void main(String[] args) {
		runFilter(new TweetToLexiconFeatureVector(), args);
	}



}
