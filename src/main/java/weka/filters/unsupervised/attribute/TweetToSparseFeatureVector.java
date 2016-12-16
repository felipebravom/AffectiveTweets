package weka.filters.unsupervised.attribute;

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
 *    Copyright (C) 1999-2016 University of Waikato, Hamilton, New Zealand
 *
 */

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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import affective.core.NegationEvaluator;
import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.impl.ModelSentence;
import cmu.arktweetnlp.impl.Sentence;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Type;
import weka.filters.SimpleBatchFilter;

/**
 *  <!-- globalinfo-start --> An attribute filter that calculates different types of sparse features 
 *  for a tweet represented as a string attribute. The type of features include: word n-grams, character n-grams,
 *  POS tags and Brown word clusters. The size of the attribute space would depend on the training dataset.
 *   
 * <p/>
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
 * <p/>
 <!-- technical-bibtex-end -->
 * 
 * 
 * 
 * @author Felipe Bravo-Marquez (fjb11@students.waikato.ac.nz)
 * @version $Revision: 1 $
 */


public class TweetToSparseFeatureVector extends SimpleBatchFilter {


	/** for serialization */
	private static final long serialVersionUID = 3635946466523698211L;


	/** Default path to where resources are stored. */
	public static String RESOURCES_FOLDER_NAME = weka.core.WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "resources";

	/** Default path of POS tagger model. */
	public static String TAGGER_FILE_NAME=RESOURCES_FOLDER_NAME+File.separatorChar+"model.20120919";

	/** The path of the word clusters. */
	public static String WORD_CLUSTERS_FILE_NAME=RESOURCES_FOLDER_NAME+File.separatorChar+"50mpaths2.txt.gz";


	/** Counts the number of tweets in which candidate attributes appear. This will help for discarding infrequent attributes. */
	protected Object2IntMap<String> attributeCount; 

	/** List of tweets to process with their feature vectors. */
	protected ObjectList<Object2IntMap<String>> procTweets; 

	/** Brown Clusters Dictionary */
	protected Object2ObjectMap<String,String> brownDict;

	/** True if url, users, and repeated letters are cleaned */
	protected boolean cleanTokens=false;

	/** True if the value of each feature is set to its frequency in the tweet. Boolean weights are used otherwise. */
	protected boolean freqWeights=true;

	/** The minimum number of tweets for an attribute to be considered in the attribute space. */
	protected int minAttDocs=0; 

	/** the index of the string attribute to be processed */
	protected int textIndex=1; 

	/** The maximum number of type of word ngrams to calculate. If n=3 Unigrams, bigrams and trigrams will calculated */
	protected int wordNgramMaxDim=1;

	/** Prefix for word ngram featues */
	protected String wordNgramPrefix="WNGRAM-";

	/** True for adding a prefix to words occurring in a negated context */
	protected boolean negateTokens=false;


	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;


	/** True to calculate character ngram features */
	protected boolean calculateCharNgram=false;

	/** Prefix for character ngram features */ 
	protected String charNgramPrefix="CNGRAM-";

	/** The minimum dimension for character ngrams.  */
	protected int charNgramMinDim=3;

	/** The maximum dimension for character ngrams.  */
	protected int charNgramMaxDim=5;


	/** The maximum dimension for POS ngrams.  */
	protected int posNgramMaxDim=0;


	/** the prefix of the POS-bases attributes */
	protected String posPrefix="POS-";


	/** The maximum dimension for cluster ngrams.  */
	protected int clustNgramMaxDim=0;


	/** the prefix of the cluster-based attributes */
	protected String clustPrefix="CLUST-";

	/** TwitterNLP Tagger model */
	protected transient Tagger tagger;

	/** The NegationEvaluator object with the negating list */
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
				+ " would depend on the training dataset.\n"+getTechnicalInformation().toString();
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


