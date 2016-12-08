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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import affective.core.EmbeddingHandler;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.SelectedTag;
import weka.core.SparseInstance;
import weka.core.Tag;
import weka.core.TechnicalInformation;
import weka.core.Utils;
import weka.core.WekaPackageManager;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Type;
import weka.filters.SimpleBatchFilter;

/**
 *  <!-- globalinfo-start --> An attribute filter that calculates word embedding (word vectros) features 
 *  for a tweet represented as a string attribute. 
 *  The embeddings format is a csv.gz file with format: emb1<tab>emb2<tab>..word. 
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;inproceedings{bravo-marquez16:_deter_word_emotion_assoc_from,
 * author = {Felipe Bravo-Marquez and Eibe Frank and Saif M. Mohammad and Bernhard Pfahringer},
 * title = {Determining Word-Emotion Associations from Tweets by Multi-Label Classification},
 * booktitle = {Proc 15th IEEE/WIC/ACM International Conference on Web Intelligence},
 * year = 2016,
 * series = {Omaha, Nebraska},
 * publisher = {IEEE Computer Society},
 * pdf = {http://www.cs.waikato.ac.nz/~eibe/pubs/emo_lex_wi.pdf}
 * }
 * </pre>
 * <p/>
 <!-- technical-bibtex-end -->
 * 
 * 
 * 
 * @author Felipe Bravo-Marquez (fjb11@students.waikato.ac.nz)
 * @version $Revision: 1 $
 */


public class TweetToEmbeddingsFeatureVector extends SimpleBatchFilter {

	/** For serialization.    **/
	private static final long serialVersionUID = -823728822240437493L;

	/** Default path to where resources are stored, */
	public static String RESOURCES_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "resources";

	/** Average Action Value. */
	public static int AVERAGE_ACTION = 0;

	/** Default path to where resources are stored. */
	public static int ADD_ACTION = 1;

	/** Concatenate Action value. */
	public static int CONCATENATE_ACTION = 2;

	/** The action type. */
	public static final Tag[]      TAGS_ACTION           = {new Tag(AVERAGE_ACTION, "Add Embeddings"),
		new Tag(ADD_ACTION, "Average Embeddings"),
		new Tag(CONCATENATE_ACTION, "Concatenate first k embeddings")};


	/** The index of the string attribute to be processed. */
	protected int textIndex=1; 

	/** Embedding Handler.    **/
	protected EmbeddingHandler embeddingDict;

	/** Embedding File Name.    **/
	protected String embeddingFileName=RESOURCES_FOLDER_NAME + File.separator + "w2v.twitter.edinburgh.100d.csv.gz";


	/** The action in which embeddings are operated on the tweet. */
	protected int m_action=AVERAGE_ACTION;


	/** The number of word embeddings to concatenate. */
	protected int k=15;

	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;

	/** True if url, users, and repeated letters are cleaned. */
	protected boolean cleanTokens=true;


	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return "An attribute filter that calculates features for a string attribute  from "
				+ "given list of word vectors (embeddings). The embeddings format is a csv.gz "
				+ "file with format: value1<tab>value2<tab>...<tab>word .\n Pretrained word embeddings are provided in"
				+ RESOURCES_FOLDER_NAME+".\n"+getTechnicalInformation().toString();
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
		result = new TechnicalInformation(Type.INPROCEEDINGS);
		result.setValue(TechnicalInformation.Field.AUTHOR, "Felipe Bravo-Marquez, Eibe Frank, and Bernhard Pfahringer");
		result.setValue(TechnicalInformation.Field.TITLE, " Determining Word--Emotion Associations from Tweets by Multi-Label Classification");
		result.setValue(TechnicalInformation.Field.YEAR, "2016");
		result.setValue(TechnicalInformation.Field.BOOKTITLE, "Proceedings of the 2016 IEEE/WIC/ACM International Conference on Web Intelligence, Omaha, Nebraska, USA");
		result.setValue(TechnicalInformation.Field.URL, "http://researchcommons.waikato.ac.nz/handle/10289/10783");

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

		result.addElement(new Option("\t The index (starting from 1) of the target string attribute.\n"
				+ "\t(default: " + this.textIndex + ")", "I", 1, "-I"));

		result.addElement(new Option("\t The file containing the word embeddings. \n"
				+ "\t It has to be a gzip compressed csv file with the following structure:\n"
				+ "\t value1<tab>value2<tab>...<tab>word  "
				+ "\n"
				+ "\t(default: " + this.embeddingFileName + ")", "B", 1, "-B"));



		result.addElement(new Option("\tSet type of action (default: "+AVERAGE_ACTION+")\n" //
				+ "\t\t "+AVERAGE_ACTION+" -- Average embeddings" //
				+ "\t\t "+ADD_ACTION+" -- Add embeddings" //
				+ "\t\t "+CONCATENATE_ACTION+" -- Concatenate embeddings", //
				"S", 1, "-S <int>"));


		result.addElement(new Option("\t Number of words to concatenate.\n"
				+ "\t(default: " + this.k + ")", "K", 1, "-K"));

		result.addElement(new Option("\t Lowercase content.\n"
				+ "\t(default: " + this.toLowerCase + ")", "L", 0, "-L"));

