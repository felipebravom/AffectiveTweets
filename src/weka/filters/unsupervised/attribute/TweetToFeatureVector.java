package weka.filters.unsupervised.attribute;


import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import affective.core.MyUtils;
import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Twokenize;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.filters.SimpleBatchFilter;

public class TweetToFeatureVector extends SimpleBatchFilter {

	/**
	 * 	 Samples labelled tweets from labelled words
	 * 
	 */



	private static final long serialVersionUID = 7553647795494402690L;

	protected String[] posEmoticons={":)","=)",":d","=d",":]","=]",":-)",":-d",":-]",";)",";d",";]",";-)",";-d",";-]"};

	protected String[] negEmoticons={":(","=(",";(",":[","=[",":-(",":-[",":’(",":’[","d:"};

	/** List of tweets with their feature vectors*/
	protected ObjectList<Object2IntMap<String>> procTweets;


	/** Counts the number of documents in which candidate attributes appear */
	protected Object2IntMap<String> attributeCount;


	/** Contains a mapping of valid attribute with their indexes. */
	protected Object2IntMap<String> m_Dictionary;

	/** Brown Clusters Dictionary */
	protected Object2ObjectMap<String,String> brownDict;

	/** the minimum number of tweets for an attribute to be considered. */
	protected int minAttDocs=0; 


	/** the index of the string attribute to be processed */
	protected int textIndex=1; 

	/** the prefix of the word attributes */
	protected String wordPrefix="NGRAM-";

	/** the prefix of the cluster-based attributes */
	protected String clustPrefix="CLUST-";

	/** the prefix of the POS-bases attributes */
	protected String posPrefix="POS-";


	/** True for calculating POS attributes. */
	protected boolean createPosAtts=true;



	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;


	/** True for calculating word-based attributes . */
	protected boolean createWordAtts=true;


	/** True for calculating cluster-based attributes . */
	protected boolean createClustAtts=true;


	/** True is stopwords are discarded */
	protected boolean removeStopWords=false;

	/** The stopwords file */
	protected String stopWordsPath="resources/stopwords.txt";


	/** True if url, users, and repeated letters are cleaned */
	protected boolean cleanTokens=false;


	/** The path of the word clusters. */
	protected String clustPath="resources/50mpaths2.txt";


	/** The POS tagger. */
	protected Tagger tagger; 