	/* (non-Javadoc)
	 * @see weka.filters.Filter#listOptions()
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector<Option> result = new Vector<Option>();

		result.addElement(new Option("\t Minimum number of tweets for an attribute to be considered.\n"
				+ "\t(default: " + this.minAttDocs + ")", "M", 1, "-M"));

		result.addElement(new Option("\t The index (starting from 1) of the target string attribute.\n"
				+ "\t(default: " + this.textIndex + ")", "I", 1, "-I"));		


		result.addElement(new Option("\t Add a prefix to words occurring in negated contexts e.g., I don't like you => I don't NEG-like NEG-you.\n "
				+ "\t The prefixes only affect word n-gram features. The scope of negation finishes with the next punctuation mark. \n"
				+ "\t(default: " + this.negateTokens + ")", "R", 0, "-R"));

		result.addElement(new Option("\t Maximum size for the word n-gram features. \n"
				+ "\t Set this variable to zero for no word n-gram attributes."
				+ " All word n-grams from i=1 to this value will be extracted."
				+ "\n"
				+ "\t(default: " + this.wordNgramMaxDim + ")", "Q", 1, "-Q"));


		result.addElement(new Option("\t Calculate character n-gram features."
				+ "\n"
				+ "\t(default: " + this.calculateCharNgram + ")", "A", 0, "-A"));


		result.addElement(new Option("\t The minimum size for character n-grams."
				+ "\n"
				+ "\t(default: " + this.charNgramMinDim + ")", "D", 1, "-D"));


		result.addElement(new Option("\t The maximum size for character n-grams."
				+ "\n"
				+ "\t(default: " + this.charNgramMaxDim + ")", "E", 1, "-E"));



		result.addElement(new Option("\t Lowercase content.\n"
				+ "\t(default: " + this.toLowerCase + ")", "L", 0, "-L"));


		result.addElement(new Option("\t Normalize tokens (replace goood by good, standarise URLs and @users).\n"
				+ "\t(default: " + this.cleanTokens + ")", "O", 0, "-O"));		

		result.addElement(new Option("\t True if the value of each feature is set to its frequency in the tweet. Boolean weights are used otherwise.\n"
				+ "\t(default: " + this.freqWeights + ")", "F", 0, "-F"));


		result.addElement(new Option("\t The maximum size for POS n-grams."
				+ " Set this variable to zero for no POS attributes. \n"
				+ "\t The tweets are POS-tagged using the CMU TweetNLP tool.\n"
				+ "\t(default: " + this.posNgramMaxDim + ")", "G", 1, "-G"));


		result.addElement(new Option("\t The maximum dimension for n-grams calculated with Brown word clusters.\n"
				+ "\t Set this variable to zero for no word-clusters attributes. \n"
				+ "\t The word clusters are taken from the CMU Tweet NLP tool.\n"
				+ "\t(default: " + this.clustNgramMaxDim + ")", "I", 1, "-I"));


		result.addAll(Collections.list(super.listOptions()));

		return result.elements();
	}


	/* (non-Javadoc)
	 * @see weka.filters.Filter#getOptions()
	 */
	@Override
	public String[] getOptions() {

		Vector<String> result = new Vector<String>();

		result.add("-M");
		result.add("" + this.getMinAttDocs());

		result.add("-I");
		result.add("" + this.getTextIndex());


		if(this.negateTokens)
			result.add("-R");

		result.add("-Q");
		result.add("" + this.getWordNgramMaxDim());

		if(this.calculateCharNgram)
			result.add("-A");


		result.add("-D");
		result.add("" + this.getCharNgramMinDim());

		result.add("-E");
		result.add("" + this.getCharNgramMaxDim());

		if(this.toLowerCase)
			result.add("-L");

		if(this.cleanTokens)
			result.add("-O");

		if(this.freqWeights)
			result.add("-F");

		result.add("-G");
		result.add("" + this.posNgramMaxDim);


		result.add("-I");
		result.add("" + this.clustNgramMaxDim);


		Collections.addAll(result, super.getOptions());

		return result.toArray(new String[result.size()]);
	}


