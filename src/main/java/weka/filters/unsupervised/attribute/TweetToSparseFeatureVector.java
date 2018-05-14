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
 *    TweetToSparseFeatureVector.java
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.GZIPInputStream;

import affective.core.NegationEvaluator;
import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.impl.ModelSentence;
import cmu.arktweetnlp.impl.Sentence;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionMetadata;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Type;

/**
 *  <!-- globalinfo-start --> An attribute filter that calculates different types of sparse features 
 *  for a tweet represented as a string attribute. The type of features include: word n-grams, character n-grams,
 *  POS tags and Brown word clusters. The size of the attribute space would depend on the training dataset.
 *   
 * <!-- globalinfo-end -->
 * 
 * <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;Article{NRCJAIR14,
 * Title                    = {Sentiment analysis of short informal texts},
 * Author                   = {Kiritchenko, Svetlana and Zhu, Xiaodan and Mohammad, Saif M},
 * Journal                  = {Journal of Artificial Intelligence Research},
 * Year                     = {2014},
 * Pages                    = {723--762},
 * Volume                   = {50}
 *}
 * </pre>
 <!-- technical-bibtex-end -->
 * 
 * 
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 2 $
 */


public class TweetToSparseFeatureVector extends TweetToFeatureVector {


	/** for serialization. */
	private static final long serialVersionUID = 3635946466523698211L;


	/** Default path to where resources are stored. */
	public static String RESOURCES_FOLDER_NAME = weka.core.WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "resources";

	/** Default path of POS tagger model. */
	protected File taggerFile=new File (RESOURCES_FOLDER_NAME+File.separatorChar+"model.20120919");


	/** The path of the word clusters. */
	protected File wordClustFile=new File(RESOURCES_FOLDER_NAME+File.separatorChar+"50mpaths2.txt.gz");


	/** Counts the number of tweets in which candidate attributes appear. This will help for discarding infrequent attributes. */
	protected Object2IntMap<String> attributeCount; 

	/** List of tweets to process represented as feature vectors. */
	protected ObjectList<Object2IntMap<String>> procTweets; 

	/** Brown Clusters Dictionary. */
	protected Object2ObjectMap<String,String> brownDict;


	/** True if the value of each feature is set to its frequency in the tweet. Boolean weights are used otherwise. */
	protected boolean freqWeights=true;

	/** The minimum number of tweets for an attribute to be considered in the attribute space. */
	protected int minAttDocs=0; 


	/** The maximum dimension for the ngrams to calculate. If n=3 Unigrams, bigrams and trigrams will be calculated. */
	protected int wordNgramMaxDim=1;

	/** Prefix for word ngram featues. */
	protected String wordNgramPrefix="WNGRAM-";

	/** True for adding a prefix to words occurring in a negated context. */
	protected boolean negateTokens=false;


	/** True to calculate character ngram features. */
	protected boolean calculateCharNgram=false;

	/** Prefix for character ngram features. */ 
	protected String charNgramPrefix="CNGRAM-";

	/** The minimum dimension for character ngrams.  */
	protected int charNgramMinDim=3;

	/** The maximum dimension for character ngrams.  */
	protected int charNgramMaxDim=5;


	/** The maximum dimension for POS ngrams.  */
	protected int posNgramMaxDim=0;


	/** The prefix of the POS-based attributes. */
	protected String posPrefix="POS-";


	/** The maximum dimension for cluster ngrams.  */
	protected int clustNgramMaxDim=0;


	/** The prefix of the cluster-based attributes. */
	protected String clustPrefix="CLUST-";

	/** TwitterNLP Tagger model. */
	protected transient Tagger tagger;

	/** The NegationEvaluator object with the negating list. */
	protected NegationEvaluator negEval;


	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	@Override
	public String globalInfo() {
		return "An attribute filter that calculates different types of sparse features for a tweet"
				+ " represented as a string attribute. The type of features include: word n-grams, "
				+ "character n-grams, POS tags and Brown word clusters. The tokenization and POS tagging"
				+ " is done with the CMU Twitter NLP tool. The size of the attribute space"
				+ " will depend on the training dataset.\n"+getTechnicalInformation().toString();
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
		result.setValue(TechnicalInformation.Field.AUTHOR, "Kiritchenko, Svetlana and Zhu, Xiaodan and Mohammad, Saif M");
		result.setValue(TechnicalInformation.Field.TITLE, " Sentiment analysis of short informal texts");
		result.setValue(TechnicalInformation.Field.YEAR, "2014");
		result.setValue(TechnicalInformation.Field.BOOKTITLE, "Journal of Artificial Intelligence Research");
		return result;
	}




