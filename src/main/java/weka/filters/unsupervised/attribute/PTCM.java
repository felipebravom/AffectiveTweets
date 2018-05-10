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
 *    PTCM.java
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */



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
import weka.core.Utils;
import weka.core.TechnicalInformation.Type;


/**
 *  <!-- globalinfo-start --> 
 *	Partitioned Tweet Centroid Model (PTCM) is a lexicon-based distant supervision method for training polarity classifiers in Twitter in the absence of labeled data. 
 *  Word vectors are calculated using the Tweet Centroid Model and labeled using the lexicon.  The tweets from the second batch are represented with compatible features.
 * <!-- globalinfo-end -->
 * 
 *  
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class PTCM extends DistantSupervisionSyntheticFilter{


	/** For serialization. */
	private static final long serialVersionUID = 7826961590788120593L;	


	/** A mapping between words and their representations. */
	protected transient Object2ObjectMap<String, WordRep> wordInfo; 


	/** The minimum number of documents for a word to be included. */
	protected int minInstDocs=0; 


	/** The partition size of each centroid. */
	protected int partNumber=-1;



	@Override
	public String globalInfo() {
		return "Partitioned Tweet Centroid Model (PTCM) is a lexicon-based distant supervision method for training polarity classifiers in Twitter in the absence of labeled data. " +
				"Word vectors are calculated using the Tweet Centroid Model and labeled using the lexicon.  The tweets from the second batch are represented with compatible features. " +
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


	/**
	 * This class contains all the information of the word to compute the centroid.
	 *
	 */
	class WordRep {

		/** The word. */
		String word; 
		
		/** The number of documents where the word occurs. */	
		int numDoc; 	
		
		/** The document vectors in which the word occurs. */
		ObjectList<Object2IntMap<String>> postingList; 


		/**
		 * Creates a new WordRep object.
		 * @param word the word
		 */
		public WordRep(String word){
			this.word=word;
			this.numDoc=0;
			this.postingList=new ObjectArrayList<Object2IntMap<String>>();
		}

		/**
		 * Adds a new document to the posting list.
		 * @param docVector a  document vector
		 */
		public void addDoc(Object2IntMap<String> docVector){
			this.postingList.add(docVector);
			this.numDoc++;
		}


	
		/**
		 * Returns a list of partitions of the posting list.
		 * @param partSize the size of the partitions.
		 * @return a list of word vectors.
		 */
		public ObjectList<ObjectList<Object2IntMap<String>>> partition(int partSize){

			ObjectList<ObjectList<Object2IntMap<String>>> resList= new ObjectArrayList<ObjectList<Object2IntMap<String>>>();

			// if the partition size is larger than the posting list, then put the whole list into one partition
			// if partsize is less or equal than zero we create one single partition too, which is equivalent to the full
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





	/**
	 * Calculates the vocabulary and the word vectors from an Instances object
	 * The vocabulary is only extracted the first time the filter is run.
	 * @param inputFormat the input Instances
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



		// reference to the content of the message
		Attribute attrCont = inputFormat.attribute(this.m_textIndex.getIndex());


		for (ListIterator<Instance> it = inputFormat.listIterator(); it
				.hasNext();) {
			Instance inst = it.next();
			String content = inst.stringValue(attrCont);


			// tokenizes the content 
			List<String> tokens = affective.core.Utils.tokenize(content, this.toLowerCase, this.standarizeUrlsUsers, this.reduceRepeatedLetters, this.m_tokenizer,this.m_stemmer,this.m_stopwordsHandler);


			// Identifies the distinct terms
			AbstractObjectSet<String> terms=new  ObjectOpenHashSet<String>(); 
			terms.addAll(tokens);


			Object2IntMap<String> docVec=this.calculateDocVec(tokens);			




			// adds the attributes to the global list of attributes
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
			// add the document to the word representation
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



	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#determineOutputFormat(weka.core.Instances)
	 */
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




	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#process(weka.core.Instances)
	 */
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
					ObjectList<ObjectList<Object2IntMap<String>>> partitions=wordRep.partition(this.getPartNumber());

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
									// we normalize the value by the number of documents
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

		// Second batch maps tweets into the original feature space
		else{
			result=this.mapTargetInstance(instances);

		}

		return result;

	}




	@OptionMetadata(displayName = "minInstDocs",
			description = "Minimum frequency of a word to be considered in the instance space.",
			commandLineParamName = "N", 
			commandLineParamSynopsis = "-N <int>",
			displayOrder = 13)	
	public int getMinInstDocs() {
		return minInstDocs;
	}
	public void setMinInstDocs(int minInstDocs) {
		this.minInstDocs = minInstDocs;
	}





	@OptionMetadata(displayName = "partNumber",
			description = "The size of the partition for the tweet centroid model (-1 for not partionining). \t default: -1", 
			commandLineParamName = "A", 
			commandLineParamSynopsis = "-A <int>",
			displayOrder = 14)
	public int getPartNumber() {
		return partNumber;
	}
	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}


	/**
	 * Main method for testing this class.
	 *
	 * @param args should contain arguments to the filter: use -h for help
	 */	
	public static void main(String[] args) {
		runFilter(new PTCM(), args);
	}

}
