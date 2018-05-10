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
 *    LexiconDistantSupervision.java
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute;



import java.io.File;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import affective.core.ArffLexiconEvaluator;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionMetadata;
import weka.core.SingleIndex;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.WekaPackageManager;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Type;
import weka.core.tokenizers.Tokenizer;
import weka.core.tokenizers.TweetNLPTokenizer;
import weka.filters.SimpleBatchFilter;



/**
 *  <!-- globalinfo-start --> 
 *  A lexicon-based distant supervision method for training polarity classifiers in Twitter in the absence of labeled data. 
 *  A lexicon is used for labeling tweets. The trailing word can be removed from the content. Tweets with both positive and negative words are discarded. 
 *    
 * <!-- globalinfo-end -->
 * 
 *  
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class LexiconDistantSupervision  extends SimpleBatchFilter {



	/** For serialization.    **/
	private static final long serialVersionUID = 1616693021695150782L;


	/** Default path to where lexicons are stored */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons"+ File.separator + "arff_lexicons";


	/** The path of the seed lexicon. */
	protected File lexicon=new File(LEXICON_FOLDER_NAME+File.separator+"emoticons.arff");


	/** The tokenizer. */
	protected Tokenizer m_tokenizer=new TweetNLPTokenizer();

	/** The index of the string attribute to be processed. */
	protected SingleIndex m_textIndex = new SingleIndex("1");


	/** True for removing the trailing labeling word from the tweet's content. */
	protected boolean removeMatchingWord=true;


	/** The target lexicon attribute name. */
	protected String polarityAttName="polarity";


	/** Object containing the lexicon to use. */
	protected ArffLexiconEvaluator lex=new ArffLexiconEvaluator();



	/** The positive attribute value name from the lexicon arff file. */
	protected String polarityAttPosValName="positive";


	/** The negative attribute value name from the lexicon arff file. */
	protected String polarityAttNegValName="negative";





	/**
	 * Returns an instance of a TechnicalInformation object, containing
	 * detailed information about the technical background of this class,
	 * e.g., paper reference or book this class is based on.
	 *
	 * @return the technical information about this class
	 */
	public TechnicalInformation getTechnicalInformation() {
		TechnicalInformation result;
		result = new TechnicalInformation(Type.MISC);
		result.setValue(TechnicalInformation.Field.AUTHOR, " Alec Go, Richa Bhayani and  Lei Huang");
		result.setValue(TechnicalInformation.Field.TITLE, "Twitter sentiment classification using distant supervision");
		result.setValue(TechnicalInformation.Field.YEAR, "2009");
		result.setValue(TechnicalInformation.Field.BOOKTITLE, "CS224N Project Report, Stanford");
		return result;
	}





	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#globalInfo()
	 */
	@Override
	public String globalInfo() {
		return "This is a lexicon-based distant supervision method for training polarity classifiers in Twitter in the absence of labeled data. " +
				"A lexicon is used for labeling tweets. If a word from the lexicon is found, the tweet is labeled with the word's polarity. "
				+ "Tweets with both positive and negative words are discarded. Emoticons are used as the default lexicon."+
				"\n"+getTechnicalInformation().toString();
	}



	/* (non-Javadoc)
	 * @see weka.filters.Filter#listOptions()
	 */
	@Override
	public Enumeration<Option> listOptions() {
		//this.getClass().getSuperclass()
		return Option.listOptionsForClassHierarchy(this.getClass(), this.getClass().getSuperclass()).elements();
	}


	/* (non-Javadoc)
	 * @see weka.filters.Filter#getOptions()
	 */
	@Override
	public String[] getOptions() {	
		return Option.getOptionsForHierarchy(this, this.getClass().getSuperclass());

		//return Option.getOptions(this, this.getClass());
	}




	/**
	 * Parses the options for this object.
	 *  
	 * @param options
	 *            the options to use
	 * @throws Exception
	 *             if setting of options fails
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		Option.setOptionsForHierarchy(options, this, this.getClass().getSuperclass());
	}


	/* (non-Javadoc)
	 * @see weka.filters.Filter#getCapabilities()
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





	/* To allow determineOutputFormat to access to entire dataset
	 * (non-Javadoc)
	 * @see weka.filters.SimpleBatchFilter#allowAccessToFullInputFormat()
	 */
	public boolean allowAccessToFullInputFormat() {
		return true;
	}



	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#determineOutputFormat(weka.core.Instances)
	 */
	@Override
	protected Instances determineOutputFormat(Instances inputFormat) {


		// set upper value for text index
		m_textIndex.setUpper(inputFormat.numAttributes() - 1);

		this.lex.setLexiconFile(this.lexicon);

		if(!this.isFirstBatchDone()){

			try {
				this.lex.processDict();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		ArrayList<Attribute> att = new ArrayList<Attribute>();

		// Adds all attributes of the inputformat
		for (int i = 0; i < inputFormat.numAttributes(); i++) {
			att.add(inputFormat.attribute(i));
		}


		// adds the binary labels
		List<String> attValues=new ArrayList<String>();
		attValues.add(this.polarityAttNegValName);
		attValues.add(this.polarityAttPosValName);		
		att.add(new Attribute(this.polarityAttName,attValues));



		Instances result = new Instances(inputFormat.relationName(), att, 0);

		// set the class index
		result.setClassIndex(inputFormat.classIndex());

		return result;
	}


	/**
	 * Creates a pattern for removing sentiment words from tweets (special characters are quoted)
	 * @param words the list of words
	 * @return a pattern 
	 */
	private static String patternFromList(List<String> words){
		String pattern="";
		for(String word:words){
			pattern += Pattern.quote(word)+"|";
		}		
		return pattern.substring(0, pattern.length()-1);
		
	}


	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#process(weka.core.Instances)
	 */
	@Override
	protected Instances process(Instances instances) throws Exception {


		// set upper value for text index
		m_textIndex.setUpper(instances.numAttributes() - 1);

		Instances result = getOutputFormat();

		// reference to the content of the message, users index start from zero
		Attribute attrCont = instances.attribute(this.m_textIndex.getIndex());


		for (int i = 0; i < instances.numInstances(); i++) {

			boolean addTweet=false;

			String content = instances.instance(i).stringValue(attrCont);

			ArrayList<String> posWords=new ArrayList<String>();
			ArrayList<String> negWords=new ArrayList<String>();

			this.m_tokenizer.tokenize(content);
			for(;this.m_tokenizer.hasMoreElements();){
				String word=this.m_tokenizer.nextElement();
				if(this.lex.getNomDict().containsKey(word)){
					String value=this.lex.getNomDict().get(word).get(this.polarityAttName);
					if(value.equals(this.polarityAttPosValName))
						posWords.add(word);
					else if(value.equals(this.polarityAttNegValName))
						negWords.add(word);
				}

			}

			if(posWords.size()>0 && negWords.size()==0){
				addTweet=true;				
				// the matching words are removed from the content if flag is set
				if(this.removeMatchingWord)
					content=content.replaceAll(patternFromList(posWords), "");			
			}
			else if(	negWords.size()>0 && posWords.size()==0){
				addTweet=true;
				if(this.removeMatchingWord)
					content=content.replaceAll(patternFromList(negWords), "");				
			}

			if(addTweet){
				double[] values = new double[result.numAttributes()];

				// copy other attributes
				for (int n = 0; n < instances.numAttributes(); n++){
					if(n!=this.m_textIndex.getIndex())
						values[n] = instances.instance(i).value(n);
				}

				// add the content
				values[this.m_textIndex.getIndex()]= attrCont.addStringValue(content);

				// label tweet according to the word's polarity
				if(posWords.size()>0)
					values[result.numAttributes()-1]=1;
				else
					values[result.numAttributes()-1]=0;


				Instance inst = new SparseInstance(1, values);

				inst.setDataset(result);

				// copy possible strings, relational values...
				copyValues(inst, false, instances, result);

				result.add(inst);

			}

		}

		return result;
	}



	@OptionMetadata(displayName = "textIndex",
			description = "The index (starting from 1) of the target string attribute. First and last are valid values. ",
			commandLineParamName = "I", commandLineParamSynopsis = "-I <col>",
			displayOrder = 0)
	public String getTextIndex() {
		return m_textIndex.getSingleIndex();
	}
	public void setTextIndex(String textIndex) {
		this.m_textIndex.setSingleIndex(textIndex);
	}

		


	@OptionMetadata(displayName = "tokenizer",
			description = "The tokenizing algorithm to use on the tweets. Uses the CMU TweetNLP tokenizer as default",
			commandLineParamName = "tokenizer",
			commandLineParamSynopsis = "-tokenizer <string>", displayOrder = 1)		
	public Tokenizer getTokenizer() {
		return m_tokenizer;
	}
	public void setTokenizer(Tokenizer m_tokenizer) {
		this.m_tokenizer = m_tokenizer;
	}

	@OptionMetadata(displayName = "lexicon",
			description = "The file containing a lexicon in ARFF format with word polarities.", 
			commandLineParamName = "lex", 
			commandLineParamSynopsis = "-lex <string>",
			displayOrder = 2)	
	public File getLexicon() {
		return lexicon;
	}
	public void setLexicon(File lexicon) {
		this.lexicon = lexicon;
	}

	
	
	@OptionMetadata(displayName = "polarityAttName",
			description = "The lexicon attribute name with the word polarities. The attribute must be nominal."
					+ " \t default: polarity", 
			commandLineParamName = "polatt", 
			commandLineParamSynopsis = "-polatt <string>",
			displayOrder = 3)	
	public String getPolarityAttName() {
		return polarityAttName;
	}
	public void setPolarityAttName(String polarityAttName) {
		this.polarityAttName = polarityAttName;
	}


	@OptionMetadata(displayName = "polarityAttPosValName",
			description = "The lexicon attribute value name for positive words. \t default: positive", 
			commandLineParamName = "posval", 
			commandLineParamSynopsis = "-posval <String>",
			displayOrder = 4)
	public String getPolarityAttPosValName() {
		return polarityAttPosValName;
	}
	public void setPolarityAttPosValName(String polarityAttPosValName) {
		this.polarityAttPosValName = polarityAttPosValName;
	}




	@OptionMetadata(displayName = "polarityAttNegValName",
			description = "The lexicon attribute value name for negative words. \t default: negative", 
			commandLineParamName = "negval", 
			commandLineParamSynopsis = "-negval <String>",
			displayOrder = 5)
	public String getPolarityAttNegValName() {
		return polarityAttNegValName;
	}
	public void setPolarityAttNegValName(String polarityAttNegValName) {
		this.polarityAttNegValName = polarityAttNegValName;
	}
	
	
	

	
	
	@OptionMetadata(displayName = "removeMatchingWord",
			description = "True for removing the words from the lexicon in the tweet. "
					+ "This avoids overfitting classifiers trained from the labelled data. \t default: TRUE", 
					commandLineParamIsFlag = true, commandLineParamName = "removeMatchingWord", 
					commandLineParamSynopsis = "-removeMatchingWord",
					displayOrder = 6)		
	public boolean isRemoveMatchingWord() {
		return removeMatchingWord;
	}
	public void setRemoveMatchingWord(boolean removeMatchingWord) {
		this.removeMatchingWord = removeMatchingWord;
	}
	

	/**
	 * Main method for testing this class.
	 *
	 * @param args should contain arguments to the filter: use -h for help
	 */	
	public static void main(String[] args) {
		runFilter(new LexiconDistantSupervision(), args);
	}

}
