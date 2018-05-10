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
 *    DistantSupervisionSyntheticFilter.java
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */


package weka.filters.unsupervised.attribute;

import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.io.File;
import java.util.List;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionMetadata;
import weka.core.SparseInstance;
import weka.core.WekaPackageManager;
import affective.core.ArffLexiconEvaluator;


/**
 *  <!-- globalinfo-start --> 
 *  
 *  An abstract filter that creates polarity labeled instances from unlabeled tweets and a seed polarity lexicon by
 *  generating synthetic instances.
 * <!-- globalinfo-end -->
 * 
 *  
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public abstract class DistantSupervisionSyntheticFilter extends TweetToFeatureVector {


	/** For serialization.    **/
	private static final long serialVersionUID = 1616693021695150782L;


	/** Default path to where resources are stored. */
	public static String RESOURCES_FOLDER_NAME = weka.core.WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "resources";


	/** Default path to where lexicons are stored. */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons"+ File.separator + "arff_lexicons";


	/** The prefix for unigram attributes. */
	static public String UNIPREFIX="WORD-";

	/** The prefix for cluster-based attributes. */
	static public String CLUSTPREFIX="CLUST-";



	/** A map counting the number of documents in which candidate attributes appear. */
	protected Object2IntMap<String> attributeCount;


	/** Contains a mapping of valid attribute with their indexes. */
	protected Object2IntMap<String> m_Dictionary;

	/** Brown Clusters Dictionary. */
	protected Object2ObjectMap<String,String> brownDict;


	/** True if the value of each feature is set to its frequency in the tweet. Boolean weights are used otherwise. */
	protected boolean freqWeights=true;

	/** The minimum number of documents for an attribute to be considered. */
	protected int minAttDocs=0; 


	/** The random number seed.  */
	protected int m_randomSeed = 1;

	
	/** True for calculating word-based attributes. */
	protected boolean createWordAtts=true;


	/** True for calculating cluster-based attributes. */
	protected boolean createClustAtts=true;



	/** The path of the seed lexicon. */
	protected File lexicon=new File(LEXICON_FOLDER_NAME+File.separator+"BingLiu.arff");

	/** The path of the word clusters. */
	protected File wordClustFile=new File(RESOURCES_FOLDER_NAME+File.separator+"50mpaths2.txt.gz");

	
	/** The target lexicon attribute. */
	protected String polarityAttName="polarity";


	/** The positive attribute value name in the lexicon. */
	protected String polarityAttPosValName="positive";


	/** The negative attribute value name in the lexicon. */
	protected String polarityAttNegValName="negative";


	/** LexiconEvaluator for sentiment prefixes. */
	protected ArffLexiconEvaluator lex=new ArffLexiconEvaluator();





	/**
	 * Maps tweets from the second batch into instances that are compatible with the ones generated 
	 * @param inp input Instances
	 * @return convertes Instances
	 */
	public Instances mapTargetInstance(Instances inp){

		// Creates instances with the same format
		Instances result=getOutputFormat();


		Attribute contentAtt=inp.attribute(this.m_textIndex.getIndex());


		for(Instance inst:inp){
			String content=inst.stringValue(contentAtt);



			// tokenizes the content 
			List<String> tokens = affective.core.Utils.tokenize(content, this.toLowerCase, this.standarizeUrlsUsers, this.reduceRepeatedLetters, this.m_tokenizer,this.m_stemmer,this.m_stopwordsHandler);

			// Identifies the distinct terms
			AbstractObjectSet<String> terms=new  ObjectOpenHashSet<String>(); 
			terms.addAll(tokens);


			Object2IntMap<String> docVec=this.calculateDocVec(tokens);

			double[] values = new double[result.numAttributes()];


			values[result.classIndex()]= inst.classValue();

			for(String att:docVec.keySet()){

				if(this.m_Dictionary.containsKey(att)){
					int attIndex=this.m_Dictionary.getInt(att);
					// we normalise the value by the number of documents
					values[attIndex]=docVec.getInt(att);					
				}


			}


			Instance outInst=new SparseInstance(1, values);

			inst.setDataset(result);

			result.add(outInst);

		}

		return result;

	}


	/**
	 * Calculates tweet vectors from a list of tokens
	 * @param tokens a tokenized tweet
	 * @return a mapping between attribute names and values
	 */
	public Object2IntMap<String> calculateDocVec(List<String> tokens) {

		Object2IntMap<String> docVec = new Object2IntOpenHashMap<String>();
		// add the word-based vector
		if(this.createWordAtts)
			docVec.putAll(affective.core.Utils.calculateTermFreq(tokens,UNIPREFIX,this.freqWeights));

		if(this.createClustAtts){
			// calcultates the vector of clusters
			List<String> brownClust=affective.core.Utils.clustList(tokens,brownDict);
			docVec.putAll(affective.core.Utils.calculateTermFreq(brownClust,CLUSTPREFIX,this.freqWeights));			
		}	


		return docVec;

	}






	@OptionMetadata(displayName = "minAttDocs",
			description = "Minimum frequency of a sparse attribute to be considered in the attribute space.", 
			commandLineParamName = "M", 
			commandLineParamSynopsis = "-M <int>",
			displayOrder = 6)	
	public int getMinAttDocs() {
		return minAttDocs;
	}
	public void setMinAttDocs(int minAttDocs) {
		this.minAttDocs = minAttDocs;
	}



	@OptionMetadata(displayName = "createWordAtts",
			description = "True for creating unigram attributes.", 
			commandLineParamIsFlag = true, commandLineParamName = "W", 
			commandLineParamSynopsis = "-W",
			displayOrder = 7)	
	public boolean isCreateWordAtts() {
		return createWordAtts;
	}

	public void setCreateWordAtts(boolean createWordAtts) {
		this.createWordAtts = createWordAtts;
	}


	@OptionMetadata(displayName = "createClustAtts",
			description = "True for creating attributes using word clusters",
			commandLineParamIsFlag = true, commandLineParamName = "C", 
			commandLineParamSynopsis = "-C",
			displayOrder = 8)	
	public void setCreateClustAtts(boolean createClustAtts) {
		this.createClustAtts = createClustAtts;
	}
	public boolean isCreateClustAtts() {
		return createClustAtts;
	}



	@OptionMetadata(displayName = "wordClustFile",
			description = "The file containing the word clusters.", 
			commandLineParamName = "H", 
			commandLineParamSynopsis = "-H <string>",
			displayOrder = 9)	
	public File getWordClustFile() {
		return wordClustFile;
	}
	public void setWordClustFile(File wordClustFile) {
		this.wordClustFile = wordClustFile;
	}


	@OptionMetadata(displayName = "lexicon",
			description = "The file containing a lexicon in ARFF format with word polarities.", 
			commandLineParamName = "lex", 
			commandLineParamSynopsis = "-lex <string>",
			displayOrder = 10)	
	public File getLexicon() {
		return lexicon;
	}
	public void setLexicon(File lexicon) {
		this.lexicon = lexicon;
	}




	@OptionMetadata(displayName = "randomseed",
			description = "The random seed number. \t default: 1", 
			commandLineParamName = "R", 
			commandLineParamSynopsis = "-R <int>",
			displayOrder = 11)
	public int getRandomSeed() {
		return m_randomSeed;	}
	public void setRandomSeed(int randomSeed) {
		this.m_randomSeed = randomSeed;
	}




	@OptionMetadata(displayName = "polarityAttName",
			description = "The lexicon attribute name with the word polarities. \t default: polarity", 
			commandLineParamName = "polatt", 
			commandLineParamSynopsis = "-polatt <string>",
			displayOrder = 12)	
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
			displayOrder = 17)
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
			displayOrder = 18)
	public String getPolarityAttNegValName() {
		return polarityAttNegValName;
	}
	public void setPolarityAttNegValName(String polarityAttNegValName) {
		this.polarityAttNegValName = polarityAttNegValName;
	}


}
