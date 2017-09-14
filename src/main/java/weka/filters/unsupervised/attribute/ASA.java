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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import affective.core.ArffLexiconEvaluator;



import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionMetadata;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.WekaPackageManager;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Type;
import weka.filters.SimpleBatchFilter;


/**
 *  <!-- globalinfo-start --> 
 *  
 *  A batch filter that creates polarity labeled instances from unlabeled tweets and a seed polarity lexicon using the Annotate-Sample-Average Algorithm.
 *  Annotate-Sample-Average (ASA) is a lexical-based distant supervision method for training polarity classifiers in Twitter in the absence of labelled data.
 *  ASA takes a collection of unlabelled tweets and a polarity lexicon composed of positive and negative words and creates synthetic labelled instances. 
 *  Each labelled instance is created by sampling with replacement a number of tweets containing at least one word from the lexicon with the desired polarity, and averaging the feature vectors of the sampled tweets.
 *  
 * <!-- globalinfo-end -->
 * 
 *  
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class ASA  extends SimpleBatchFilter {



	/** For serialization.    **/
	private static final long serialVersionUID = 1616693021695150782L;


	/** Default path to where resources are stored. */
	public static String RESOURCES_FOLDER_NAME = weka.core.WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "resources";


	/** Default path to where lexicons are stored */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons"+ File.separator + "arff_lexicons";


	/** the prefix of the word attributes */
	static public String UNIPREFIX="WORD-";

	/** the prefix of the cluster-based attributes */
	static public String CLUSTPREFIX="CLUST-";




	/** List of tweets having at least one positive word and no negative words*/
	protected ObjectList<Object2IntMap<String>> posTweets;

	/** List of tweets having at least one negative word and no positive words*/
	protected ObjectList<Object2IntMap<String>> negTweets;


	/** Counts the number of documents in which candidate attributes appear */
	protected Object2IntMap<String> attributeCount;


	/** Contains a mapping of valid attribute with their indexes. */
	protected Object2IntMap<String> m_Dictionary;

	/** Brown Clusters Dictionary */
	protected Object2ObjectMap<String,String> brownDict;


	/** True if the value of each feature is set to its frequency in the tweet. Boolean weights are used otherwise. */
	protected boolean freqWeights=true;

	/** the minimum number of documents for an attribute to be considered. */
	protected int minAttDocs=0; 


	/** The random number seed  */
	protected int m_randomSeed = 1;

	/** The number of tweets sampled in each centroid */
	protected int tweetsPerCentroid=10;	

	/** The number of positive instances to generate */
	protected int numPosInstances=1000;


	/** The number of negative instances to generate */
	protected int numNegInstances=1000;

	/** the index of the string attribute to be processed */
	protected int textIndex=1; 


	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;


	/** True for calculating word-based attributes . */
	protected boolean createWordAtts=true;


	/** True for calculating cluster-based attributes . */
	protected boolean createClustAtts=true;



	/** True if url, users, and repeated letters are cleaned */
	protected boolean cleanTokens=false;



	/** The path of the seed lexicon . */
	protected File lexicon=new File(LEXICON_FOLDER_NAME+File.separator+"BingLiu.arff");

	/** The path of the word clusters. */
	protected File wordClustFile=new File(RESOURCES_FOLDER_NAME+File.separator+"50mpaths2.txt.gz");

	/** Create Exclusive Sets */
	protected boolean exclusiveSets=false;



	/** The target lexicon attribute */
	protected String polarityAttName="polarity";


	/** The positive attribute value name in the lexicon */
	protected String polarityAttPosValName="positive";


	/** The negative attribute value name in the lexicon */
	protected String polarityAttNegValName="negative";


	/** LexiconEvaluator for sentiment prefixes */
	protected ArffLexiconEvaluator lex=new ArffLexiconEvaluator();






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
		result.setValue(TechnicalInformation.Field.TITLE, "Annotate-Sample-Average (ASA): A New Distant Supervision Approach for Twitter Sentiment Analysis");
		result.setValue(TechnicalInformation.Field.YEAR, "2016");
		result.setValue(TechnicalInformation.Field.BOOKTITLE, "22nd European Conference on Artificial Intelligence (ECAI)");
		return result;
	}





	@Override
	public String globalInfo() {
		return "Annotate-Sample-Average (ASA) is a lexical-based distant supervision method for training polarity classifiers in Twitter in the absence of labelled data. " +
				"ASA takes a collection of unlabelled tweets and a polarity lexicon composed of positive and negative words and creates synthetic labelled instances. " +
				"Each labelled instance is created by sampling with replacement a number of tweets containing at least one word from the lexicon with the desired polarity, and averaging the feature vectors of the sampled tweets." +
				"\n Use this filter with the FilteredClassifier. \n"+getTechnicalInformation().toString();
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



	/* (non-Javadoc)
	 * @see weka.filters.Filter#listOptions()
	 */
	@Override
	public Enumeration<Option> listOptions() {
		return Option.listOptionsForClass(this.getClass()).elements();
	}


	/* (non-Javadoc)
	 * @see weka.filters.Filter#getOptions()
	 */
	@Override
	public String[] getOptions() {		
		return Option.getOptions(this, this.getClass());
	}


	/**
	 * Parses the options for this object.
	 * 
	 * <!-- options-start --> 
	 * <pre> 
	 *-I &lt;col&gt;
	 *  Index of string attribute (default: 1)
	 * </pre>
	 * <pre>
	 *-U 
	 *	 Lowercase content	(default: false)
	 * </pre>
	 * <pre>
	 *-O 
	 *	 Clean tokens (replace goood by good, standarise URLs and @users) 	(default: false)
	 *</pre> 
	 *  
	 * <!-- options-end -->
	 * 
	 * @param options
	 *            the options to use
	 * @throws Exception
	 *             if setting of options fails
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		Option.setOptions(options, this, this.getClass());
	}



	/* To allow determineOutputFormat to access to entire dataset
	 * (non-Javadoc)
	 * @see weka.filters.SimpleBatchFilter#allowAccessToFullInputFormat()
	 */
	public boolean allowAccessToFullInputFormat() {
		return true;
	}








	// Maps a given instance with an attribute called content into a target instance with the same dimensions
	public Instances mapTargetInstance(Instances inp){

		// Creates instances with the same format
		Instances result=getOutputFormat();


		Attribute contentAtt=inp.attribute(this.textIndex-1);


		for(Instance inst:inp){
			String content=inst.stringValue(contentAtt);



			List<String> tokens=affective.core.Utils.tokenize(content, this.toLowerCase, this.cleanTokens); 

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






	/* Annotates the given tweets according to 
	 * 
	 */	 
	public void annotatePhase(Instances inputFormat) {

		this.lex.setLexiconFile(this.lexicon);


		try {
			this.lex.processDict();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		this.posTweets=new ObjectArrayList<Object2IntMap<String>>();
		this.negTweets=new ObjectArrayList<Object2IntMap<String>>();

		this.attributeCount= new Object2IntOpenHashMap<String>(); 



		// the Dictionary of the brown Clusters
		if(this.createClustAtts){
			this.brownDict=new Object2ObjectOpenHashMap<String,String>();
			try {
				FileInputStream fin = new FileInputStream(this.wordClustFile);
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
				this.setCreateClustAtts(false);
			}

		}




		// reference to the content of the message, users index start from zero
		Attribute attrCont = inputFormat.attribute(this.textIndex-1);

		for (ListIterator<Instance> it = inputFormat.listIterator(); it
				.hasNext();) {
			Instance inst = it.next();
			String content = inst.stringValue(attrCont);


			// tokenises the content 
			List<String> tokens=affective.core.Utils.tokenize(content,this.toLowerCase,this.cleanTokens); 

			// Identifies the distinct terms
			AbstractObjectSet<String> terms=new  ObjectOpenHashSet<String>(); 
			terms.addAll(tokens);

			boolean hasPos=false;
			boolean hasNeg=false;

			for(String word:tokens){
				if(this.lex.getNomDict().containsKey(word)){
					String value=this.lex.getNomDict().get(word).get(this.polarityAttName);
					if(value.equals(this.polarityAttPosValName))
						hasPos=true;
					else if(value.equals(this.polarityAttNegValName))
						hasNeg=true;
				}


			}


			boolean condition=false;
			if(this.exclusiveSets)
				condition=(hasPos&&!hasNeg || !hasPos&&hasNeg);
			else
				condition=(hasPos || hasNeg);


			Object2IntMap<String> docVec=this.calculateDocVec(tokens);

			if(condition){			

				if(hasPos)
					this.posTweets.add(docVec);
				if(hasNeg)
					this.negTweets.add(docVec);

			}
			// adds the attributes to the List of attributes
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

	@Override
	protected Instances determineOutputFormat(Instances inputFormat) {




		//	inputFormat.setClassIndex(inputFormat.numAttributes()-1);

		// calculates the word frequency vectors and the vocabulary

		if (!this.isFirstBatchDone()){
			this.annotatePhase(inputFormat);
		}

		// the dictionary of words and attribute indexes
		this.m_Dictionary=new Object2IntOpenHashMap<String>();


		ArrayList<Attribute> att = new ArrayList<Attribute>();

		int i=0;

		for(String attribute:this.attributeCount.keySet()){
			if(this.attributeCount.get(attribute)>=this.minAttDocs){
				Attribute a = new Attribute(attribute);
				att.add(a);		
				this.m_Dictionary.put(attribute, i);
				i++;

			}
		}


		att.add(inputFormat.classAttribute());


		Instances result = new Instances(inputFormat.relationName(), att, 0);


		result.setClassIndex(result.numAttributes()-1);

		return result;
	}




	@Override
	protected Instances process(Instances instances) throws Exception {

		Instances result;


		// The first batch creates de labelled data		
		if(!this.isFirstBatchDone()){
			result = getOutputFormat();

			Random r=new Random(this.m_randomSeed);


			for(int i=0;i<this.numPosInstances;i++){
				double[] values = new double[result.numAttributes()];
				for(int j=0;j<this.getTweetsPerCentroid();j++){
					int randomIndex=r.nextInt(this.posTweets.size()); 
					Object2IntMap<String> vec=this.posTweets.get(randomIndex);
					for(String innerAtt:vec.keySet()){
						if(this.m_Dictionary.containsKey(innerAtt)){
							int attIndex=this.m_Dictionary.getInt(innerAtt);
							// we normalise the value by the number of documents
							values[attIndex]+=((double)vec.getInt(innerAtt))/this.getTweetsPerCentroid();
						}
					}				

				}
				values[result.numAttributes()-1]=1;

				Instance inst=new SparseInstance(1, values);


				inst.setDataset(result);

				result.add(inst);	

			}

			for(int i=0;i<this.numNegInstances;i++){

				double[] values = new double[result.numAttributes()];

				for(int j=0;j<this.getTweetsPerCentroid();j++){
					int randomIndex=r.nextInt(this.negTweets.size()); 
					Object2IntMap<String> vec=this.negTweets.get(randomIndex);
					for(String innerAtt:vec.keySet()){
						if(this.m_Dictionary.containsKey(innerAtt)){
							int attIndex=this.m_Dictionary.getInt(innerAtt);
							// we normalise the value by the number of documents
							values[attIndex]+=((double)vec.getInt(innerAtt))/this.getTweetsPerCentroid();
						}
					}				

				}
				values[result.numAttributes()-1]=0;

				Instance inst=new SparseInstance(1, values);


				inst.setDataset(result);

				result.add(inst);	

			}
		}

		// Second batch maps tweets into the corresponding feature space
		else{
			result=this.mapTargetInstance(instances);

		}



		return result;

	}







	@OptionMetadata(displayName = "textIndex",
			description = "The index (starting from 1) of the target string attribute.",
			commandLineParamName = "I", commandLineParamSynopsis = "-I <int>",
			displayOrder = 0)
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



	@OptionMetadata(displayName = "cleanTokens",
			description = "Reduce the attribute space by replacing sequences of letters occurring more than two "
					+ "times in a row with two occurrences of them (e.g., huuungry is reduced to huungry, loooove to loove), "
					+ "and replacing user mentions and URLs with generic tokens.", 
					commandLineParamIsFlag = true, commandLineParamName = "O", 
					commandLineParamSynopsis = "-O",
					displayOrder = 2)	
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


	@OptionMetadata(displayName = "minAttDocs",
			description = "Minimum frequency of a sparse attribute to be considered in the attribute space.", 
			commandLineParamName = "M", 
			commandLineParamSynopsis = "-M <int>",
			displayOrder = 3)	
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
			displayOrder = 4)	
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
			displayOrder = 5)	
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
			displayOrder = 6)	
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
			displayOrder = 7)	
	public File getLexicon() {
		return lexicon;
	}
	public void setLexicon(File lexicon) {
		this.lexicon = lexicon;
	}




	@OptionMetadata(displayName = "tweetsPerCentroid",
			description = "The number of tweets to average in each generated instance. \t default: 10", 
			commandLineParamName = "A", 
			commandLineParamSynopsis = "-A <int>",
			displayOrder = 8)
	public int getTweetsPerCentroid() {
		return tweetsPerCentroid;
	}
	public void setTweetsPerCentroid(int tweetsPerCentroid) {
		this.tweetsPerCentroid = tweetsPerCentroid;
	}




	@OptionMetadata(displayName = "numPosInstances",
			description = "The number of positive instances to generaTE. \t default: 1000", 
			commandLineParamName = "npos", 
			commandLineParamSynopsis = "-npos <int>",
			displayOrder = 9)	
	public int getNumPosInstances() {
		return numPosInstances;
	}
	public void setNumPosInstances(int numPosInstances) {
		this.numPosInstances = numPosInstances;
	}



	@OptionMetadata(displayName = "numNegInstances",
			description = "The number of negative instances to generate. \t default: 1000", 
			commandLineParamName = "nneg", 
			commandLineParamSynopsis = "-nneg <int>",
			displayOrder = 10)
	public int getNumNegInstances() {
		return numNegInstances;
	}
	public void setNumNegInstances(int numNegInstances) {
		this.numNegInstances = numNegInstances;
	}



	@OptionMetadata(displayName = "exclusiveSets",
			description = "True for dicarding tweets containing both positive and negative words. False for adding these tweets into both the positive and negative sets from which tweets are sampled." +
					"\t default False",
					commandLineParamIsFlag = true, commandLineParamName = "E", 
					commandLineParamSynopsis = "-E",
					displayOrder = 11)	
	public boolean isExclusiveSets() {
		return exclusiveSets;
	}
	public void setExclusiveSets(boolean exclusiveSets) {
		this.exclusiveSets = exclusiveSets;
	}




	@OptionMetadata(displayName = "randomseed",
			description = "The random seed number. \t default: 1", 
			commandLineParamName = "R", 
			commandLineParamSynopsis = "-R <int>",
			displayOrder = 12)
	public int getRandomSeed() {
		return m_randomSeed;	}
	public void setRandomSeed(int randomSeed) {
		this.m_randomSeed = randomSeed;
	}


	

	@OptionMetadata(displayName = "polarityAttName",
			description = "The lexicon attribute name with the word polarities. \t default: polarity", 
			commandLineParamName = "polatt", 
			commandLineParamSynopsis = "-polatt <string>",
			displayOrder = 13)	
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
			displayOrder = 14)
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
			displayOrder = 15)
	public String getPolarityAttNegValName() {
		return polarityAttNegValName;
	}
	public void setPolarityAttNegValName(String polarityAttNegValName) {
		this.polarityAttNegValName = polarityAttNegValName;
	}


	public static void main(String[] args) {
		runFilter(new ASA(), args);
	}

}
