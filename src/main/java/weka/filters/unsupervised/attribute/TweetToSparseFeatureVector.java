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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.impl.ModelSentence;
import cmu.arktweetnlp.impl.Sentence;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.filters.SimpleBatchFilter;

public class TweetToSparseFeatureVector extends SimpleBatchFilter {

	/**  Converts one String attribute into a set of attributes
	 * representing word occurrence based on the TwitterNLP tokenizer.
	 * 
	 */


	/** for serialization */
	private static final long serialVersionUID = 3635946466523698211L;
	
	
	/** Default path to where resources are stored */
	public static String RESOURCES_FOLDER_NAME = weka.core.WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "resources";

	/** Default path of POS tagger model. */
	public static String TAGGER_FILE_NAME=RESOURCES_FOLDER_NAME+File.separatorChar+"model.20120919";
	
	/** The path of the word clusters. */
	public static String WORD_CLUSTERS_FILE_NAME=RESOURCES_FOLDER_NAME+File.separatorChar+"50mpaths2.txt.gz";

	

	/** Counts the number of documents in which candidate attributes appear. This will help for discarding infrequent attributes */
	protected Object2IntMap<String> attributeCount; 

	/** List of tweets to process with their feature vectors*/
	protected ObjectList<Object2IntMap<String>> procTweets; 
	
	/** Brown Clusters Dictionary */
	protected Object2ObjectMap<String,String> brownDict;
	
	/** True if url, users, and repeated letters are cleaned */
	protected boolean cleanTokens=false;


	/** True if the value of each feature is set to its frequency in the tweet. Boolean weights are used otherwise */
	protected boolean freqWeights=true;
	
	/** the minimum number of tweets for an attribute to be considered. */
	protected int minAttDocs=0; 

	/** the index of the string attribute to be processed */
	protected int textIndex=1; 
	
	/** The maximum number of type of word ngrams to calculate. If n=3 Unigrams, bigrams and trigrams will calculated */
	protected int wordNgramMaxDim=1;

	/** Prefix for word ngram featues */
	protected String wordNgramPrefix="WNGRAM-";
			

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



	@Override
	public String globalInfo() {
		return "A simple batch filter that adds attributes for all the "
				+ "Twitter-oriented POS tags of the TwitterNLP library.  ";
	}



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




	@Override
	public Enumeration<Option> listOptions() {
		Vector<Option> result = new Vector<Option>();

		result.addElement(new Option("\t Minimum count of an attribute.\n"
				+ "\t(default: " + this.minAttDocs + ")", "M", 1, "-M"));

		result.addElement(new Option("\t Index of string attribute.\n"
				+ "\t(default: " + this.textIndex + ")", "I", 1, "-I"));		

		result.addElement(new Option("\t Prefix of word ngram features.\n"
				+ "\t(default: " + this.wordNgramPrefix + ")", "P", 1, "-P"));

		
		result.addElement(new Option("\t Maximum number of n for ngram features. If n=3, unigrams, bigrams and trigrams will be extracted."
				+ "\n"
				+ "\t(default: " + this.wordNgramMaxDim + ")", "Q", 1, "-Q"));
		
		
		result.addElement(new Option("\t Calculate character ngram featues."
				+ "\n"
				+ "\t(default: " + this.calculateCharNgram + ")", "A", 0, "-A"));
		

		result.addElement(new Option("\t Prefix of character ngram featues."
				+ "\n"
				+ "\t(default: " + this.charNgramPrefix + ")", "B", 1, "-B"));

		
		result.addElement(new Option("\t The minimum dimension for character ngrams.."
				+ "\n"
				+ "\t(default: " + this.charNgramMinDim + ")", "D", 1, "-D"));
		
		
		result.addElement(new Option("\t The maximum dimension for character ngrams.."
				+ "\n"
				+ "\t(default: " + this.charNgramMaxDim + ")", "E", 1, "-E"));
		
		
		
		result.addElement(new Option("\t Lowercase content.\n"
				+ "\t(default: " + this.toLowerCase + ")", "L", 0, "-L"));

		
		result.addElement(new Option("\t Clean tokens (replace goood by good, standarise URLs and @users).\n"
				+ "\t(default: " + this.cleanTokens + ")", "O", 0, "-O"));		
		
		result.addElement(new Option("\t True if the value of each feature is set to its frequency in the tweet. Boolean weights are used otherwise.\n"
				+ "\t(default: " + this.freqWeights + ")", "F", 0, "-F"));
		
		
		result.addElement(new Option("\t The maximum dimension for POS ngrams.\n"
				+ "\t(default: " + this.posNgramMaxDim + ")", "G", 1, "-G"));
		
		result.addElement(new Option("\t The prefix of POS-based attributes.\n"
				+ "\t(default: " + this.posPrefix + ")", "H", 1, "-H"));
		
		result.addElement(new Option("\t The maximum dimension for ngrams calculated with Brown word clusters.\n"
				+ "\t(default: " + this.clustNgramMaxDim + ")", "I", 1, "-I"));

		
		result.addElement(new Option("\t The prefix of the cluster-based attributes\n"
				+ "\t(default: " + this.clustPrefix + ")", "K", 1, "-K"));
	
	
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

		result.add("-M");
		result.add("" + this.getMinAttDocs());

		result.add("-I");
		result.add("" + this.getTextIndex());

		result.add("-P");
		result.add("" + this.getWordNgramPrefix());
		
		result.add("-Q");
		result.add("" + this.getWordNgramMaxDim());
			
		if(this.calculateCharNgram)
			result.add("-A");
		

		result.add("-B");
		result.add("" + this.getCharNgramPrefix());
		
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
		
		result.add("-H");
		result.add("" + this.posPrefix);
		
		result.add("-I");
		result.add("" + this.clustNgramMaxDim);

		
		result.add("-K");
		result.add("" + this.clustPrefix);		
		
	

		Collections.addAll(result, super.getOptions());

		return result.toArray(new String[result.size()]);
	}


