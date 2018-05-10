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
 *    TweetToSentiStrengthFeatureVector.java
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import affective.core.SentiStrengthEvaluator;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionMetadata;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.WekaPackageManager;
import weka.core.TechnicalInformation.Type;


/**
 *  <!-- globalinfo-start --> An attribute filter that calculates positive and negative scores using 
 *  SentiStrength for a tweet represented as a string attribute. 
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
 <!-- technical-bibtex-end -->
 *  
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 2 $
 */


public class TweetToSentiStrengthFeatureVector extends TweetToFeatureVector {

	/** For serialization.  */
	private static final long serialVersionUID = 3748678887246129719L;

	/** Default path to where lexicons are stored. */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons";

	/** The path of SentiStrength. */
	public static String SENTISTRENGTH_FOLDER_NAME=LEXICON_FOLDER_NAME+java.io.File.separator+"SentiStrength"+java.io.File.separator;


	/** The folder with the language files. */
	protected File langFolder=new File(SENTISTRENGTH_FOLDER_NAME+"english");





	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */	
	@Override
	public String globalInfo() {
		return "A filter that calcuates positive and negative scores for a tweet using SentiSrength.\n"
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

		att.add(new Attribute("SentiStrength-posScore"));
		att.add(new Attribute("SentiStrength-negScore"));

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


		// SentiStrength is re-intialized in each batch as it is not serializable
		SentiStrengthEvaluator sentiStrengthEvaluator=new SentiStrengthEvaluator(
				this.langFolder.getAbsolutePath()+File.separator,"SentiStrength");
		sentiStrengthEvaluator.processDict();

		for (int i = 0; i < instances.numInstances(); i++) {
			double[] values = new double[result.numAttributes()];
			for (int n = 0; n < instances.numAttributes(); n++)
				values[n] = instances.instance(i).value(n);

			String content = instances.instance(i).stringValue(attrCont);
			List<String> words = affective.core.Utils.tokenize(content, this.toLowerCase, this.standarizeUrlsUsers, this.reduceRepeatedLetters, this.m_tokenizer,this.m_stemmer,this.m_stopwordsHandler);

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



	@OptionMetadata(displayName = "language folder",
			description = "The folder containing SentiStrength Files. Change it for using a language different from English.",
			commandLineParamName = "L", commandLineParamSynopsis = "-L <string>",
			displayOrder = 6)
	/**
	 * Returns the folder with the SentiStrength files.
	 * @return the folder with the SentiStrength files.
	 */
	public File getLangFolder() {
		return langFolder;
	}

	/**
	 * Sets the folder with the SentiStrength files.
	 * @param langFolder the folder with the SentiStrength files.
	 */
	public void setLangFolder(File langFolder) {
		this.langFolder = langFolder;
	}



	/**
	 * Main method for testing this class.
	 *
	 * @param args should contain arguments to the filter: use -h for help
	 */		
	public static void main(String[] args) {
		runFilter(new TweetToSentiStrengthFeatureVector(), args);
	}




}