		result.addElement(new Option("\t Normalize tokens (replace goood by good, normalize URLs and @users).\n"
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

		result.add("-B");
		result.add("" + this.getEmbeddingFileName());		

		result.add("-S");
		result.add("" + this.m_action);

		result.add("-K");
		result.add("" + this.getK());

		if(this.toLowerCase)
			result.add("-L");

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
	 * 
	 * <pre>
	 * -I
	 *	 Index of string attribute.
	 *	(default: 1)
	 * </pre>
	 * 
	 * <pre>
	 * -S &lt;col&gt;
	 *	Set type of action (default: 0)
	 *		 0 -- Average embeddings		 1 -- Add embeddings		 2 -- Concatenate embeddings
	 * </pre>
	 * 
	 * <pre>
	 * -K &lt;col&gt;
	 *	 Number of words to concatenate.
	 *	(default: 15)
	 * </pre>
	 * 
	 * <pre>
	 * -L
	 *	 Lowercase content.
	 *	(default: false)
	 * </pre>
	 * 
	 * <pre>
	 * -O
	 *	 Normalize tokens (replace goood by good, standarise URLs and @users).
	 *	(default: false)
	 * </pre>
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

		String embeddingFileNameOption = Utils.getOption('B', options);
		if (embeddingFileNameOption.length() > 0) 
			this.setEmbeddingFileName(embeddingFileNameOption);




		String tmpStr = Utils.getOption('S', options);
		if (tmpStr.length() != 0)
			setAction(new SelectedTag(Integer.parseInt(tmpStr), TAGS_ACTION));
		else
			setAction(new SelectedTag(AVERAGE_ACTION, TAGS_ACTION));



		String kOption = Utils.getOption('K', options);
		if (kOption.length() > 0) {
			String[] kOptionSpec = Utils.splitOptions(kOption);
			if (kOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid value");
			}
			int kValue = Integer.parseInt(kOptionSpec[0]);
			this.setK(kValue);

		}


		this.toLowerCase=Utils.getFlag('L', options);

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
		if(!this.isFirstBatchDone()){
			this.embeddingDict=new EmbeddingHandler(this.embeddingFileName);
			this.embeddingDict.createDict();						
		}


		if(this.m_action==AVERAGE_ACTION || this.m_action==ADD_ACTION){
			for(int j=0;j<this.embeddingDict.getDimensions();j++){
				att.add(new Attribute("Embedding-"+j));
			}			
		}
		else if(this.m_action==CONCATENATE_ACTION){
			for(int i=0;i<this.k;i++){
				for(int j=0;j<this.embeddingDict.getDimensions();j++){
					att.add(new Attribute("Embedding-"+i+","+j));
				}			
			}


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

		Instances result = getOutputFormat();

		// reference to the content of the message, users index start from zero
		Attribute attrCont = instances.attribute(this.textIndex-1);


		for (int i = 0; i < instances.numInstances(); i++) {
			double[] values = new double[result.numAttributes()];
			for (int n = 0; n < instances.numAttributes(); n++)
				values[n] = instances.instance(i).value(n);

			String content = instances.instance(i).stringValue(attrCont);
			List<String> words = affective.core.Utils.tokenize(content, this.toLowerCase, this.cleanTokens);

			int m=0;
			for(String word:words){
				if(this.embeddingDict.getWordMap().containsKey(word)){
					AbstractDoubleList embforWordVals=this.embeddingDict.getWordMap().get(word);
					int j=0;
					for(double embDimVal:embforWordVals){						
						if(m_action==AVERAGE_ACTION){
							values[result.attribute("Embedding-"+j).index()] += embDimVal/words.size();	
						}
						else if(m_action==ADD_ACTION){
							values[result.attribute("Embedding-"+j).index()] += embDimVal;
						}
						else if(m_action==CONCATENATE_ACTION){
							if(m<this.k){
								values[result.attribute("Embedding-"+m+","+j).index()] += embDimVal;
							}
						}

						j++;
					}					
				}
				m++;

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
	 * Get the file name containing the word vectors.
	 * 
	 * @return the embeddings file name
	 */	
	public String getEmbeddingFileName() {
		return embeddingFileName;
	}

	/**
	 * Set the embeddings's file name.
	 * 
	 * @param embeddingFileName the embeddings file name
	 */
	public void setEmbeddingFileName(String embeddingFileName) {
		this.embeddingFileName = embeddingFileName;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String embeddingFileNameTipText() {

		return "The file name containing the word vectors. It has to be a gzip compressed csv file with the following structure:\n"
				+ "value1<tab>value2<tab>..word" ;
	}	




	/**
	 * Gets the action for aggregating the embeddings.
	 * 
	 * @return the current action.
	 */
	public SelectedTag getAction() {
		return new SelectedTag(m_action, TAGS_ACTION);
	}


	/**
	 * Sets the aggregation action.
	 * 
	 * @param value the action type
	 * 
	 */
	public void setAction(SelectedTag value) {
		if (value.getTags() == TAGS_ACTION) {
			this.m_action=value.getSelectedTag().getID();
		}
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String actionTipText() {

		return "The action for aggregating the word embeddings: 1) Average embeddings, "
				+ "2) Add embeddings, 3) Concatenate embeddings";
	}	


	/**
	 * Gets the number of embeddings to concatenate.
	 * 
	 * @return the number of embeddings.
	 */
	public int getK() {
		return k;
	}	

	/**
	 * Sets the number of embeddings to concatenate.
	 * 
	 * @param k the number of embeddings.
	 * 
	 */
	public void setK(int k) {
		this.k = k;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String kTipText() {
		return "Number of words (from left to right) of the tweet whose embeddings will be concatenated.";
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
				+ "and replacing 	user mentions and URLs with generic tokens..";		
	}


}
