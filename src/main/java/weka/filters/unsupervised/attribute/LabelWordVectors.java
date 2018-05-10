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
 *    LabelWordVector.java
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import affective.core.ArffLexiconWordLabeller;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionMetadata;
import weka.core.SingleIndex;
import weka.core.SparseInstance;
import weka.core.WekaPackageManager;
import weka.core.Capabilities.Capability;
import weka.filters.Filter;
import weka.filters.SimpleBatchFilter;

/**
 *  <!-- globalinfo-start --> A filter for labeling word vectors using a list of affective lexicons in arff format.
 *   
 * <!-- globalinfo-end -->
 * 
 *  
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class LabelWordVectors extends SimpleBatchFilter {


	/** For serialization.  */
	private static final long serialVersionUID = 1122160781260403611L;


	/** Default path to where lexicons are stored. */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons"+ File.separator + "arff_lexicons";

	/** The path of the default lexicon. */
	public static String NRC_AFFECT_INTENSITY_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"metaLexEmo.arff";


	/** The index of the string attribute with the target word. */
	protected SingleIndex m_WordIndex = new SingleIndex("last");



	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;


	/** List of lexicons to use. */
	protected ArffLexiconWordLabeller[] lexiconLabs=new ArffLexiconWordLabeller[]{new ArffLexiconWordLabeller()};



	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */	
	@Override
	public String globalInfo() {
		return "A batch filter that labels word vectors using a list of lexicons in Arff format."
				+ " All numeric and nominal attributes from the lexicons are appended. Unknown words receive missing values. \n";
	}



	/* (non-Javadoc)
	 * @see weka.filters.Filter#listOptions()
	 */
	@Override
	public Enumeration<Option> listOptions() {
		//this.getClass().getSuperclass()
		return Option.listOptionsForClassHierarchy(this.getClass(), Filter.class).elements();
	}


	/* (non-Javadoc)
	 * @see weka.filters.Filter#getOptions()
	 */
	@Override
	public String[] getOptions() {	
		return Option.getOptionsForHierarchy(this, Filter.class);

		//return Option.getOptions(this, this.getClass());
	}




	/* (non-Javadoc)
	 * @see weka.filters.Filter#setOptions(java.lang.String[])
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		Option.setOptionsForHierarchy(options, this, Filter.class);
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
		try {
			for(ArffLexiconWordLabeller lexEval:this.lexiconLabs)
				lexEval.processDict();
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

		ArrayList<Attribute> atts = new ArrayList<Attribute>();

		// Adds all attributes of the inputformat
		for (int i = 0; i < inputFormat.numAttributes(); i++) {
			atts.add(inputFormat.attribute(i));
		}


		// The dictionaries of the lexicons are intialized only in the first batch
		if(!this.isFirstBatchDone())
			this.initializeDicts();



		for(ArffLexiconWordLabeller lexEval:this.lexiconLabs){
			for(Attribute att:lexEval.getAttributes()){
				if(att.isNumeric())
					atts.add(new Attribute(lexEval.getLexiconName()+"-"+att.name()));
				else if(att.isNominal()){
					List<String> attValues=new ArrayList<String>();

					for(int i=0;i<att.numValues();i++){
						attValues.add(att.value(i));
					}					

					atts.add(new Attribute(lexEval.getLexiconName()+"-"+att.name(),attValues));

				}


			}

		}




		Instances result = new Instances(inputFormat.relationName(), atts, 0);

		// set the class index
		result.setClassIndex(inputFormat.classIndex());



		return result;
	}



	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#process(weka.core.Instances)
	 */
	@Override
	protected Instances process(Instances instances) throws Exception {


		// set upper value for word index
		m_WordIndex.setUpper(instances.numAttributes() - 1);


		Instances result = getOutputFormat();


		// reference to the words attribute
		Attribute attrCont = instances.attribute(m_WordIndex.getIndex());




		for (int i = 0; i < instances.numInstances(); i++) {
			double[] values = new double[result.numAttributes()];
			for (int n = 0; n < instances.numAttributes(); n++)
				values[n] = instances.instance(i).value(n);

			String word = instances.instance(i).stringValue(attrCont);


			for(ArffLexiconWordLabeller lexEval:this.lexiconLabs){
				Map<Attribute,Double> featuresForLex=lexEval.evaluateWord(word);
				for(Attribute att:featuresForLex.keySet()){
					values[result.attribute(lexEval.getLexiconName()+"-"+att.name()).index()] = featuresForLex.get(att);		

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



	@OptionMetadata(displayName = "wordIndex",
			description = "The word index (starting from 1) of the target string attribute. Start and last are valid values."
					+ "\t(default last).",
					commandLineParamName = "I", commandLineParamSynopsis = "-I <col>",
					displayOrder = 0)	
	public String getWordIndex() {
		return m_WordIndex.getSingleIndex();
	}
	public void setWordIndex(String wordIndex) {		
		this.m_WordIndex.setSingleIndex(wordIndex);
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



	@OptionMetadata(displayName = "ArffLexiconWordLabeller",
			description = "The specification of a lexicon evaluator. This option can be used multiple times.",
			commandLineParamName = "lexicon_evaluator",
			commandLineParamSynopsis = "-lexicon_labeller <string>", displayOrder = 3)		
	public ArffLexiconWordLabeller[] getLexiconLabs() {
		return lexiconLabs;
	}
	public void setLexiconLabs(ArffLexiconWordLabeller[] lexiconLabs) {
		this.lexiconLabs = lexiconLabs;
	}


	/**
	 * Main method for testing this class.
	 *
	 * @param args should contain arguments to the filter: use -h for help
	 */	
	public static void main(String[] args) {
		runFilter(new LabelWordVectors(), args);
	}	




}