	/**
	 * Initializes the POS tagger
	 */	
	public void initializeTagger(){
		try {
			this.tagger= new Tagger();
			this.tagger.loadModel(taggerFile.getAbsolutePath());
		} catch (IOException e) {
			this.posNgramMaxDim=0;
		}
	}

	/**
	 * Initializes the NegationEvaluator object
	 */	
	public void initiliazeNegationEvaluator(){
		this.negEval=new NegationEvaluator(TweetToLexiconFeatureVector.NEGATION_LIST_FILE_NAME,"Negation");
		try {
			this.negEval.processDict();
		} catch (IOException e) {
			e.printStackTrace();
			this.negateTokens=false;
		}
	}


	/**
	 * Returns POS tags from a List of tokens using the CMU TweetNLP tool
	 * 
	 * @param tokens the input tokens 
	 * @return the list of POS tags
	 */	
	public List<String> getPOStags(List<String> tokens) {

		ArrayList<String> tags = new ArrayList<String>();

		try{
			Sentence sentence = new Sentence();
			sentence.tokens = tokens;
			ModelSentence ms = new ModelSentence(sentence.T());
			this.tagger.featureExtractor.computeFeatures(sentence, ms);
			this.tagger.model.greedyDecode(ms, false);



			for (int t = 0; t < sentence.T(); t++) {
				String tag = this.tagger.model.labelVocab.name(ms.labels[t]);
				tags.add(tag);
			}


		}
		catch(Exception e){
			System.err.println("Tagging Problem");
			for(int i=0;i<tokens.size();i++){
				tags.add("?");
				System.err.print(tokens.get(i));
			}

			e.printStackTrace(System.err);
		}

		return tags;
	}



	/**
	 * Calculates a vector of attributes from a String
	 * 
	 * @param content the input 
	 * @return an Object2IntMap object mapping the attributes to their values
	 */		
	public Object2IntMap<String> calculateDocVec(String content) {

		// tokenizes the content 
		List<String> tokens = affective.core.Utils.tokenize(content, this.toLowerCase, this.standarizeUrlsUsers, this.reduceRepeatedLetters, this.m_tokenizer,this.m_stemmer,this.m_stopwordsHandler);
		Object2IntMap<String> docVec = new Object2IntOpenHashMap<String>();

		if(this.calculateCharNgram){
			for(int i=this.getCharNgramMinDim();i<=this.getCharNgramMaxDim();i++){
				docVec.putAll(affective.core.Utils.calculateTermFreq(affective.core.Utils.extractCharNgram(content,i),"CHAR-"+i+"-",this.freqWeights));	
			}
		}

		if(this.clustNgramMaxDim>0){
			// calcultates the vector of clusters
			List<String> brownClust=affective.core.Utils.clustList(tokens,brownDict);
			docVec.putAll(affective.core.Utils.calculateTermFreq(brownClust,this.clustPrefix+"1-",this.freqWeights));		
			// add ngrams where n > 1
			if(this.clustNgramMaxDim>1){
				for(int i=2;i<=this.clustNgramMaxDim;i++){
					docVec.putAll(affective.core.Utils.calculateTermFreq(affective.core.Utils.calculateTokenNgram(brownClust,i),this.clustPrefix+i+"-",this.freqWeights));					
				}

			}
		}	

		if(this.posNgramMaxDim>0){
			List<String> posTags=this.getPOStags(tokens);
			docVec.putAll(affective.core.Utils.calculateTermFreq(posTags,this.posPrefix+"1-",this.freqWeights));
			// add ngrams where n > 1
			if(this.posNgramMaxDim>1){
				for(int i=2;i<=this.posNgramMaxDim;i++){
					docVec.putAll(affective.core.Utils.calculateTermFreq(affective.core.Utils.calculateTokenNgram(posTags,i),this.posPrefix+i+"-",this.freqWeights));					
				}
			}
		}

		// use negated tokens for word ngrams features if option is set
		if(this.negateTokens)
			tokens=affective.core.Utils.negateTokens(tokens, this.negEval.getWordList());

		// add the ngram vectors
		if(this.wordNgramMaxDim>0){
			// add the unigrams
			docVec.putAll(affective.core.Utils.calculateTermFreq(tokens,this.wordNgramPrefix+"1-",this.freqWeights));
			// add ngrams where n > 1
			if(this.wordNgramMaxDim>1){
				for(int i=2;i<=this.wordNgramMaxDim;i++){
					docVec.putAll(affective.core.Utils.calculateTermFreq(affective.core.Utils.calculateTokenNgram(tokens,i),this.wordNgramPrefix+i+"-",this.freqWeights));					
				}				
			}			
		}

		return docVec;
	}


