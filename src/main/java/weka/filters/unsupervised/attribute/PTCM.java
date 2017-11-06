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
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import affective.core.ArffLexiconEvaluator;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionMetadata;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.Utils;
import weka.core.WekaPackageManager;
import weka.core.TechnicalInformation.Type;

public class PTCM extends TweetToFeatureVector{

	/**
	 *  Represents words and tweets by vectors of the same dimensionality unsing the Tweet Centroid Model. 
	 *  Uses a lexicon to label word vectors calculated from unlabelled tweets, the target tweets are mapped
	 *  into a compatible feature space.
	 *   
	 **/ 



	private static final long serialVersionUID = 7553647795494402690L;


	/** Default path to where resources are stored. */
	public static String RESOURCES_FOLDER_NAME = weka.core.WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "resources";


	/** Default path to where lexicons are stored */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons"+ File.separator + "arff_lexicons";



	/** True if the value of each feature is set to its frequency in the tweet. Boolean weights are used otherwise. */
	protected boolean freqWeights=true;


	/** The path of the seed lexicon . */
	protected File lexicon=new File(LEXICON_FOLDER_NAME+File.separator+"BingLiu.arff");

	/** The path of the word clusters. */
	protected File wordClustFile=new File(RESOURCES_FOLDER_NAME+File.separator+"50mpaths2.txt.gz");


	/** the vocabulary and the WordRep */
	protected transient Object2ObjectMap<String, WordRep> wordInfo; 

	/** Counts the number of documents in which candidate attributes appear */
	protected Object2IntMap<String> attributeCount;


	/** Contains a mapping of valid attribute with their indexes. */
	protected Object2IntMap<String> m_Dictionary;

	/** Brown Clusters Dictionary */
	protected Object2ObjectMap<String,String> brownDict;

	/** the minimum number of documents for an attribute to be considered. */
	protected int minAttDocs=0; 


	/** the minimum number of documents for a word to be included. */
	protected int minInstDocs=0; 


	/** the number of parititions in each centrod */
	protected int partNumber=-1;

	/** the prefix of the word attributes */
	static public String UNIPREFIX="WORD-";

	/** the prefix of the cluster-based attributes */
	static public String CLUSTPREFIX="CLUST-";


	/** True for calculating word-based attributes . */
	protected boolean createWordAtts=true;


	/** True for calculating cluster-based attributes . */
	protected boolean createClustAtts=true;



	/** The target lexicon attribute */
	protected String polarityAttName="polarity";


	/** The positive attribute value name in the lexicon */
	protected String polarityAttPosValName="positive";


	/** The negative attribute value name in the lexicon */
	protected String polarityAttNegValName="negative";


	/** LexiconEvaluator for sentiment prefixes */
	protected ArffLexiconEvaluator lex=new ArffLexiconEvaluator();




	@Override
	public String globalInfo() {
		return "Partioned Tweet Centroid Model (PTCM) is a lexical-based distant supervision method for training polarity classifiers in Twitter in the absence of labelled data. " +
				"PTCM takes calculated word vectors from a corpus of unalabelled tweets using the Tweet Centroid Model. These word vectors are labelled using a seed lexicon. " +
				"The tweets from the second batch are represented with compatible features." +
				"\n Use this filter with the FilteredClassifier. \n"+getTechnicalInformation().toString();
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
		result.setValue(TechnicalInformation.Field.TITLE, " From opinion lexicons to sentiment classification of tweets and vice versa: a transfer learning approach.");
		result.setValue(TechnicalInformation.Field.YEAR, "2016");
		result.setValue(TechnicalInformation.Field.BOOKTITLE, "Proceedings of the 2016 IEEE/WIC/ACM International Conference on Web Intelligence, Omaha, Nebraska, USA 2016.");
		return result;
	}


	// This class contains all the information of the word to compute the centroid
	class WordRep {

		String word; // the word
		int numDoc; // number of documents where the word occurs		
		ObjectList<Object2IntMap<String>> postingList; 


		public WordRep(String word){
			this.word=word;
			this.numDoc=0;
			this.postingList=new ObjectArrayList<Object2IntMap<String>>();
		}

		public void addDoc(Object2IntMap<String> docVector){
			this.postingList.add(docVector);
			this.numDoc++;
		}


		// returns a list of partions of the posting List
		public ObjectList<ObjectList<Object2IntMap<String>>> partitionate(int partSize){

			ObjectList<ObjectList<Object2IntMap<String>>> resList= new ObjectArrayList<ObjectList<Object2IntMap<String>>>();

			// if the partition size is larger than the posting list, then put the whole list into one partition
			// if partsize is less or equal than zero we create one single partition too, which is equivalent as the full
			// tweet centroid model
			if(partSize>=this.postingList.size() || partSize <=0){
				resList.add(this.postingList);
			}
			else{
				int i=0;
				while(i+partSize<=this.postingList.size()){
					resList.add(this.postingList.subList(i, i+partSize));
					i+=partSize;				
				}
				if(i<this.postingList.size()&& i+partSize>this.postingList.size() ){
					resList.add(this.postingList.subList(i, this.postingList.size()));
				}

			}



			return resList;

		}

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



