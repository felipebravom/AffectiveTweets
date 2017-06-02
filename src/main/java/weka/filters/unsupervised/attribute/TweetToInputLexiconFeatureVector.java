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
 *    TweetToInputLexiconFeatureVector.java
 *    Copyright (C) 1999-2017 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import affective.core.ArffLexiconEvaluator;
import affective.core.LexiconEvaluator;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionMetadata;
import weka.core.SparseInstance;
import weka.core.WekaPackageManager;
import weka.core.Capabilities.Capability;
import weka.filters.SimpleBatchFilter;

/**
 *  <!-- globalinfo-start --> A batch filter that calculates attributes for a tweet using a given affective lexicon in arff format.
 *   
 * <!-- globalinfo-end -->
 * 
 *  
 * 
 * @author Felipe Bravo-Marquez (fjb11@students.waikato.ac.nz)
 * @version $Revision: 1 $
 */


public class TweetToInputLexiconFeatureVector extends SimpleBatchFilter {

	
	/** For serialization  */
	private static final long serialVersionUID = -530731678800460897L;


	/** Default path to where lexicons are stored */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons"+ File.separator + "arff_lexicons";

	/** The path of the MPQA lexicon */
	public static String NRC_AFFECT_INTENSITY_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"NRC-AffectIntensity-Lexicon.arff";
	
		
	

	/** the index of the string attribute to be processed */
	protected int textIndex=1; 
	

	/** the index of the word attribute in the given arff lexicon */
	protected int lexiconWordIndex=1;
	
    /** The input lexicon in arff format  */
	protected File m_lexiconFile = new File(NRC_AFFECT_INTENSITY_FILE_NAME);
    
	/** The lexicon name to be prefixed in all features */
	protected String lexiconName="NRC-Affect-Intensity";

	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;


	/** True if url, users, and repeated letters are cleaned */
	protected boolean cleanTokens=false;

	/** List of Lexicons to use */
	private LexiconEvaluator lexiconEval;


	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */	
	@Override
	public String globalInfo() {
		return "A batch filter that calcuates attributes for a tweet using a given affective lexicon in arff format."
				+ " The features are calculated by adding or counting the affective associations of the words matching the lexicon."
				+ " All numeric and nominal attributes from the given lexicon are considered. Numeric scores are added and nominal are counted. "
				+ "The NRC-Affect-Intensity is used by deault. \n";
	}



	/* (non-Javadoc)
	 * @see weka.filters.Filter#listOptions()
	 */
	@Override
	public Enumeration<Option> listOptions() {
		return Option.listOptionsForClass(this.getClass()).elements();
	}


	/* (non-Javadoc)
	 * @see weka.filters.Filter#getOptions()
	 */
	@Override
	public String[] getOptions() {		
		return Option.getOptions(this, this.getClass());
	}


	/**
	 * Parses the options for this object.
	 * 
	 * <!-- options-start --> 
	 * <pre> 
	 *-I &lt;col&gt;
	 *  Index of string attribute (default: 1)
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
		Option.setOptions(options, this, this.getClass());
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




	/**
	 * Initializes the dictionaries of all the lexicons to use. 
	 */
	protected void initializeDicts() {
		this.lexiconEval=new ArffLexiconEvaluator(this.m_lexiconFile.getAbsolutePath(),this.lexiconName,this.lexiconWordIndex);
		try {
			this.lexiconEval.processDict();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}


	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#determineOutputFormat(weka.core.Instances)
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
		


			for(String attName:this.lexiconEval.getFeatureNames())
				att.add(new Attribute(attName));			
		

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

			
				Map<String,Double> featuresForLex=this.lexiconEval.evaluateTweet(words);
				for(String featName:featuresForLex.keySet()){
					values[result.attribute(featName).index()] = featuresForLex.get(featName);
				}
			


			Instance inst = new SparseInstance(1, values);

			inst.setDataset(result);

			// copy possible strings, relational values...
			copyValues(inst, false, instances, result);

			result.add(inst);

		}

		return result;
	}

    @OptionMetadata(displayName = "textIndex",
            description = "The index (starting from 1) of the target string attribute.",
            commandLineParamName = "I", commandLineParamSynopsis = "-I <int>",
            displayOrder = 0)
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



	
    @OptionMetadata(displayName = "lowercase",
            description = "Lowercase the tweet's content.", commandLineParamIsFlag = true,
            commandLineParamName = "U", commandLineParamSynopsis = "-U",
            displayOrder = 1)
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



    @OptionMetadata(displayName = "cleanTokens",
            description = "Reduce the attribute space by replacing sequences of letters occurring more than two "
				+ "times in a row with two occurrences of them (e.g., huuungry is reduced to huungry, loooove to loove), "
				+ "and replacing user mentions and URLs with generic tokens.", 
				commandLineParamIsFlag = true, commandLineParamName = "O", 
				commandLineParamSynopsis = "-O",
				displayOrder = 2)	
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


    @OptionMetadata(
            displayName = "lexicon file",
            description = "The arff file with the input lexicon.",
            commandLineParamName = "lexiconFile", commandLineParamSynopsis = "-lexiconFile <string>",
            displayOrder = 1)
    public File getLexiconFile() { return m_lexiconFile; }
    public void setLexiconFile(File lexiconFile) { m_lexiconFile = lexiconFile; }
	

    
    @OptionMetadata(displayName = "lexiconName",
            description = "The lexicon name to be prefixed in all features calculated from this lexicon.",
            commandLineParamName = "B", commandLineParamSynopsis = "-B",
            displayOrder = 1)   
	public String getLexiconName() {
		return lexiconName;
	}
	public void setLexiconName(String lexiconName) {
		this.lexiconName = lexiconName;
	}
	
	

    @OptionMetadata(displayName = "lexiconWordIndex",
            description = "The index of the word attribute in the given arff lexicon (starting from 1).", 
            commandLineParamName = "A", commandLineParamSynopsis = "-A",
            displayOrder = 1)	
	public int getLexiconWordIndex() {
		return lexiconWordIndex;
	}
	public void setLexiconWordIndex(int lexiconWordIndex) {
		this.lexiconWordIndex = lexiconWordIndex;
	}


}