	/**
	 * Parses the options for this object.
	 * <p/>
	 * 
	 * <!-- options-start -->
	 *<pre>  
	 *-M
	 *	 Minimum number of tweets for an attribute to be considered.
	 *	(default: 0)
	 *</pre> 
	 *<pre> 
	 *-I
	 *	 The index (starting from 1) of the target string attribute.
	 *	(default: 1)
	 *</pre>
	 *<pre>  
	 *-R
	 *	 Add a prefix to words occurring in negated contexts e.g., I don't like you => I don't NEG-like NEG-you. The prefixes only affect word n-gram features. The scope of negation finishes with the next punctuation mark. 
	 *	(default: false)
	 *</pre>
	 *<pre>  
	 *-Q
	 *	 Maximum size for the word n-gram features. All word n-grams from i=1 to this value will be extracted.
	 *	(default: 1)
	 *</pre>
	 *<pre>  
	 *-A
	 *	 Calculate character n-gram features.
	 *	(default: false)
	 *</pre> 
	 *<pre> 
	 *-D
	 *	 The minimum size for character n-grams.
	 *	(default: 3)
	 *</pre> 
	 *<pre> 
	 *-E
	 *	 The maximum size for character n-grams.
	 *	(default: 5)
	 *</pre> 
	 *<pre> 
	 *-L
	 *	 Lowercase content.
	 *	(default: false)
	 *</pre> 
	 *<pre> 
	 *-O
	 *	 Normarlize tokens (replace goood by good, standarise URLs and @users).
	 *	(default: false)
	 *</pre> 
	 *<pre> 
	 *-F
	 *	 True if the value of each feature is set to its frequency in the tweet. Boolean weights are used otherwise.
	 *	(default: false)
	 *</pre> 
	 *<pre> 
	 *-G
	 *	 The maximum size for POS n-grams. The tweets are POS-tagges using the CMU TweetNLP tool.
	 *	(default: 0)
	 *</pre> 
	 *<pre> 
	 *-I
	 *	 The maximum dimension for n-grams calculated with Brown word clusters. The word clusters are taken from the CMU Tweet NLP tool.
	 *	(default: 0)
	 *</pre>  
	 *  
	 *  <!-- options-end -->
	 * 
	 * @param options
	 *            the options to use
	 * @throws Exception
	 *             if setting of options fails
	 */
	@Override
	public void setOptions(String[] options) throws Exception {

		String textMinAttDocOption = Utils.getOption('M', options);
		if (textMinAttDocOption.length() > 0) {
			String[] textMinAttDocOptionSpec = Utils.splitOptions(textMinAttDocOption);
			if (textMinAttDocOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid value");
			}
			int minDocAtt = Integer.parseInt(textMinAttDocOptionSpec[0]);
			this.setMinAttDocs(minDocAtt);

		}



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

		this.negateTokens=Utils.getFlag('R', options);	

		String wordNgramMaxDimOption = Utils.getOption('Q', options);
		if (wordNgramMaxDimOption.length() > 0) {
			String[] wordNgramMaxDimOptionSpec = Utils.splitOptions(wordNgramMaxDimOption);
			if (wordNgramMaxDimOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid value");
			}			
			int ngramMaxDimOptionVal = Integer.parseInt( wordNgramMaxDimOptionSpec[0]);
			this.setWordNgramMaxDim(ngramMaxDimOptionVal);

		}


		this.calculateCharNgram=Utils.getFlag('A', options);		


		String charNgramMinDimOption = Utils.getOption('D', options);
		if (charNgramMinDimOption.length() > 0) {
			String[] charNgramMinDimOptionSpec = Utils.splitOptions(charNgramMinDimOption);
			if (charNgramMinDimOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid value");
			}			
			int charNgramMinDimOptionVal = Integer.parseInt( charNgramMinDimOptionSpec[0]);
			this.setCharNgramMinDim(charNgramMinDimOptionVal);

		}

		String charNgramMaxDimOption = Utils.getOption('E', options);
		if (charNgramMaxDimOption.length() > 0) {
			String[] charNgramMaxDimOptionSpec = Utils.splitOptions(charNgramMaxDimOption);
			if (charNgramMaxDimOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
			}			
			int charNgramMaxDimOptionVal = Integer.parseInt( charNgramMaxDimOptionSpec[0]);
			this.setCharNgramMaxDim(charNgramMaxDimOptionVal);

		}


		this.toLowerCase=Utils.getFlag('L', options);

		this.cleanTokens=Utils.getFlag('O', options);

		this.freqWeights=Utils.getFlag('F', options);


		String posNgramMaxDimOption = Utils.getOption('G', options);
		if (posNgramMaxDimOption.length() > 0) {
			String[] posNgramMaxDimOptionSpec = Utils.splitOptions(posNgramMaxDimOption);
			if (posNgramMaxDimOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
			}			
			int posNgramMaxDimOptionVal = Integer.parseInt(posNgramMaxDimOptionSpec[0]);
			this.setPosNgramMaxDim(posNgramMaxDimOptionVal);

		}


		String clustNgramMaxDimOption = Utils.getOption('I', options);
		if (clustNgramMaxDimOption.length() > 0) {
			String[] clustNgramMaxDimOptionSpec = Utils.splitOptions(clustNgramMaxDimOption);
			if (clustNgramMaxDimOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
			}			
			int clustNgramMaxDimOptionVal = Integer.parseInt(clustNgramMaxDimOptionSpec[0]);
			this.setClustNgramMaxDim(clustNgramMaxDimOptionVal);

		}


		super.setOptions(options);

		Utils.checkForRemainingOptions(options);


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
	 * Calculates token n-grams from a sequence of tokens.
	 * 
	 * @param tokens the input tokens from which the word n-grams will be calculated
	 * @param n the size of the word n-gram
	 * @return a list with the word n-grams
	 */
	public static List<String> calculateTokenNgram(List<String> tokens,int n){
		List<String> tokenNgram=new ArrayList<String>();
		if(tokens.size()>=n){			
			for(int i=0;i<=tokens.size()-n;i++){
				String ngram="";
				for(int j=i;j<i+n;j++){
					ngram+=tokens.get(j);
					if(j<i+n-1)
						ngram+="-";
				}				
				tokenNgram.add(ngram);
			}
		}
		return tokenNgram;		
	}


	/**
	 * Calculates character n-grams from a sequence of tokens.
	 * 
	 * @param tokens the input tokens from which the character n-grams will be calculated
	 * @param n the size of the character n-gram
	 * @return a list with the character n-grams
	 */
	public static List<String> extractCharNgram(String content,int n){
		List<String> charNgram=new ArrayList<String>();
		if(content.length()>=n){
			for(int i=0;i<content.length()-n;i++){
				String cgram="";
				for(int j=i;j<i+n;j++){
					cgram+=content.charAt(j);
				}				
				charNgram.add(cgram);

			}
		}

		return charNgram;		
	}


	/**
	 * Calculates a sequence of word-clusters from a list of tokens and a dictionary.
	 * 
	 * @param tokens the input tokens 
	 * @param dict the dictionary with the word clusters
	 * @return a list of word-clusters
	 */	
	public List<String> clustList(List<String> tokens, Map<String,String> dict){
		List<String> clusters=new ArrayList<String>();
		for(String token:tokens){
			if(dict.containsKey(token)){
				clusters.add(dict.get(token));
			}

		}	
		return clusters;
	}


	/**
	 * Calculates a vector of attributes from a list of tokens
	 * 
	 * @param tokens the input tokens 
	 * @param prefix the prefix of each vector attribute
	 * @return an Object2IntMap object mapping the attributes to their values
	 */		
	public Object2IntMap<String> calculateTermFreq(List<String> tokens, String prefix) {
		Object2IntMap<String> termFreq = new Object2IntOpenHashMap<String>();

		// Traverse the strings and increments the counter when the token was
		// already seen before
		for (String token : tokens) {
			// add frequency weights if the flat is set
			if(this.freqWeights)
				termFreq.put(prefix+token, termFreq.getInt(prefix+token) + 1);
			// otherwise, just consider boolean weights
			else{
				if(!termFreq.containsKey(token))
					termFreq.put(prefix+token, 1);
			}
		}

		return termFreq;
	}

	/**
	 * Initializes the POS tagger
	 */	
	public void initializeTagger(){
		try {
			this.tagger= new Tagger();
			this.tagger.loadModel(TAGGER_FILE_NAME);
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
		List<String> tokens=affective.core.Utils.tokenize(content,this.toLowerCase,this.cleanTokens); 
		Object2IntMap<String> docVec = new Object2IntOpenHashMap<String>();

		if(this.calculateCharNgram){
			for(int i=this.getCharNgramMinDim();i<=this.getCharNgramMaxDim();i++){
				docVec.putAll(calculateTermFreq(extractCharNgram(content,i),"CHAR-"+i+"-"));	
			}
		}

		if(this.clustNgramMaxDim>0){
			// calcultates the vector of clusters
			List<String> brownClust=clustList(tokens,brownDict);
			docVec.putAll(calculateTermFreq(brownClust,this.clustPrefix+"1-"));		
			// add ngrams where n > 1
			if(this.clustNgramMaxDim>1){
				for(int i=2;i<=this.clustNgramMaxDim;i++){
					docVec.putAll(calculateTermFreq(calculateTokenNgram(brownClust,i),this.clustPrefix+i+"-"));					
				}

			}
		}	

		if(this.posNgramMaxDim>0){
			List<String> posTags=this.getPOStags(tokens);
			docVec.putAll(calculateTermFreq(posTags,this.posPrefix+"1-"));
			// add ngrams where n > 1
			if(this.posNgramMaxDim>1){
				for(int i=2;i<=this.posNgramMaxDim;i++){
					docVec.putAll(calculateTermFreq(calculateTokenNgram(posTags,i),this.posPrefix+i+"-"));					
				}
			}
		}

		// use negated tokens for word ngrams features if option is set
		if(this.negateTokens)
			tokens=affective.core.Utils.negateTokens(tokens, this.negEval.getWordList());

		// add the ngram vectors
		if(this.wordNgramMaxDim>0){
			// add the unigrams
			docVec.putAll(calculateTermFreq(tokens,this.wordNgramPrefix+"1-"));
			// add ngrams where n > 1
			if(this.wordNgramMaxDim>1){
				for(int i=2;i<=this.wordNgramMaxDim;i++){
					docVec.putAll(calculateTermFreq(calculateTokenNgram(tokens,i),this.wordNgramPrefix+i+"-"));					
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
					FileInputStream fin = new FileInputStream(WORD_CLUSTERS_FILE_NAME);
					GZIPInputStream gzis = new GZIPInputStream(fin);
					InputStreamReader xover = new InputStreamReader(gzis);
					BufferedReader bf = new BufferedReader(xover);

					String line;
					while ((line = bf.readLine()) != null) {
						String pair[] = line.split("\t");
						brownDict.put(pair[1], pair[0]);


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
		Attribute attrCont = tweetInstances.attribute(this.textIndex-1);

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
	 * Get the minAttDocs value.
	 *
	 * @return the minAttDocs value.
	 */		
	public int getMinAttDocs() {
		return minAttDocs;
	}


	/**
	 * Sets the value of minAttDocs.
	 * 
	 * @param minAttDocs the value of minAttDocs.
	 * 
	 */
	public void setMinAttDocs(int minAttDocs) {
		this.minAttDocs = minAttDocs;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String minAttDocsTipText() {

		return "Minimum number of tweets for an attribute to be considered." ;
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
	 * Get the value of.
	 *
	 * @return the freqWeights value.
	 */			
	public boolean isFreqWeights() {
		return freqWeights;
	}



	/**
	 * Sets the value of the freqWeights flag.
	 * 
	 * @param freqWeights the value of the flag.
	 * 
	 */	
	public void setFreqWeights(boolean freqWeights) {
		this.freqWeights = freqWeights;
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String freqWeightsTipText() {
		return "True if the value of each feature is set to its frequency in the tweet. Boolean weights are used otherwise." ;
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
				+ "and replacing user mentions and URLs with generic tokens.";		
	}


	/**
	 * Get the wordNgramMaxDim value.
	 *
	 * @return the wordNgramMaxDim value.
	 */		
	public int getWordNgramMaxDim() {
		return wordNgramMaxDim;
	}


	/**
	 * Sets the value of wordNgramMaxDim.
	 * 
	 * @param wordNgramMaxDim the value of the variable.
	 * 
	 */
	public void setWordNgramMaxDim(int wordNgramMaxDim) {
		this.wordNgramMaxDim = wordNgramMaxDim;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String wordNgramMaxDimTipText() {

		return "Maximum size for the word n-gram features. All word n-grams from i=1 to this value will be extracted."
				+ " Set this variable to zero for no word n-gram attributes." ;
	}

	/**
	 * Get the negateTokens value.
	 *
	 * @return the negateTokens value.
	 */		
	public boolean isNegateTokens() {
		return negateTokens;
	}


	/**
	 * Sets the value of the negateTokens flag.
	 * 
	 * @param negateTokens the value of the flag.
	 * 
	 */	
	public void setNegateTokens(boolean negateTokens) {
		this.negateTokens = negateTokens;
	}

	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String negateTokensTipText() {

		return "Add a prefix to words occurring in negated contexts e.g., I don't like you => I don't NEG-like NEG-you. \n "
				+ "The prefixes only affect word n-gram features. \n"
				+ "The scope of negation finishes with the next punctuation mark. \n" ;
	}

	/**
	 * Get the calculateCharNgram value.
	 *
	 * @return the calculateCharNgram value.
	 */			
	public boolean isCalculateCharNgram() {
		return calculateCharNgram;
	}



	/**
	 * Sets the value of calculateCharNgram.
	 * 
	 * @param calculateCharNgram the value of the variable.
	 * 
	 */	
	public void setCalculateCharNgram(boolean calculateCharNgram) {
		this.calculateCharNgram = calculateCharNgram;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String calculateCharNgramTipText() {

		return "Calculate character n-gram features." ;
	}	


	/**
	 * Get the charNgramMinDim value.
	 *
	 * @return the charNgramMinDim value.
	 */			
	public int getCharNgramMinDim() {
		return charNgramMinDim;
	}



	/**
	 * Sets the value of charNgramMinDim .
	 * 
	 * @param charNgramMinDim the value of the variable.
	 * 
	 */	
	public void setCharNgramMinDim(int charNgramMinDim) {
		this.charNgramMinDim = charNgramMinDim;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String charNgramMinDimTipText() {

		return "The minimum size for character n-grams." ;
	}		


	/**
	 * Get the charNgramMaxDim value.
	 *
	 * @return the charNgramMaxDim value.
	 */			
	public int getCharNgramMaxDim() {
		return charNgramMaxDim;
	}



	/**
	 * Sets the value of the charNgramMaxDim variable.
	 * 
	 * @param charNgramMaxDim the value of the variable.
	 * 
	 */	
	public void setCharNgramMaxDim(int charNgramMaxDim) {
		this.charNgramMaxDim = charNgramMaxDim;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String charNgramMaxDimTipText() {

		return "The maximum size for character n-grams." ;
	}		


	/**
	 * Get the posNgramMaxDim value.
	 *
	 * @return the posNgramMaxDim value.
	 */		
	public int getPosNgramMaxDim() {
		return posNgramMaxDim;
	}



	/**
	 * Sets the value of the posNgramMaxDim value.
	 * 
	 * @param posNgramMaxDim the value of the variable.
	 * 
	 */	
	public void setPosNgramMaxDim(int posNgramMaxDim) {
		this.posNgramMaxDim = posNgramMaxDim;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String posNgramMaxDimTipText() {
		return "The maximum size for POS n-grams. Set this variable to zero for no POS attributes. "
				+ "The tweets are POS-tagged using the CMU TweetNLP tool." ;
	}	


	/**
	 * Get the clustNgramMaxDim value.
	 *
	 * @return the clustNgramMaxDim value.
	 */		
	public int getClustNgramMaxDim() {
		return clustNgramMaxDim;
	}


	/**
	 * Sets the value of the clustNgramMaxDim variable.
	 * 
	 * @param clustNgramMaxDim the value of the variable.
	 * 
	 */
	public void setClustNgramMaxDim(int clustNgramMaxDim) {
		this.clustNgramMaxDim = clustNgramMaxDim;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String clustNgramMaxDimTipText() {
		return "	 The maximum dimension for n-grams calculated with Brown word clusters."
				+ " Set this variable to zero for no word-clusters attributes. "
				+ "	The word clusters are taken from the CMU Tweet NLP tool." ;
	}		



	public static void main(String[] args) {
		runFilter(new TweetToSparseFeatureVector(), args);
	}

}