	// Maps a given instance with an attribute called content into a target instance with the same dimensions
	public Instances mapTargetInstance(Instances inp){
		// Creates instances with the same format
		Instances result=getOutputFormat();
		Attribute contentAtt=inp.attribute(this.m_textIndex.getIndex());

		for(Instance inst:inp){
			String content=inst.stringValue(contentAtt);

			// tokenises the content 
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






	/* Calculates the vocabulary and the word vectors from an Instances object
	 * The vocabulary is only extracted the first time the filter is run.
	 * 
	 */	 
	public void computeWordVecsAndVoc(Instances inputFormat) {


		this.lex.setLexiconFile(this.lexicon);

		try {
			this.lex.processDict();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}



		this.wordInfo = new Object2ObjectOpenHashMap<String, WordRep>();

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
		Attribute attrCont = inputFormat.attribute(this.m_textIndex.getIndex());



		for (ListIterator<Instance> it = inputFormat.listIterator(); it
				.hasNext();) {
			Instance inst = it.next();
			String content = inst.stringValue(attrCont);


			// tokenises the content 
			List<String> tokens = affective.core.Utils.tokenize(content, this.toLowerCase, this.standarizeUrlsUsers, this.reduceRepeatedLetters, this.m_tokenizer,this.m_stemmer,this.m_stopwordsHandler);


			// Identifies the distinct terms
			AbstractObjectSet<String> terms=new  ObjectOpenHashSet<String>(); 
			terms.addAll(tokens);


			Object2IntMap<String> docVec=this.calculateDocVec(tokens);			




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



			// if the word is new we add it to the vocabulary, otherwise we
			// add the document to the vector
			for (String word : terms) {
				if(this.lex.getNomDict().containsKey(word)){				
					String value=this.lex.getNomDict().get(word).get(this.polarityAttName);
					if(value.equals(this.polarityAttPosValName)||value.equals(this.polarityAttNegValName)){
						if (this.wordInfo.containsKey(word)) {
							WordRep wordRep=this.wordInfo.get(word);
							wordRep.addDoc(docVec); // add the document
						} else{
							WordRep wordRep=new WordRep(word);
							wordRep.addDoc(docVec); // add the document
							this.wordInfo.put(word, wordRep);						
						}
					}
				}
			}

		}
	}



	@Override
	protected Instances determineOutputFormat(Instances inputFormat) {


		// set upper value for text index
		m_textIndex.setUpper(inputFormat.numAttributes() - 1);

		// calculates the word frequency vectors and the vocabulary
		if(!this.isFirstBatchDone()){
			this.computeWordVecsAndVoc(inputFormat);
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

			for(String word:this.wordInfo.keySet()){
				// get the word vector
				WordRep wordRep=this.wordInfo.get(word);

				// We just consider valid words
				if(wordRep.numDoc>=this.minInstDocs){

					// a list of lists of tweet vectors
					ObjectList<ObjectList<Object2IntMap<String>>> partitions=wordRep.partitionate(this.getPartNumber());

					// traverse the partitions
					for(ObjectList<Object2IntMap<String>> tweetPartition:partitions){
						// create one instance per partition	
						double[] values = new double[result.numAttributes()];

						// average the vectors of the tweets in the partition
						// traverse each feature space in the partition
						for(Object2IntMap<String> wordSpace:tweetPartition){

							for(String innerWord:wordSpace.keySet()){
								// only include valid words
								if(this.m_Dictionary.containsKey(innerWord)){
									int attIndex=this.m_Dictionary.getInt(innerWord);
									// we normalise the value by the number of documents
									values[attIndex]+=((double)wordSpace.getInt(innerWord))/tweetPartition.size();					
								}
							}
						}



						String wordPol=this.lex.getNomDict().get(word).get(this.polarityAttName);
						if(wordPol.equals(this.polarityAttNegValName))
							values[result.numAttributes()-1]=0;
						else if(wordPol.equals(this.polarityAttPosValName))
							values[result.numAttributes()-1]=1;
						else
							values[result.numAttributes()-1]= Utils.missingValue();					



						Instance inst=new SparseInstance(1, values);


						inst.setDataset(result);

						result.add(inst);




					}
				}
			}
		}

		// Second batch maps tweets into the corresponding feature space
		else{
			result=this.mapTargetInstance(instances);

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



	@OptionMetadata(displayName = "minInstDocs",
			description = "Minimum frequency of a word to be considered in the instance space.",
			commandLineParamName = "N", 
			commandLineParamSynopsis = "-N <int>",
			displayOrder = 7)	
	public int getMinInstDocs() {
		return minInstDocs;
	}
	public void setMinInstDocs(int minInstDocs) {
		this.minInstDocs = minInstDocs;
	}

	@OptionMetadata(displayName = "createWordAtts",
			description = "True for creating unigram attributes.", 
			commandLineParamIsFlag = true, commandLineParamName = "W", 
			commandLineParamSynopsis = "-W",
			displayOrder = 8)	
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
			displayOrder = 9)	
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
			displayOrder = 10)	
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
			displayOrder = 11)	
	public File getLexicon() {
		return lexicon;
	}
	public void setLexicon(File lexicon) {
		this.lexicon = lexicon;
	}




	@OptionMetadata(displayName = "partNumber",
			description = "The size of the partition for the tweet centroid model (-1 for not partionining). \t default: -1", 
			commandLineParamName = "A", 
			commandLineParamSynopsis = "-A <int>",
			displayOrder = 12)
	public int getPartNumber() {
		return partNumber;
	}
	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}



	public static void main(String[] args) {
		runFilter(new PTCM(), args);
	}

}
