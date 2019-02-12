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
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import affective.core.ArffLexiconEvaluator;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionMetadata;
import weka.core.SparseInstance;
import weka.core.WekaPackageManager;

/**
 *  <!-- globalinfo-start --> A filter that calculates attributes for a tweet using a list of affective lexicons in arff format.
 *   
 * <!-- globalinfo-end -->
 * 
 *  
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */


public class TweetToInputLexiconFeatureVector extends TweetToFeatureVector {


	/** For serialization.  */
	private static final long serialVersionUID = -530731678800460897L;


	/** Default path to where lexicons are stored. */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons"+ File.separator + "arff_lexicons";

	/** The path of the MPQA lexicon */
	public static String NRC_AFFECT_INTENSITY_FILE_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"NRC-AffectIntensity-Lexicon.arff";


	/** List of Lexicons to use. */
	protected ArffLexiconEvaluator[] lexiconEval=new ArffLexiconEvaluator[]{new ArffLexiconEvaluator()};



	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */	
	@Override
	public String globalInfo() {
		return "A filter that calcuates attributes for a tweet using a given list of affective lexicons in arff format."
				+ " The features are calculated by adding or counting the affective associations of the words matching the lexicon."
				+ " All numeric and nominal attributes from the given lexicon are considered. Numeric scores are added and nominal are counted. "
				+ "The NRC-Affect-Intensity is used by deault. \n";
	}



	/**
	 * Initializes the dictionaries of all the lexicons to use. 
	 */
	protected void initializeDicts() {
		try {
			for(ArffLexiconEvaluator lexEval:this.lexiconEval)
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

		ArrayList<Attribute> att = new ArrayList<Attribute>();

		// Adds all attributes of the inputformat
		for (int i = 0; i < inputFormat.numAttributes(); i++) {
			att.add(inputFormat.attribute(i));
		}


		// The dictionaries of the lexicons are initialized only in the first batch
		if(!this.isFirstBatchDone())
			this.initializeDicts();



		for(ArffLexiconEvaluator lexEval:this.lexiconEval){
			for(String attName:lexEval.getFeatureNames())
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

		
		// set upper value for text index
		m_textIndex.setUpper(instances.numAttributes() - 1);

		Instances result = getOutputFormat();


		// reference to the content of the message, users index start from zero
		Attribute attrCont = instances.attribute(this.m_textIndex.getIndex());


		for (int i = 0; i < instances.numInstances(); i++) {
			double[] values = new double[result.numAttributes()];
			for (int n = 0; n < instances.numAttributes(); n++)
				values[n] = instances.instance(i).value(n);

			String content = instances.instance(i).stringValue(attrCont);
			List<String> words = affective.core.Utils.tokenize(content, this.toLowerCase, this.standarizeUrlsUsers, this.reduceRepeatedLetters, this.m_tokenizer,this.m_stemmer,this.m_stopwordsHandler);




			for(ArffLexiconEvaluator lexEval:this.lexiconEval){
				Map<String,Double> featuresForLex=lexEval.evaluateTweet(words);
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




	@OptionMetadata(displayName = "ArffLexiconEvaluator",
			description = "The specification of a lexicon evaluator. This option can be used multiple times.",
			commandLineParamName = "lexicon_evaluator",
			commandLineParamSynopsis = "-lexicon_evaluator <string>", displayOrder = 6)		
	public ArffLexiconEvaluator[] getLexiconEval() {
		return lexiconEval;
	}
	public void setLexiconEval(ArffLexiconEvaluator[] lexiconEval) {
		this.lexiconEval = lexiconEval;
	}
	

	/**
	 * Main method for testing this class.
	 *
	 * @param args should contain arguments to the filter: use -h for help
	 */		
	public static void main(String[] args) {
		runFilter(new TweetToInputLexiconFeatureVector(), args);
	}		
	

}
