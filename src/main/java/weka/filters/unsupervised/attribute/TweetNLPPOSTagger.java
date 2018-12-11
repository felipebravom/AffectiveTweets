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

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.impl.ModelSentence;
import cmu.arktweetnlp.impl.Sentence;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionMetadata;
import weka.core.SingleIndex;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Type;
import weka.core.tokenizers.Tokenizer;
import weka.core.tokenizers.TweetNLPTokenizer;
import weka.filters.SimpleBatchFilter;



/**
 *  <!-- globalinfo-start --> A Twitter-specific POS tagger based on the CMU TwitterNLP library: http://www.cs.cmu.edu/~ark/TweetNLP/
 * <!-- globalinfo-end -->
 * 
 * <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;InProceedings{twitterNLP,
 * 	Title                    = {Part-of-speech tagging for twitter: Annotation, features, and experiments},
 * 	Author                   = {Gimpel, Kevin and Schneider, Nathan and O'Connor, Brendan and Das, Dipanjan and Mills, Daniel and Eisenstein, Jacob and Heilman, Michael and Yogatama, Dani and Flanigan, Jeffrey and Smith, Noah A},
 * 	Booktitle                = {Proceedings of the 49th Annual Meeting of the Association for Computational Linguistics: Human Language Technologies: short papers-Volume 2},
 * 	Year                     = {2011},
 * 	Organization             = {Association for Computational Linguistics},
 * 	Pages                    = {42--47}
 *	}
 * </pre>
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class TweetNLPPOSTagger  extends SimpleBatchFilter {



	/** For serialization.    **/
	private static final long serialVersionUID = 1616693021695150782L;



	/** The tokenizer. */
	protected Tokenizer m_tokenizer=new TweetNLPTokenizer();

	/** The index of the string attribute to be processed. */
	protected SingleIndex m_textIndex = new SingleIndex("1");
	
	
	/** The separator String between the token and the POS tag. */
	protected String separator = "_";



	/** Default path to where resources are stored. */
	public static String RESOURCES_FOLDER_NAME = weka.core.WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "resources";

	/** Default path of POS tagger model. */
	protected File taggerFile=new File (RESOURCES_FOLDER_NAME+File.separatorChar+"model.20120919");


	/** TwitterNLP Tagger model. */
	protected transient Tagger tagger;






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
        result.setValue(TechnicalInformation.Field.AUTHOR, "Gimpel, Kevin and Schneider, Nathan and O'Connor, Brendan and Das, Dipanjan and Mills, Daniel and Eisenstein, Jacob and Heilman, Michael and Yogatama, Dani and Flanigan, Jeffrey and Smith, Noah A");
        result.setValue(TechnicalInformation.Field.TITLE, "Part-of-speech tagging for twitter: Annotation, features, and experiments");
        result.setValue(TechnicalInformation.Field.YEAR, "2011");
        result.setValue(TechnicalInformation.Field.URL, "http://www.cs.cmu.edu/~ark/TweetNLP/");
        result.setValue(TechnicalInformation.Field.NOTE, "The Weka tokenizer works with version 0.32 of TweetNLP.");

        return result;
}




	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#globalInfo()
	 */
	@Override
	public String globalInfo() {
		return "A Twitter-specific POS tagger based on the CMU TweetNLP library.\n" + getTechnicalInformation().toString();				
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





	/**
	 * Returns POS-tagger String from a given String using the CMU TweetNLP tool
	 * 
	 * @param content the String 
	 * @return the tagged String
	 */	
	public String tagTweet(String content) {


		List<String> tokens = new ArrayList<String>();

		this.m_tokenizer.tokenize(content);
		for(;this.m_tokenizer.hasMoreElements();)
			tokens.add(this.m_tokenizer.nextElement());

		String [] tokensArray = tokens.toArray(new String[0]);


		String tags = new String();

		try{
			Sentence sentence = new Sentence();
			sentence.tokens = tokens;
			ModelSentence ms = new ModelSentence(sentence.T());
			this.tagger.featureExtractor.computeFeatures(sentence, ms);
			this.tagger.model.greedyDecode(ms, false);



			for (int t = 0; t < sentence.T(); t++) {
				String tag = this.tagger.model.labelVocab.name(ms.labels[t]);
				tags += tag+this.separator+tokensArray[t]+" ";
			}


		}
		catch(Exception e){
			System.err.println("Tagging Problem");
			for(int i=0;i<tokens.size();i++){
				tags += "?"+this.separator+tokensArray[i]+" ";
				System.err.print(tokens.get(i));
			}

			e.printStackTrace(System.err);
		}

		return tags;
	}



	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#determineOutputFormat(weka.core.Instances)
	 */
	@Override
	protected Instances determineOutputFormat(Instances inputFormat) {


		if (!this.isFirstBatchDone()){

			this.tagger= new Tagger();
			try {
				this.tagger.loadModel(taggerFile.getAbsolutePath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}


		// set upper value for text index
		m_textIndex.setUpper(inputFormat.numAttributes() - 1);



		ArrayList<Attribute> att = new ArrayList<Attribute>();

		// Adds all attributes of the inputformat
		for (int i = 0; i < inputFormat.numAttributes(); i++) {
			att.add(inputFormat.attribute(i));
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



			String content = instances.instance(i).stringValue(attrCont);
			String tokenizedContent = this.tagTweet(content);




			double[] values = new double[result.numAttributes()];

			// copy other attributes
			for (int n = 0; n < instances.numAttributes(); n++){
				if(n!=this.m_textIndex.getIndex())
					values[n] = instances.instance(i).value(n);
			}

			// add the content
			values[this.m_textIndex.getIndex()]= attrCont.addStringValue(tokenizedContent);



			Instance inst = new SparseInstance(1, values);

			inst.setDataset(result);

			// copy possible strings, relational values...
			copyValues(inst, false, instances, result);

			result.add(inst);



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
	
	
	@OptionMetadata(displayName = "taggerFile",
			description = "The file with TweetNLP POS tagger model.",
			commandLineParamName = "taggerFile", commandLineParamSynopsis = "-taggerFile <string>",
			displayOrder = 2)
	public File getTaggerFile() {
		return taggerFile;
	}
	public void setTaggerFile(File taggerFile) {
		this.taggerFile = taggerFile;
	}


	@OptionMetadata(displayName = "separator",
			description = "The separator String between the token and the POS tag.",
			commandLineParamName = "separator",
			commandLineParamSynopsis = "-separator <string>", displayOrder = 3)		
	public String getSeparator() {
		return separator;
	}
	public void setSeparator(String separator) {
		this.separator = separator;
	}



	/**
	 * Main method for testing this class.
	 *
	 * @param args should contain arguments to the filter: use -h for help
	 */	
	public static void main(String[] args) {
		runFilter(new TweetNLPPOSTagger(), args);
	}

}