	@Override
	public String globalInfo() {
		return "A batch filter that creates a vector representation of words with polarity labels "
				+ "provided by a seed lexicon.  ";
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

		result.addElement(new Option("\t Create word-based attributes.\n"
				+ "\t(default: " + this.createWordAtts + ")", "W", 0, "-W"));

		result.addElement(new Option("\t Create cluster-based attributes.\n"
				+ "\t(default: " + this.createClustAtts + ")", "C", 0, "-C"));


		result.addElement(new Option("\t Index of string attribute.\n"
				+ "\t(default: " + this.textIndex + ")", "I", 1, "-I"));		

		result.addElement(new Option("\t Prefix of word attributes.\n"
				+ "\t(default: " + this.wordPrefix + ")", "P", 1, "-P"));


		result.addElement(new Option("\t Prefix of cluster attributes.\n"
				+ "\t(default: " + this.clustPrefix + ")", "Q", 1, "-Q"));


		result.addElement(new Option("\t Lowercase content.\n"
				+ "\t(default: " + this.toLowerCase + ")", "L", 0, "-L"));

		result.addElement(new Option("\t The path of the file with the word clusters.\n"
				+ "\t(default: " + this.clustPath + ")", "H", 1, "-H"));

		result.addElement(new Option("\t Discard stopwords.\n"
				+ "\t(default: " + this.removeStopWords + ")", "S", 0, "-S"));

		result.addElement(new Option("\t The path of the stopwords file.\n"
				+ "\t(default: " + this.stopWordsPath + ")", "T", 1, "-T"));

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

		result.add("-M");
		result.add("" + this.getMinAttDocs());

		if(this.createWordAtts)
			result.add("-W");

		if(this.createClustAtts)
			result.add("-C");

		result.add("-I");
		result.add("" + this.getTextIndex());

		result.add("-P");
		result.add("" + this.getWordPrefix());

		result.add("-Q");
		result.add("" + this.getClustPrefix());


		if(this.toLowerCase)
			result.add("-L");

		result.add("-H");
		result.add("" + this.getClustPath());

		if(this.isRemoveStopWords())
			result.add("-S");

		result.add("-T");
		result.add("" + this.getStopWordsPath());

		if(this.isCleanTokens())
			result.add("-O");

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


		this.createWordAtts=Utils.getFlag('W', options);

		this.createClustAtts=Utils.getFlag('C', options);




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

		String wordPrefixOption = Utils.getOption('P', options);
		if (wordPrefixOption.length() > 0) {
			String[] wordPrefixSpec = Utils.splitOptions(wordPrefixOption);
			if (wordPrefixSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
			}
			String wordPref = wordPrefixSpec[0];
			this.setWordPrefix(wordPref);

		}

		String clustPrefixOption = Utils.getOption('Q', options);
		if (clustPrefixOption.length() > 0) {
			String[] clustPrefixOptionSpec = Utils.splitOptions(clustPrefixOption);
			if (clustPrefixOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
			}
			String clustPref = clustPrefixOptionSpec[0];
			this.setClustPrefix(clustPref);

		}


		this.toLowerCase=Utils.getFlag('L', options);


		String clustPathOption = Utils.getOption('H', options);
		if (clustPathOption.length() > 0) {
			String[] clustPathOptionSpec = Utils.splitOptions(clustPathOption);
			if (clustPathOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
			}
			String clustPathVal = clustPathOptionSpec[0];
			this.setClustPath(clustPathVal);

		}


		this.removeStopWords=Utils.getFlag('S', options);


		String stopWordsPathOption = Utils.getOption('T', options);
		if (stopWordsPath.length() > 0) {
			String[] stopWordsPathSpec = Utils.splitOptions(stopWordsPathOption);
			if (stopWordsPathSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
			}
			String stopWordsPathVal = stopWordsPathSpec[0];
			this.setStopWordsPath(stopWordsPathVal);

		}

		this.cleanTokens=Utils.getFlag('O', options);



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


	// tokenises and cleans the content 
	public List<String> tokenize(String content) {

		if(this.toLowerCase)
			content=content.toLowerCase();


		if(!this.cleanTokens&&!this.removeStopWords)
			return Twokenize.tokenizeRawTweetText(content);


		AbstractObjectSet<String> stopWords=null;

		if(this.removeStopWords){
			stopWords=new  ObjectOpenHashSet<String>(); 
			// we add the stop-words from the file
			try {
				BufferedReader bf=new BufferedReader(new FileReader(this.stopWordsPath));
				String line;
				while((line=bf.readLine())!=null){
					stopWords.add(line);
				}
				bf.close();				

			} catch (IOException e) {
				this.removeStopWords=false;

			}


		}


		// if a letters appears two or more times it is replaced by only two
		// occurrences of it
		if(this.cleanTokens)
			content = content.replaceAll("([a-z])\\1+", "$1$1");

		List<String> tokens = new ArrayList<String>();

		for (String word : Twokenize.tokenizeRawTweetText(content)) {
			String cleanWord = word;


			if(this.cleanTokens){
				// Replace URLs to a generic URL
				if (word.matches("http.*|ww\\..*")) {
					cleanWord = "http://www.url.com";
				}

				// Replaces user mentions to a generic user
				else if (word.matches("@.*")) {
					cleanWord = "@user";
				}

				// check stopWord
				if(this.removeStopWords){
					if(stopWords.contains(cleanWord))
						continue;

				}

			}

			tokens.add(cleanWord);
		}
		return tokens;
	}


	public Object2IntMap<String> calculateTermFreq(List<String> tokens, String prefix) {
		Object2IntMap<String> termFreq = new Object2IntOpenHashMap<String>();

		// Traverse the strings and increments the counter when the token was
		// already seen before
		for (String token : tokens) {
			termFreq.put(prefix+token, termFreq.getInt(prefix+token) + 1);			
		}

		return termFreq;
	}




	/* Converts a sequence of words into a sequence of word-clusters
	 * 
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


	

	public Object2IntMap<String> calculateDocVec(List<String> tokens) {

		Object2IntMap<String> docVec = new Object2IntOpenHashMap<String>();
		// add the word-based vector
		if(this.createWordAtts)
			docVec.putAll(calculateTermFreq(tokens,this.wordPrefix));

		if(this.createClustAtts){
			// calcultates the vector of clusters
			List<String> brownClust=clustList(tokens,brownDict);
			docVec.putAll(calculateTermFreq(brownClust,this.clustPrefix));			
		}	

		if(this.createPosAtts){
			List<String> posTags=MyUtils.getPOStags(tokens, this.tagger);
			docVec.putAll(calculateTermFreq(posTags,this.posPrefix));
		}


		return docVec;

	}







	public void processTweets(Instances inputFormat) {


		// Most objects are initialized in the first batch
		if (!this.isFirstBatchDone()){

			this.attributeCount= new Object2IntOpenHashMap<String>(); 



			// the Dictionary of the brown Clusters
			if(this.createClustAtts){
				this.brownDict=new Object2ObjectOpenHashMap<String,String>();
				try {
					BufferedReader bf = new BufferedReader(new FileReader(
							this.clustPath));
					String line;
					while ((line = bf.readLine()) != null) {
						String pair[] = line.split("\t");
						brownDict.put(pair[1], pair[0]);


					}
					bf.close();

				} catch (IOException e) {
					// do not create clusters attributes
					this.setCreateClustAtts(false);
				}

			}


			// Loads the POS tagger model
			if(this.createPosAtts){				
				try {
					this.tagger= new Tagger();
					this.tagger.loadModel("models/model.20120919");
				} catch (IOException e) {
					this.createPosAtts=false;
				}

			}



		}



		this.procTweets=new ObjectArrayList<Object2IntMap<String>>();


		// reference to the content of the message, users index start from zero
		Attribute attrCont = inputFormat.attribute(this.textIndex-1);

		for (ListIterator<Instance> it = inputFormat.listIterator(); it
				.hasNext();) {
			Instance inst = it.next();
			String content = inst.stringValue(attrCont);


			// tokenises the content 
			List<String> tokens=this.tokenize(content); 


			// Identifies the distinct terms
			AbstractObjectSet<String> terms=new  ObjectOpenHashSet<String>(); 
			terms.addAll(tokens);

			Object2IntMap<String> docVec=this.calculateDocVec(tokens);	

			this.procTweets.add(docVec);

			// adds the attributes to the List of attributes in the first batch
			if (!this.isFirstBatchDone()) {

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


		// We start with processing the input tweets
		this.processTweets(inputFormat);

		// the dictionary of words and attribute indexes
		this.m_Dictionary=new Object2IntOpenHashMap<String>();


		ArrayList<Attribute> att = new ArrayList<Attribute>();




		int i=0;

		// Adds all other attributes from the input data
		for (; i < inputFormat.numAttributes(); i++) {
			att.add(inputFormat.attribute(i));
		}	

		for(String attribute:this.attributeCount.keySet()){
			if(this.attributeCount.get(attribute)>=this.minAttDocs){
				Attribute a = new Attribute(attribute);
				att.add(a);		
				this.m_Dictionary.put(attribute, i);
				i++;

			}
		}






		Instances result = new Instances(inputFormat.relationName(), att, 0);

		return result;
	}

	@Override
	protected Instances process(Instances instances) throws Exception {

		Instances result = getOutputFormat();

		// if we are in the testing data we calculate the word vectors again
		if (this.isFirstBatchDone()) {
			this.processTweets(instances);
		}



		int i=0; // counts the tweets in the input data 
		for(Object2IntMap<String> vec:this.procTweets){



			double[] values = new double[result.numAttributes()];

			// copy the values of all the input attributes
			for (int n = 0; n < instances.numAttributes(); n++)
				values[n] = instances.instance(i).value(n);


			for(String innerAtt:vec.keySet()){
				if(this.m_Dictionary.containsKey(innerAtt)){
					int attIndex=this.m_Dictionary.getInt(innerAtt);
					// we normalise the value by the number of documents
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
	 * Sets the index of the string attribute
	 * 
	 * @return the index of the documents.
	 */
	public int getTextIndex() {
		return textIndex;
	}

	/**
	 * Sets the index of the string attribute
	 * 
	 * @param textIndex the index of the string attribute
	 * 
	 */
	public void setTextIndex(int textIndex) {
		this.textIndex = textIndex;
	}


	public String getWordPrefix() {
		return wordPrefix;
	}


	public void setWordPrefix(String prefix) {
		this.wordPrefix = prefix;
	}


	public String getClustPrefix() {
		return clustPrefix;
	}



	public void setClustPrefix(String clustPrefix) {
		this.clustPrefix = clustPrefix;
	}


	public boolean isToLowerCase() {
		return toLowerCase;
	}

	public void setToLowerCase(boolean toLowerCase) {
		this.toLowerCase = toLowerCase;
	}


	public int getMinAttDocs() {
		return minAttDocs;
	}



	public void setMinAttDocs(int minAttDocs) {
		this.minAttDocs = minAttDocs;
	}


	public boolean isCreateWordAtts() {
		return createWordAtts;
	}



	public void setCreateWordAtts(boolean createWordAtts) {
		this.createWordAtts = createWordAtts;
	}

	public void setCreateClustAtts(boolean createClustAtts) {
		this.createClustAtts = createClustAtts;
	}


	public boolean isCreateClustAtts() {
		return createClustAtts;
	}



	public String getClustPath() {
		return clustPath;
	}



	public void setClustPath(String clustPath) {
		this.clustPath = clustPath;
	}




	public boolean isRemoveStopWords() {
		return removeStopWords;
	}



	public void setRemoveStopWords(boolean removeStopWords) {
		this.removeStopWords = removeStopWords;
	}



	public String getStopWordsPath() {
		return stopWordsPath;
	}



	public void setStopWordsPath(String stopWordsPath) {
		this.stopWordsPath = stopWordsPath;
	}



	public boolean isCleanTokens() {
		return cleanTokens;
	}



	public void setCleanTokens(boolean cleanTokens) {
		this.cleanTokens = cleanTokens;
	}

	public ObjectList<Object2IntMap<String>> getPosTweets() {
		return procTweets;
	}



	public void setPosTweets(ObjectList<Object2IntMap<String>> posTweets) {
		this.procTweets = posTweets;
	}



	public static void main(String[] args) {
		runFilter(new TweetToFeatureVector(), args);
	}

}