	/**
	 * Parses the options for this object.
	 * <p/>
	 * 
	 * <!-- options-start --> <!-- options-end -->
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
						"Invalid index");
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

		String wordNgramPrefixOption = Utils.getOption('P', options);
		if (wordNgramPrefixOption.length() > 0) {
			String[] wordNgramPrefixOptionSpec = Utils.splitOptions(wordNgramPrefixOption);
			if (wordNgramPrefixOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
			}
			String wordNgramPrefixValue = wordNgramPrefixOptionSpec[0];
			this.setWordNgramPrefix(wordNgramPrefixValue);

		}
		
		String wordNgramMaxDimOption = Utils.getOption('Q', options);
		if (wordNgramMaxDimOption.length() > 0) {
			String[] wordNgramMaxDimOptionSpec = Utils.splitOptions(wordNgramMaxDimOption);
			if (wordNgramMaxDimOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
			}			
			int ngramMaxDimOptionVal = Integer.parseInt( wordNgramMaxDimOptionSpec[0]);
			this.setWordNgramMaxDim(ngramMaxDimOptionVal);

		}
		

		this.calculateCharNgram=Utils.getFlag('A', options);		
		
		
		String charNgramPrefixOption = Utils.getOption('B', options);
		if (charNgramPrefixOption.length() > 0) {
			String[] charNgramPrefixOptionSpec = Utils.splitOptions(charNgramPrefixOption);
			if (charNgramPrefixOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
			}
			String charNgramPrefixValue = charNgramPrefixOptionSpec[0];
			this.setCharNgramPrefix(charNgramPrefixValue);

		}


		
		String charNgramMinDimOption = Utils.getOption('D', options);
		if (charNgramMinDimOption.length() > 0) {
			String[] charNgramMinDimOptionSpec = Utils.splitOptions(charNgramMinDimOption);
			if (charNgramMinDimOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
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
		
		String posPrefixOption = Utils.getOption('H', options);
		if (posPrefixOption.length() > 0) {
			String[] posPrefixOptionSpec = Utils.splitOptions(posPrefixOption);
			if (posPrefixOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
			}
			String posPrefixValue = posPrefixOptionSpec[0];
			this.setPosPrefix(posPrefixValue);

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
		
		
		String clustPrefixOption = Utils.getOption('K', options);
		if (clustPrefixOption.length() > 0) {
			String[] clustPrefixOptionSpec = Utils.splitOptions(clustPrefixOption);
			if (clustPrefixOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
			}
			String clustPrefixValue = clustPrefixOptionSpec[0];
			this.setClustPrefix(clustPrefixValue);

		}
		
		
		
		super.setOptions(options);

		Utils.checkForRemainingOptions(options);


	}



	/* To allow determineOutputFormat to access to entire dataset
	 * (non-Javadoc)
	 * @see weka.filters.SimpleBatchFilter#allowAccessToFullInputFormat()
	 */
	public boolean allowAccessToFullInputFormat() {
		return true;
	}

	

	
	// Calculates token ngrams from a sequence of tokens
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
	