	/**
	 * Processes a batch of tweets.
	 * 
	 * @param tweetInstances the input tweets 
	 */		
	public void tweetsToVectors(Instances tweetInstances) {


		// The vocabulary is created only in the first execution
		if (!this.isFirstBatchDone()){
			this.attributeCount = new Object2IntOpenHashMap<String>();


			// the Dictionary of the brown Clusters
			if(this.clustNgramMaxDim>0){
				this.brownDict=new Object2ObjectOpenHashMap<String,String>();
				try {
					FileInputStream fin = new FileInputStream(wordClustFile);
					GZIPInputStream gzis = new GZIPInputStream(fin);
					InputStreamReader xover = new InputStreamReader(gzis);
					BufferedReader bf = new BufferedReader(xover);

					String line;
					while ((line = bf.readLine()) != null) {
						String pair[] = line.split("\t");
						// the word in the clusters are stemmed
						brownDict.put(this.m_stemmer.stem(pair[1]), pair[0]);


					}
					bf.close();
					xover.close();
					gzis.close();
					fin.close();

				} catch (IOException e) {
					// do not create clusters attributes
					this.clustNgramMaxDim=0;
				}

			}

			// Initializes NegationEvaluator
			if(this.negateTokens)
				this.initiliazeNegationEvaluator();

		}


		// Loads the POS tagger model 
		if(this.posNgramMaxDim>0 && this.tagger==null){				
			this.initializeTagger();
		}

		this.procTweets = new ObjectArrayList<Object2IntMap<String>>();

		// reference to the content of the message, users index start from zero
		Attribute attrCont = tweetInstances.attribute(this.m_textIndex.getIndex());

		for (ListIterator<Instance> it = tweetInstances.listIterator(); it
				.hasNext();) {
			Instance inst = it.next();
			String content = inst.stringValue(attrCont);
			if(this.toLowerCase)
				content=content.toLowerCase();



			Object2IntMap<String> docVec=calculateDocVec(content);

			// Add the frequencies of the different words
			this.procTweets.add(docVec);

			// The attribute space is calculated only the first time we run the filter.
			// This avoids adding new features for the test data
			if (!this.isFirstBatchDone()) {

				// if the attribute is new we add it to the attribute list, otherwise we
				// increment the count
				for(String docAtt:docVec.keySet()){
					if(this.attributeCount.containsKey(docAtt)){
						int prevFreq=this.attributeCount.getInt(docAtt);
						this.attributeCount.put(docAtt,prevFreq+1);						
					}
					else{
						this.attributeCount.put(docAtt,1);
					}

				}

			}

		}

	}


	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#determineOutputFormat(weka.core.Instances)
	 */
	@Override
	protected Instances determineOutputFormat(Instances inputFormat) {

		// set upper value for text index
		m_textIndex.setUpper(inputFormat.numAttributes() - 1);

		ArrayList<Attribute> att = new ArrayList<Attribute>();


		// Adds all attributes of the inputformat
		for (int i=0; i < inputFormat.numAttributes(); i++) {
			att.add(inputFormat.attribute(i));
		}

		// calculates the word frequency vectors and the vocabulary
		this.tweetsToVectors(inputFormat);


		for (String attributeName : this.attributeCount.keySet()) {
			if(this.attributeCount.get(attributeName)>=this.minAttDocs){
				Attribute a = new Attribute(attributeName);
				att.add(a); // adds an attribute for each word using a prefix
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

		// if we are in the testing data we calculate the word vectors again
		if (this.isFirstBatchDone()) {
			this.tweetsToVectors(instances);
		}


		int i = 0;
		for (Object2IntMap<String> vec : this.procTweets) {
			double[] values = new double[result.numAttributes()];

			// copy previous attributes values
			for (int n = 0; n < instances.numAttributes(); n++)
				values[n] = instances.instance(i).value(n);

			// add words using the frequency as attribute value
			for (String innerAtt : vec.keySet()) {
				// we only add the value of valid attributes
				if (result.attribute(innerAtt) != null){
					int attIndex=result.attribute(innerAtt).index();					
					values[attIndex]=(double)vec.getInt(innerAtt);

				}


			}


			Instance inst=new SparseInstance(1, values);


			inst.setDataset(result);
			// copy possible strings, relational values...
			copyValues(inst, false, instances, result);

			result.add(inst);
			i++;

		}

		return result;
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


	@OptionMetadata(displayName = "freqWeights",
			description = "True if the value of each feature is set to its frequency in the tweet. Boolean weights are used otherwise.\n",
			commandLineParamIsFlag = true, 
			commandLineParamName = "F", 
			commandLineParamSynopsis = "-F",
			displayOrder = 7)	
	public boolean isFreqWeights() {
		return freqWeights;
	}
	public void setFreqWeights(boolean freqWeights) {
		this.freqWeights = freqWeights;
	}




	@OptionMetadata(displayName = "wordNgramMaxDim",
			description = "Maximum size for the word n-gram features. \n"
					+ "\t Set this variable to zero for no word n-gram attributes."
					+ " All word n-grams from i=1 to this value will be extracted.", 
					commandLineParamName = "Q", 
					commandLineParamSynopsis = "-Q <int>",
					displayOrder = 8)	
	public int getWordNgramMaxDim() {
		return wordNgramMaxDim;
	}
	public void setWordNgramMaxDim(int wordNgramMaxDim) {
		this.wordNgramMaxDim = wordNgramMaxDim;
	}


	@OptionMetadata(displayName = "negateTokens",
			description = "Add a prefix to words occurring in negated contexts e.g., I don't like you => I don't NEG-like NEG-you.\n "
					+ "\t The prefixes only affect word n-gram features. The scope of negation finishes with the next punctuation mark.",
					commandLineParamIsFlag = true, 
					commandLineParamName = "R", 
					commandLineParamSynopsis = "-R",
					displayOrder = 9)		
	public boolean isNegateTokens() {
		return negateTokens;
	}
	public void setNegateTokens(boolean negateTokens) {
		this.negateTokens = negateTokens;
	}


	@OptionMetadata(displayName = "calculateCharNgram",
			description = "Calculate character n-gram features.",
			commandLineParamIsFlag = true, 
			commandLineParamName = "A", 
			commandLineParamSynopsis = "-A",
			displayOrder = 10)		
	public boolean isCalculateCharNgram() {
		return calculateCharNgram;
	}
	public void setCalculateCharNgram(boolean calculateCharNgram) {
		this.calculateCharNgram = calculateCharNgram;
	}

	@OptionMetadata(displayName = "charNgramMinDim",
			description = "The minimum dimension for character n-grams.", 
			commandLineParamName = "D", 
			commandLineParamSynopsis = "-D <int>",
			displayOrder = 11)		
	public int getCharNgramMinDim() {
		return charNgramMinDim;
	}
	public void setCharNgramMinDim(int charNgramMinDim) {
		this.charNgramMinDim = charNgramMinDim;
	}



	@OptionMetadata(displayName = "charNgramMaxDim",
			description = "The maximum dimension for character n-grams.", 
			commandLineParamName = "E", 
			commandLineParamSynopsis = "-E <int>",
			displayOrder = 12)	
	public int getCharNgramMaxDim() {
		return charNgramMaxDim;
	}
	public void setCharNgramMaxDim(int charNgramMaxDim) {
		this.charNgramMaxDim = charNgramMaxDim;
	}

	@OptionMetadata(displayName = "posNgramMaxDim",
			description = "The maximum size for POS n-grams."
					+ " Set this variable to zero for no POS attributes. \n"
					+ "\t The tweets are POS-tagged using the CMU TweetNLP tool.", 
					commandLineParamName = "G", 
					commandLineParamSynopsis = "-G <int>",
					displayOrder = 13)	
	public int getPosNgramMaxDim() {
		return posNgramMaxDim;
	}
	public void setPosNgramMaxDim(int posNgramMaxDim) {
		this.posNgramMaxDim = posNgramMaxDim;
	}


	@OptionMetadata(displayName = "clustNgramMaxDim",
			description = "The maximum dimension for n-grams calculated with Brown word clusters.\n"
					+ "\t Set this variable to zero for no word-clusters attributes. \n"
					+ "\t The word clusters are taken from the CMU Tweet NLP tool.", 
					commandLineParamName = "I", 
					commandLineParamSynopsis = "-I <int>",
					displayOrder = 14)	
	public int getClustNgramMaxDim() {
		return clustNgramMaxDim;
	}
	public void setClustNgramMaxDim(int clustNgramMaxDim) {
		this.clustNgramMaxDim = clustNgramMaxDim;
	}


	@OptionMetadata(displayName = "taggerFile",
			description = "The file with TweetNLP POS tagger model.",
			commandLineParamName = "taggerFile", commandLineParamSynopsis = "-taggerFile <string>",
			displayOrder = 15)
	public File getTaggerFile() {
		return taggerFile;
	}
	public void setTaggerFile(File taggerFile) {
		this.taggerFile = taggerFile;
	}

	@OptionMetadata(displayName = "wordClustFile",
			description = "The file with the word clusters in gzip format.",
			commandLineParamName = "wordClustFile", commandLineParamSynopsis = "-wordClustFile <string>",
			displayOrder = 16)
	public File getWordClustFile() {
		return wordClustFile;
	}
	public void setWordClustFile(File wordClustFile) {
		this.wordClustFile = wordClustFile;
	}




	/**
	 * Main method for testing this class.
	 *
	 * @param args should contain arguments to the filter: use -h for help
	 */		
	public static void main(String[] args) {
		runFilter(new TweetToSparseFeatureVector(), args);
	}

}
