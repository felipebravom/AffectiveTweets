package weka.filters.unsupervised.attribute;


import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

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

public class TwitterNlpWordToVector extends SimpleBatchFilter {

	/**  Converts one String attribute into a set of attributes
	 * representing word occurrence based on the TwitterNLP tokenizer.
	 * 
	 */


	/** for serialization */
	private static final long serialVersionUID = 3635946466523698211L;

	/** Counts the number of documents in which candidate attributes appear. This will help for discarding infrequent attributes */
	protected Object2IntMap<String> attributeCount; 

	/** List of tweets to process with their feature vectors*/
	protected ObjectList<Object2IntMap<String>> procTweets; 

	/** Contains a mapping of valid attribute with their indexes. */
	protected Object2IntMap<String> m_Dictionary;

	/** Brown Clusters Dictionary */
	protected Object2ObjectMap<String,String> brownDict;

	/** the minimum number of tweets for an attribute to be considered. */
	protected int minAttDocs=0; 

	/** the index of the string attribute to be processed */
	protected int textIndex=1; 

	/** the index of the string attribute to be processed */
	protected String ngramPrefix="NGRAM-";

	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;


	/** TwitterNLP Tagger model */
	protected Tagger tagger;



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

		result.addElement(new Option("\t Prefix of ngram features.\n"
				+ "\t(default: " + this.ngramPrefix + ")", "P", 1, "-P"));

		result.addElement(new Option("\t Lowercase content.\n"
				+ "\t(default: " + this.toLowerCase + ")", "L", 0, "-L"));


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
		result.add("" + this.getNgramPrefix());

		if(this.toLowerCase)
			result.add("-L");


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

		String prefixOption = Utils.getOption('P', options);
		if (prefixOption.length() > 0) {
			String[] prefixSpec = Utils.splitOptions(prefixOption);
			if (prefixSpec.length == 0) {
				throw new IllegalArgumentException(
						"Invalid prefix");
			}
			String pref = prefixSpec[0];
			this.setNgramPrefix(pref);

		}

		this.toLowerCase=Utils.getFlag('L', options);

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

	/* Processes a batch of tweets. Tweets are mapped into feature vectors.
	 * The feature space is only determined the first time the filter is run.
	 * 
	 */	 
	public void tweetsToVectors(Instances inputFormat) {


		// The vocabulary is created only in the first execution
		if (!this.isFirstBatchDone()){
			this.attributeCount = new Object2IntOpenHashMap<String>();

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

			// tokenises the content 
			List<String> tokens=Twokenize.tokenizeRawTweetText(content);; 

			Object2IntMap<String> docVec=calculateTermFreq(tokens,"NGRAMS-");

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



	public int getTextIndex() {
		return textIndex;
	}


	public void setTextIndex(int textIndex) {
		this.textIndex = textIndex;
	}


	public String getNgramPrefix() {
		return ngramPrefix;
	}


	public void setNgramPrefix(String prefix) {
		this.ngramPrefix = prefix;
	}


	public int getMinAttDocs() {
		return minAttDocs;
	}



	public void setMinAttDocs(int minAttDocs) {
		this.minAttDocs = minAttDocs;
	}



	public boolean isToLowerCase() {
		return toLowerCase;
	}

	public void setToLowerCase(boolean toLowerCase) {
		this.toLowerCase = toLowerCase;
	}


	public static void main(String[] args) {
		runFilter(new TwitterNlpWordToVector(), args);
	}

}
