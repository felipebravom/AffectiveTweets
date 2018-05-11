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
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */


package weka.filters.unsupervised.attribute;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import affective.core.CSVEmbeddingHandler;
import affective.core.EmbeddingHandler;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionMetadata;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.WekaPackageManager;
import weka.core.TechnicalInformation.Type;


/**
 *  <!-- globalinfo-start --> An attribute filter that calculates word embedding (word vectors) features 
 *  for a tweet represented as a string attribute. 
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
 <!-- technical-bibtex-end -->
 * 
 * 
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 2 $
 */


public class TweetToEmbeddingsFeatureVector extends TweetToFeatureVector {

	/** For serialization.    **/
	private static final long serialVersionUID = -823728822240437493L;

	/** Default path to where resources are stored. */
	public static String RESOURCES_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "resources";


	/**  Possible actions to perform on the embeddings.  */
	protected enum Action {
		AVERAGE_ACTION,
		ADD_ACTION,
		CONCATENATE_ACTION,
	}

	/** The action in which embeddings are operated on the tweet. */
	protected Action m_action = Action.AVERAGE_ACTION;


	/** Embedding Handler.    **/
	protected EmbeddingHandler embeddingHandler=new CSVEmbeddingHandler();


	/** The number of word embeddings to concatenate. */
	protected int k=15;


	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String globalInfo() {
		return "An attribute filter that calculates features for a string attribute  from "
				+ "given list of word vectors (embeddings). The embeddings format is a csv.gz file.\n "
				+ "Pretrained word embeddings are provided in: "
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
		if(!this.isFirstBatchDone()){
			this.embeddingHandler.createDict();						
		}


		if(this.m_action.equals(Action.AVERAGE_ACTION) || this.m_action.equals(Action.ADD_ACTION)){
			for(int j=0;j<this.embeddingHandler.getDimensions();j++){
				att.add(new Attribute("Embedding-"+j));
			}			
		}
		else if(this.m_action.equals(Action.CONCATENATE_ACTION)){
			for(int i=0;i<this.k;i++){
				for(int j=0;j<this.embeddingHandler.getDimensions();j++){
					att.add(new Attribute("Embedding-"+i+","+j));
				}			
			}


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


			int m=0;
			for(String word:words){
				if(this.embeddingHandler.getWordMap().containsKey(word)){
					AbstractDoubleList embforWordVals=this.embeddingHandler.getWordMap().get(word);
					int j=0;
					for(double embDimVal:embforWordVals){						
						if(m_action.equals(Action.AVERAGE_ACTION)){
							values[result.attribute("Embedding-"+j).index()] += embDimVal/words.size();	
						}
						else if(m_action.equals(Action.ADD_ACTION)){
							values[result.attribute("Embedding-"+j).index()] += embDimVal;
						}
						else if(m_action.equals(Action.CONCATENATE_ACTION)){
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





	@OptionMetadata(
			displayName = "action",
			description = "The action for aggregating the word embeddings: "
					+ "1) Average embeddings of the input string (AVERAGE_ACTION),"
					+ "2) Add embeddings of the input string (ADD_ACTION), "
					+ "3) Concatenate the first *k* embeddings of  the input string (CONCATENATE_ACTION), (default AVERAGE_ACTION).",
					commandLineParamName = "S", commandLineParamSynopsis = "-S <speficiation>",
					displayOrder = 6)	
	public Action getAction() {
		return m_action;
	}
	public void setAction(Action action) {
		this.m_action = action;
	}


	@OptionMetadata(displayName = "k",
			description = "Number of words (from left to right) of the tweet whose embeddings will be concatenated.", 
			commandLineParamName = "K", 
			commandLineParamSynopsis = "-K <int>",
			displayOrder = 7)		
	public int getK() {
		return k;
	}	
	public void setK(int k) {
		this.k = k;
	}


	@OptionMetadata(displayName = "embeddingHandler",
			description = "The embeddingHandler object to use.",
			commandLineParamName = "embeddingHandler",
			commandLineParamSynopsis = "-embeddingHandler <string>", displayOrder = 6)		
	public EmbeddingHandler getEmbeddingHandler() {
		return embeddingHandler;
	}
	public void setEmbeddingHandler(EmbeddingHandler embeddingHandler) {
		this.embeddingHandler = embeddingHandler;
	}


	/**
	 * Main method for testing this class.
	 *
	 * @param args should contain arguments to the filter: use -h for help
	 */		
	public static void main(String[] args) {
		runFilter(new TweetToEmbeddingsFeatureVector(), args);
	}	

}
