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
import java.util.Map;
import java.util.Vector;

import affective.core.SentiStrengthEvaluator;
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
 * &#64;@Article{ThelwallBP12,
 *  Title                    = {Sentiment strength detection for the social web.},
 *  Author                   = {Thelwall, Mike and Buckley, Kevan and Paltoglou, Georgios},
 *  Journal                  = {Journal of the American Society for Information Science and Technology},
 *  Year                     = {2012},
 *  Number                   = {1},
 *  Pages                    = {163-173},
 *  Volume                   = {63}
 * }
 * </pre>
 * <p/>
 <!-- technical-bibtex-end -->
 *  
 * 
 * @author Felipe Bravo-Marquez (fjb11@students.waikato.ac.nz)
 * @version $Revision: 1 $
 */


public class TweetToSentiStrengthFeatureVector extends SimpleBatchFilter {

	/** For serialization  */
	private static final long serialVersionUID = 3748678887246129719L;

	/** Default path to where lexicons are stored */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons";

	/** The path of SentiStrength */
	public static String SENTISTRENGTH_FOLDER_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"SentiStrength"+java.io.File.separator;

	/** the index of the string attribute to be processed */
	protected int textIndex=1; 


	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;


	/** True if url, users, and repeated letters are cleaned */
	protected boolean cleanTokens=false;

	

	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */	
	@Override
	public String globalInfo() {
		return "A batch filter that calcuates positive and negative scores for a tweet using SentiSrength.\n"
				+ "More info at: http://sentistrength.wlv.ac.uk .\n"
				+ "Disclaimer: SentiStrength can only be used for academic purposes from whitin this package.\n"+getTechnicalInformation().toString();
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
		result.setValue(TechnicalInformation.Field.AUTHOR, "Thelwall, Mike and Buckley, Kevan and Paltoglou, Georgios");
		result.setValue(TechnicalInformation.Field.TITLE, "Sentiment strength detection for the social web");
		result.setValue(TechnicalInformation.Field.YEAR, "2012");
		result.setValue(TechnicalInformation.Field.JOURNAL, "Journal of the American Society for Information Science and Technology");

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

		att.add(new Attribute("SentiStrength-posScore"));
		att.add(new Attribute("SentiStrength-negScore"));

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


		// SentiStrength is re-intialized in each batch as it is not serializable
		SentiStrengthEvaluator sentiStrengthEvaluator=new SentiStrengthEvaluator(
				SENTISTRENGTH_FOLDER_NAME,"SentiStrength");
		sentiStrengthEvaluator.processDict();

		for (int i = 0; i < instances.numInstances(); i++) {
			double[] values = new double[result.numAttributes()];
			for (int n = 0; n < instances.numAttributes(); n++)
				values[n] = instances.instance(i).value(n);

			String content = instances.instance(i).stringValue(attrCont);
			List<String> words = affective.core.Utils.tokenize(content, this.toLowerCase, this.cleanTokens);


			
			Map<String,Double> featuresForLex=sentiStrengthEvaluator.evaluateTweet(words);
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
