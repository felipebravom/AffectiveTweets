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
 *    Copyright (C) 1999-2017 University of Waikato, Hamilton, New Zealand
 *
 */


package weka.filters.unsupervised.attribute;


import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

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

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.Utils;
import weka.core.WekaPackageManager;
import weka.core.Capabilities.Capability;
import weka.core.TechnicalInformation.Type;
import weka.filters.SimpleBatchFilter;



/**
 *  <!-- globalinfo-start --> Given a corpus of documents creates Vector Space model for each word using bag-of-words and cluster-based attributes.
 *   
 * <!-- globalinfo-end -->
 * 
 *  
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class TweetCentroid extends SimpleBatchFilter {


	/** For serialization.    **/
	private static final long serialVersionUID = 7553647795494402690L;

	
	/** Default path to where resources are stored. */
	public static String RESOURCES_FOLDER_NAME = weka.core.WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "resources";

	/** Default path to where lexicons are stored */
	public static String LEXICON_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "lexicons";

	

	/** the vocabulary and the WordRep */
	protected Object2ObjectMap<String, WordRep> wordInfo; 

	/** Counts the number of documents in which candidate attributes appears */
	protected Object2IntMap<String> attributeCount;


	/** Contains a mapping of valid attribute with their indexes. */
	protected Object2IntMap<String> m_Dictionary;

	/** Brown Clusters Dictionary */
	protected Object2ObjectMap<String,String> brownDict;

	/** the minimum number of documents for an attribute to be considered. */
	protected int minAttDocs=0; 


	/** the minimum number of documents for a word to be included. */
	protected int minInstDocs=0; 


	/** the index of the string attribute to be processed */
	protected int textIndex=1; 

	/** the prefix of the word attributes */
	protected String wordPrefix="WORD-";

	/** the prefix of the cluster-based attributes */
	protected String clustPrefix="CLUST-";


	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;


	/** True for calculating word-based attributes . */
	protected boolean createWordAtts=true;


	/** True for calculating cluster-based attributes . */
	protected boolean createClustAtts=true;


	/** True if the word name is included as an attribute */
	protected boolean reportWord=true;


	/** True if url, users, and repeated letters are cleaned */
	protected boolean cleanTokens=false;


	/** The path of the word clusters. */
	protected String clustPath=RESOURCES_FOLDER_NAME+File.separator+"50mpaths2.txt.gz";


	public String minAttDocsTipText() {
		return "The minimum number of documents for an attribute to be considered.";
	}
	
	public String minInstDocsTipText() {
		return "The minimum number of documents for a word to be included. ";
	}
	
	public String textIndexTipText() {
		return "The index of the string attribute to be processed.";
	}

	public String wordPrefixTipText() {
		return "The prefix of the word attributes.";
	}
	
	public String clustPrefixTipText() {
		return "The prefix of the cluster-based attributes.";
	}
	
	public String toLowerCaseTipText() {
		return "True if all tokens should be downcased.";
	}
	
	public String createWordAttsTipText() {
		return "True for calculating word-based attributes.";
	}

	public String createClustAttsTipText() {
		return "True for calculating cluster-based attributes.";
	}
			

	public String reportWordTipText() {
		return "True if the word name is included as an attribute.";
	}
	
	
	public String cleanTokensTipText() {
		return "True if url, users, and repeated letters are cleaned.";
	}
	
	public String lexPathTipText() {
		return "The path of the seed lexicon.";
	}
	
	public String clustPathTipText() {
		return "The path of the word clusters.";
	}
	
	
	// This class contains all the information of the word to compute the centroid
	class WordRep{
		String word; // the word
		int numDoc; // number of documents where the word occurs
		Object2IntMap<String> wordSpace; // the vector space model of the word



		public WordRep(String word){
			this.word=word;
			this.numDoc=0;
			this.wordSpace=new Object2IntOpenHashMap<String>();
		}

		public void addDoc(Object2IntMap<String> docVector){
			this.numDoc++;
			for(String vecWord:docVector.keySet()){
				int vecWordFreq=docVector.getInt(vecWord);
				// if the word was seen before we add the current frequency
				this.wordSpace.put(vecWord,vecWordFreq+this.wordSpace.getInt(vecWord));
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

	@Override
	public String globalInfo() {
		return "A batch filter that creates word vectors from tweets using the Tweet Centroid Model."
				+ "Each word is calculated as the average vector of the tweets in which it occurs."				
				+ "\n"+getTechnicalInformation().toString();
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

		result.addElement(new Option("\t Minimum count of an instance.\n"
				+ "\t(default: " + this.minInstDocs + ")", "N", 1, "-N"));

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


		result.addElement(new Option("\t Include the word name as a string attribute.\n"
				+ "\t(default: " + this.reportWord + ")", "R", 0, "-R"));

		result.addElement(new Option("\t  Clean tokens (replace 3 or more repetitions of a letter to 2 repetitions of it e.g, gooood to good, standarise URLs and @users).\n"
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

		result.add("-N");
		result.add("" + this.getMinInstDocs());

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

		if(this.isReportWord())
			result.add("-R");


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

		String textMinInstDocsOption = Utils.getOption('N', options);
		if (textMinInstDocsOption.length() > 0) {
			String[] textMinInstDocsOptionSpec = Utils.splitOptions(textMinInstDocsOption);
			if (textMinInstDocsOptionSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid index");
			}
			int minDocIns = Integer.parseInt(textMinInstDocsOptionSpec[0]);
			this.setMinInstDocs(minDocIns);

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


		this.reportWord=Utils.getFlag('R', options);


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

		return docVec;

	}






	/* Calculates the vocabulary and the word vectors from an Instances object
	 * The vocabulary is only extracted the first time the filter is run.
	 * 
	 */	 
	public void computeWordVecsAndVoc(Instances inputFormat) {


		if (!this.isFirstBatchDone()){


			this.wordInfo = new Object2ObjectOpenHashMap<String, WordRep>();

			this.attributeCount= new Object2IntOpenHashMap<String>(); 



			// the Dictionary of the brown Clusters
			if(this.createClustAtts){
				this.brownDict=new Object2ObjectOpenHashMap<String,String>();
				try {
					FileInputStream fin = new FileInputStream(this.clustPath);
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

	@Override
	protected Instances determineOutputFormat(Instances inputFormat) {



		// calculates the word frequency vectors and the vocabulary
		this.computeWordVecsAndVoc(inputFormat);


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


		// we add the word name as an attribute
		if(this.reportWord)
			att.add(new Attribute("WORD_NAME", (ArrayList<String>) null));


		Instances result = new Instances(inputFormat.relationName(), att, 0);

		return result;
	}

	@Override
	protected Instances process(Instances instances) throws Exception {



		Instances result = getOutputFormat();

		for(String word:this.wordInfo.keySet()){
			// get the word vector
			WordRep wordRep=this.wordInfo.get(word);

			// We just consider valid words
			if(wordRep.numDoc>=this.minInstDocs){
				double[] values = new double[result.numAttributes()];


				for(String innerWord:wordRep.wordSpace.keySet()){
					// only include valid words
					if(this.m_Dictionary.containsKey(innerWord)){
						int attIndex=this.m_Dictionary.getInt(innerWord);
						// we normalise the value by the number of documents
						values[attIndex]=((double)wordRep.wordSpace.getInt(innerWord))/wordRep.numDoc;					
					}
				}


				if(this.reportWord){
					int wordNameIndex=result.attribute("WORD_NAME").index();
					values[wordNameIndex]=result.attribute(wordNameIndex).addStringValue(word);					
				}


				Instance inst=new SparseInstance(1, values);

				inst.setDataset(result);

				result.add(inst);

			}


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

	public int getMinInstDocs() {
		return minInstDocs;
	}



	public void setMinInstDocs(int minInstDocs) {
		this.minInstDocs = minInstDocs;
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



	public boolean isReportWord() {
		return reportWord;
	}



	public void setReportWord(boolean reportWord) {
		this.reportWord = reportWord;
	}




	public boolean isCleanTokens() {
		return cleanTokens;
	}



	public void setCleanTokens(boolean cleanTokens) {
		this.cleanTokens = cleanTokens;
	}



	public static void main(String[] args) {
		runFilter(new TweetCentroid(), args);
	}

}
