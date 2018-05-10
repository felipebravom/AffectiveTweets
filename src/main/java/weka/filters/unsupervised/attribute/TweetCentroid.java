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
 *    TweetCentroid.java
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */


package weka.filters.unsupervised.attribute;


import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
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
import java.util.zip.GZIPInputStream;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionMetadata;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Type;


/**
 *  <!-- globalinfo-start --> 
 *  A filter that creates word vectors from tweets using the Tweet Centroid Model. 
 *  Each word is calculated as the average vector of the tweets in which it occurs.   
 * <!-- globalinfo-end -->
 * 
 *  
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class TweetCentroid extends TweetToFeatureVector {


	/** For serialization.    **/
	private static final long serialVersionUID = 7553647795494402690L;


	/** Default path to where resources are stored. */
	public static String RESOURCES_FOLDER_NAME = weka.core.WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "resources";

	/** the prefix of the word attributes */
	static String UNIPREFIX="WORD-";

	/** the prefix of the cluster-based attributes */
	static String CLUSTPREFIX="CLUST-";


	/** the vocabulary and the WordRep */
	protected Object2ObjectMap<String, WordRep> wordInfo; 

	/** Numerical attributes to be transferred to the word-level */
	protected ObjectList<Attribute> numericAttributes;


	/** Counts the number of documents in which candidate attributes appears */
	protected Object2IntMap<String> attributeCount;


	/** Contains a mapping of valid attribute with their indexes. */
	protected Object2IntMap<String> m_Dictionary;

	/** Brown Clusters Dictionary */
	protected Object2ObjectMap<String,String> brownDict;


	/** True if the value of each feature is set to its frequency in the tweet. Boolean weights are used otherwise. */
	protected boolean freqWeights=true;


	/** the minimum number of documents for an attribute to be considered. */
	protected int minAttDocs=0; 


	/** the minimum number of documents for a word to be included. */
	protected int minInstDocs=0; 


	/** True for calculating word-based attributes . */
	protected boolean createWordAtts=true;


	/** True for calculating cluster-based attributes . */
	protected boolean createClustAtts=true;



	/** True if additional numerical attributes should be included to the centroid */
	protected boolean considerNumericAtts=true;



	/** The path of the word clusters. */
	protected File wordClustFile=new File(RESOURCES_FOLDER_NAME+File.separator+"50mpaths2.txt.gz");





	/**
	 * This class contains all the information of a word for calculating a word vector.
	 *
	 */
	class WordRep{
		/** The word. */
		String word; 

		/** The number of documents where the word occurs. */
		int numDoc; 

		/** The vector representation of the word. */
		Object2IntMap<String> wordSpace; 

		/** Additional numeric attributes occurring in the original dataset.  */
		Object2DoubleMap<String> metaData; //


		/**
		 * Creates a new WordRep object.
		 * @param word the word
		 */
		public WordRep(String word){
			this.word=word;
			this.numDoc=0;
			this.wordSpace=new Object2IntOpenHashMap<String>();
			this.metaData=new Object2DoubleOpenHashMap<String>();
		}


		/**
		 * Adds a new document to the word representation.
		 * @param docVector a  document vector
		 */
		public void addDoc(Object2IntMap<String> docVector){
			this.numDoc++;
			for(String vecWord:docVector.keySet()){
				int vecWordFreq=docVector.getInt(vecWord);
				// if the word was seen before we add the current frequency
				this.wordSpace.put(vecWord,vecWordFreq+this.wordSpace.getInt(vecWord));
			}	

		}

		/**
		 * Adds values of additional attributes to the word vector.
		 * @param metaVector the values of the additional attributes of the document
		 */
		public void addMetaData(Object2DoubleMap<String> metaVector){
			for(String metaName:metaVector.keySet()){
				double metaVal=metaVector.getDouble(metaName);
				this.metaData.put(metaName, metaVal+this.metaData.getDouble(metaName));
			}
		}


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
		result.setValue(TechnicalInformation.Field.TITLE, "From Unlabelled Tweets to Twitter-specific Opinion Words");
		result.setValue(TechnicalInformation.Field.YEAR, "2015");
		result.setValue(TechnicalInformation.Field.BOOKTITLE, "SIGIR '15: Proceedings of the 38th International ACM SIGIR Conference on Research & Development in Information Retrieval. Santiago, Chile.");
		result.setValue(TechnicalInformation.Field.URL, "http://dl.acm.org/citation.cfm?id=2767770");

		return result;
	}

	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#globalInfo()
	 */
	@Override
	public String globalInfo() {
		return "A filter that creates word vectors from tweets using the Tweet Centroid Model."
				+ "Each word is calculated as the average vector of the tweets in which it occurs."				
				+ "\n"+getTechnicalInformation().toString();
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



	/**
	 * Calculates the vocabulary and the word vectors from an Instances object
	 * The vocabulary is only extracted the first time the filter is run.
	 * @param inputFormat the input Instances
	 */
	public void computeWordVecsAndVoc(Instances inputFormat) {


		if (!this.isFirstBatchDone()){

			// set upper value for text index
			m_textIndex.setUpper(inputFormat.numAttributes() - 1);


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



			// if considerNumericAtts is set to true we will include additional features in the word vectors
			if(this.considerNumericAtts){
				this.numericAttributes=new  ObjectArrayList<Attribute>();
				for(int i=0;i<inputFormat.numAttributes();i++){
					if(i!=this.m_textIndex.getIndex() && inputFormat.attribute(i).type()==Attribute.NUMERIC){
						this.numericAttributes.add(inputFormat.attribute(i));						
					}

				}
			}



			// reference to the content of the message
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


					WordRep wordRep;

					if (this.wordInfo.containsKey(word)) {
						wordRep=this.wordInfo.get(word);
						wordRep.addDoc(docVec); // add the document

					} else{
						wordRep=new WordRep(word);
						wordRep.addDoc(docVec); // add the document
						this.wordInfo.put(word, wordRep);						
					}

					if(this.considerNumericAtts){
						Object2DoubleMap<String> metaValues=new Object2DoubleOpenHashMap<String>();
						for(Attribute att:this.numericAttributes){
							// just consider attribute with values different from zero
							double val=inst.value(att);
							if(val!=0.0)
								metaValues.put(att.name(),val);
						}
						wordRep.addMetaData(metaValues);						
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

		// calculates the word frequency vectors and the vocabulary
		this.computeWordVecsAndVoc(inputFormat);


		// the dictionary of words and attribute indexes
		this.m_Dictionary=new Object2IntOpenHashMap<String>();


		ArrayList<Attribute> att = new ArrayList<Attribute>();

		int i=0;


		if(this.considerNumericAtts){
			for(Attribute metaAtt:this.numericAttributes){
				att.add(metaAtt);
				i++;
			}
		}


		for(String attribute:this.attributeCount.keySet()){
			if(this.attributeCount.get(attribute)>=this.minAttDocs){
				Attribute a = new Attribute(attribute);
				att.add(a);		
				this.m_Dictionary.put(attribute, i);
				i++;

			}
		}


		att.add(new Attribute("WORD_NAME", (ArrayList<String>) null));


		Instances result = new Instances(inputFormat.relationName(), att, 0);

		return result;
	}

	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#process(weka.core.Instances)
	 */
	@Override
	protected Instances process(Instances instances) throws Exception {



		Instances result = getOutputFormat();

		for(String word:this.wordInfo.keySet()){
			// get the word vector
			WordRep wordRep=this.wordInfo.get(word);

			// We just consider valid words
			if(wordRep.numDoc>=this.minInstDocs){
				double[] values = new double[result.numAttributes()];


				for(String wordFeat:wordRep.wordSpace.keySet()){
					// only include valid words
					if(this.m_Dictionary.containsKey(wordFeat)){
						int attIndex=this.m_Dictionary.getInt(wordFeat);
						// we normalise the value by the number of documents
						values[attIndex]=((double)wordRep.wordSpace.getInt(wordFeat))/wordRep.numDoc;					
					}
				}


				if(this.considerNumericAtts){
					for(Attribute metaAtt:this.numericAttributes){
						String metaAttName=metaAtt.name();
						values[result.attribute(metaAttName).index()]= wordRep.metaData.getDouble(metaAtt.name())/wordRep.numDoc;
					}


				}


				int wordNameIndex=result.attribute("WORD_NAME").index();
				values[wordNameIndex]=result.attribute(wordNameIndex).addStringValue(word);					


				Instance inst=new SparseInstance(1, values);

				inst.setDataset(result);

				result.add(inst);

			}


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




	@OptionMetadata(displayName = "considerNumericAtts",
			description = "True for considering all numeric attributes in the original dataset in the averaged word vectors.",
			commandLineParamIsFlag = true, 
			commandLineParamName = "natt", 
			commandLineParamSynopsis = "-natt",
			displayOrder = 11)	
	public boolean isIncludeMetaData() {
		return considerNumericAtts;
	}
	public void setIncludeMetaData(boolean includeMetaData) {
		this.considerNumericAtts = includeMetaData;
	}



	@OptionMetadata(displayName = "freqWeights",
			description = "True if the value of each feature is set to its frequency in the tweet. Boolean weights are used otherwise.",
			commandLineParamIsFlag = true, 
			commandLineParamName = "F", 
			commandLineParamSynopsis = "-F",
			displayOrder = 12)		
	public boolean isFreqWeights() {
		return freqWeights;
	}
	public void setFreqWeights(boolean freqWeights) {
		this.freqWeights = freqWeights;
	}


	/**
	 * Main method for testing this class.
	 *
	 * @param args should contain arguments to the filter: use -h for help
	 */		
	public static void main(String[] args) {
		runFilter(new TweetCentroid(), args);
	}

}