	// Calculates Character Ngrams
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
	
	
	/* Converts a sequence of words into a sequence of word-clusters 	 */	 	
	public List<String> clustList(List<String> tokens, Map<String,String> dict){
		List<String> clusters=new ArrayList<String>();
		for(String token:tokens){
			if(dict.containsKey(token)){
				clusters.add(dict.get(token));
			}

		}	
		return clusters;
	}
	

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
	
	// Initializes the POS tagger
	public void initializeTagger(){
		try {
			this.tagger= new Tagger();
			this.tagger.loadModel(TAGGER_FILE_NAME);
		} catch (IOException e) {
			this.posNgramMaxDim=0;
		}
	}
	
	
	// Returns POS tags from a List of tokens using TwitterNLP
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
	
	

	public Object2IntMap<String> calculateDocVec(String content) {
		
		// tokenizes the content 
		List<String> tokens=affective.core.Utils.tokenize(content,this.toLowerCase,this.cleanTokens); 

		Object2IntMap<String> docVec = new Object2IntOpenHashMap<String>();
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
			

		return docVec;

	}
	
	

	/* Processes a batch of tweets. Tweets are mapped into feature vectors.
	 * The feature space is only determined the first time the filter is run.
	 * 
	 */	 
	public void tweetsToVectors(Instances inputFormat) {


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

		}
		
		
		// Loads the POS tagger model 
		if(this.posNgramMaxDim>0 && this.tagger==null){				
			this.initializeTagger();
		}

		this.procTweets = new ObjectArrayList<Object2IntMap<String>>();

		// reference to the content of the message, users index start from zero
		Attribute attrCont = inputFormat.attribute(this.textIndex-1);

		for (ListIterator<Instance> it = inputFormat.listIterator(); it
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

	@Override
	protected Instances process(Instances instances) throws Exception {

		Instances result = getOutputFormat();

		// if we are in the testing data we calculate the word vectors again
		if (this.isFirstBatchDone()) {
			this.tweetsToVectors(instances);
		}

		// System.out.println("++++" + instances);

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




	public int getMinAttDocs() {
		return minAttDocs;
	}



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
	
	
	public boolean isFreqWeights() {
		return freqWeights;
	}



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

		return "The index (starting from 1) of the target string attribute." ;
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
	

	public int getWordNgramMaxDim() {
		return wordNgramMaxDim;
	}



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

		return "The index (starting from 1) of the target string attribute." ;
	}
		
	

	public String getWordNgramPrefix() {
		return wordNgramPrefix;
	}



	public void setWordNgramPrefix(String wordNgramPrefix) {
		this.wordNgramPrefix = wordNgramPrefix;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String wordNgramPrefixTipText() {

		return "The index (starting from 1) of the target string attribute." ;
	}
	

	public boolean isCalculateCharNgram() {
		return calculateCharNgram;
	}



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

		return "The index (starting from 1) of the target string attribute." ;
	}	


	public int getCharNgramMinDim() {
		return charNgramMinDim;
	}



	public void setCharNgramMinDim(int charNgramMinDim) {
		this.charNgramMinDim = charNgramMinDim;
	}


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String charNgramTipText() {

		return "The index (starting from 1) of the target string attribute." ;
	}		
	

	public int getCharNgramMaxDim() {
		return charNgramMaxDim;
	}



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

		return "The index (starting from 1) of the target string attribute." ;
	}		
	
	
	public String getCharNgramPrefix() {
		return charNgramPrefix;
	}



	public void setCharNgramPrefix(String charNgramPrefix) {
		this.charNgramPrefix = charNgramPrefix;
	}

	

	public String getPosPrefix() {
		return posPrefix;
	}



	public void setPosPrefix(String posPrefix) {
		this.posPrefix = posPrefix;
	}


	
	
	public int getPosNgramMaxDim() {
		return posNgramMaxDim;
	}



	public void setPosNgramMaxDim(int posNgramMaxDim) {
		this.posNgramMaxDim = posNgramMaxDim;
	}



	public int getClustNgramMaxDim() {
		return clustNgramMaxDim;
	}



	public void setClustNgramMaxDim(int clustNgramMaxDim) {
		this.clustNgramMaxDim = clustNgramMaxDim;
	}
	

	public String getClustPrefix() {
		return clustPrefix;
	}



	public void setClustPrefix(String clustPrefix) {
		this.clustPrefix = clustPrefix;
	}
	

	public static void main(String[] args) {
		runFilter(new TweetToSparseFeatureVector(), args);
	}

}
