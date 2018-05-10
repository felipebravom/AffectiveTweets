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
 *    ASA.java
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute;


import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
import java.util.Random;
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
 *  
 *  A batch filter that creates polarity labeled instances from unlabeled tweets and a seed polarity lexicon using the Annotate-Sample-Average Algorithm.
 *  Annotate-Sample-Average (ASA) is a lexical-based distant supervision method for training polarity classifiers in Twitter in the absence of labeled data.
 *  ASA takes a collection of unlabeled tweets and a polarity lexicon composed of positive and negative words and creates synthetic labeled instances. 
 *  Each labeled instance is created by sampling with replacement a number of tweets containing at least one word from the lexicon with the desired polarity, and averaging the feature vectors of the sampled tweets.
 *  
 * <!-- globalinfo-end -->
 * 
 *  
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class ASA  extends DistantSupervisionSyntheticFilter {



	/** For serialization.    **/
	private static final long serialVersionUID = 4853514702099557689L;



	/** List of tweets having at least one positive word and no negative words. */
	protected ObjectList<Object2IntMap<String>> posTweets;

	/** List of tweets having at least one negative word and no positive words. */
	protected ObjectList<Object2IntMap<String>> negTweets;


	/** The number of tweets sampled in each centroid. */
	protected int tweetsPerCentroid=10;	

	/** The number of positive instances to generate. */
	protected int numPosInstances=1000;


	/** The number of negative instances to generate. */
	protected int numNegInstances=1000;


	/** True for discarding tweets containing both positive and negative words during the annotation phase. */
	protected boolean exclusiveSets=false;





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





	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#globalInfo()
	 */
	@Override
	public String globalInfo() {
		return "Annotate-Sample-Average (ASA) is a lexical-based distant supervision method for training polarity classifiers in Twitter in the absence of labelled data. " +
				"ASA takes a collection of unlabelled tweets and a polarity lexicon composed of positive and negative words and creates synthetic labelled instances. " +
				"Each labelled instance is created by sampling with replacement a number of tweets containing at least one word from the lexicon with the desired polarity, and averaging the feature vectors of the sampled tweets." +
				"\n Use this filter with the FilteredClassifier. \n"+getTechnicalInformation().toString();
	}






	/**
	 * Annotates tweets according using the polarity lexicon
	 * @param inputFormat the input Instances
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

	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#determineOutputFormat(weka.core.Instances)
	 */
	@Override
	protected Instances determineOutputFormat(Instances inputFormat) {


		// set upper value for text index
		m_textIndex.setUpper(inputFormat.numAttributes() - 1);


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




	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#process(weka.core.Instances)
	 */
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

		// Second batch maps tweets into the original feature space
		else{
			result=this.mapTargetInstance(instances);

		}



		return result;

	}




	@OptionMetadata(displayName = "tweetsPerCentroid",
			description = "The number of tweets to average in each generated instance. \t default: 10", 
			commandLineParamName = "A", 
			commandLineParamSynopsis = "-A <int>",
			displayOrder = 13)
	public int getTweetsPerCentroid() {
		return tweetsPerCentroid;
	}
	public void setTweetsPerCentroid(int tweetsPerCentroid) {
		this.tweetsPerCentroid = tweetsPerCentroid;
	}




	@OptionMetadata(displayName = "numPosInstances",
			description = "The number of positive instances to generate. \t default: 1000", 
			commandLineParamName = "npos", 
			commandLineParamSynopsis = "-npos <int>",
			displayOrder = 14)	
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
			displayOrder = 15)
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
					displayOrder = 16)	
	public boolean isExclusiveSets() {
		return exclusiveSets;
	}
	public void setExclusiveSets(boolean exclusiveSets) {
		this.exclusiveSets = exclusiveSets;
	}




	/**
	 * Main method for testing this class.
	 *
	 * @param args should contain arguments to the filter: use -h for help
	 */	
	public static void main(String[] args) {
		runFilter(new ASA(), args);
	}

}
