package weka.filters.unsupervised.attribute;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Twokenize;
import cmu.arktweetnlp.impl.ModelSentence;
import cmu.arktweetnlp.impl.Sentence;
import affective.core.LexiconEvaluator;
import affective.core.MyUtils;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.DenseInstance;
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

	/** the vocabulary and the number of documents where the word appears */
	protected Map<String, Integer> vocDocFreq; 

	/** List of word vectors with their corresponding frequencies per tweet */
	protected List<Map<String, Integer>> wordVecs; 

	/** the index of the string attribute to be processed */
	protected int textIndex=1; 

	/** the index of the string attribute to be processed */
	protected String prefix="";

	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;

	/** True if instances should be sparse */
	protected boolean sparseInstances=true;


	/** True if a part-of-speech prefix should be included to each word */
	protected boolean posPrefix=false;


	/** True if a Sentiment prefix calculatef from a Lexicon should be included to each word */
	protected boolean sentPrefix=false;


	/** TwitterNLP Tagger model */
	protected Tagger tagger;
	
	
	/** LexiconEvaluator for sentiment prefixes */
	protected LexiconEvaluator lex;



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

		result.addElement(new Option("\t Index of string attribute.\n"
				+ "\t(default: " + this.textIndex + ")", "I", 1, "-I"));		

		result.addElement(new Option("\t Prefix of attributes.\n"
				+ "\t(default: " + this.prefix + ")", "P", 1, "-P"));

		result.addElement(new Option("\t Lowercase content.\n"
				+ "\t(default: " + this.toLowerCase + ")", "L", 0, "-L"));

		result.addElement(new Option("\t Sparse instances.\n"
				+ "\t(default: " + this.sparseInstances + ")", "S", 0, "-S"));

		result.addElement(new Option("\t POS prefix.\n"
				+ "\t(default: " + this.posPrefix + ")", "K", 0, "-K"));


		result.addElement(new Option("\t Sent prefix.\n"
				+ "\t(default: " + this.sentPrefix + ")", "H", 0, "-H"));


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

		result.add("-I");
		result.add("" + this.getTextIndex());

		result.add("-P");
		result.add("" + this.getPrefix());

		if(this.toLowerCase)
			result.add("-L");

		if(this.sparseInstances)
			result.add("-S");

		if(this.posPrefix)
			result.add("-K");

		if(this.sentPrefix)
			result.add("-H");


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
			this.setPrefix(pref);

		}

		this.toLowerCase=Utils.getFlag('L', options);

		this.sparseInstances=Utils.getFlag('S', options);

		this.posPrefix=Utils.getFlag('K', options);
		
		this.sentPrefix=Utils.getFlag('H', options);

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

	/* Calculates the vocabulary and the word vectors from an Instances object
	 * The vocabulary is only extracted the first time the filter is run.
	 * 
	 */	 
	public void computeWordVecsAndVoc(Instances inputFormat) {

		
		
		
		
		// The vocabulary is created only in the first execution
		if (!this.isFirstBatchDone()){
			this.vocDocFreq = new TreeMap<String, Integer>();
			
			// process the Tagger
			if(this.posPrefix){
				try {
					this.tagger= new Tagger();
					this.tagger.loadModel("models/model.20120919");
				} catch (IOException e) {
					this.posPrefix=false;
					System.err.println("Warning: TwitterNLP model couldn't be read.");
				}	

			}
			
			// process the LexiconEvaluator
			if(this.sentPrefix){	
				try {
					this.lex= new LexiconEvaluator("lexicons/AFINN-111.txt");
					this.lex.processDict();
				} catch (IOException e) {
					this.sentPrefix=false;
					System.err.println("Warning: Lexicon couldn't be read.");
				}
			}

		}

		this.wordVecs = new ArrayList<Map<String, Integer>>();

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
			List<String> posTokens = null;
			List<String> sentTokens = null;
			
			
			if(this.posPrefix){
				try{
				posTokens=MyUtils.getPOStags(tokens, tagger);
				}
				catch(Exception E){
					
				}
			}

			
			if(this.sentPrefix){
				sentTokens=new ArrayList<String>();
				for(String token:tokens){
					String sentToken="";
					if(this.lex.getDict().containsKey(token)){
						Double value=Double.parseDouble(this.lex.getDict().get(token));
						sentToken +=  (value>0)?"POSITIVE-":"NEGATIVE";
						sentToken += "("+this.lex.getDict().get(token)+")-";						
					}
						
					sentTokens.add(sentToken);
				}
				
				
			}
			
			if(this.posPrefix){
				for(int i=0; i<tokens.size();i++){
					tokens.set(i, posTokens.get(i)+"-"+tokens.get(i));
				}
			}
			
			if(this.sentPrefix){
				for(int i=0; i<tokens.size();i++){
					tokens.set(i, sentTokens.get(i)+tokens.get(i));
				}
			}





			Map<String, Integer> wordFreqs = MyUtils.calculateTermFreq(tokens);

			// Add the frequencies of the different words
			this.wordVecs.add(wordFreqs);

			// The vocabulary is calculated only the first time we run the
			// filter
			if (!this.isFirstBatchDone()) {

				// if the word is new we add it to the vocabulary, otherwise we
				// increment the document count
				for (String word : wordFreqs.keySet()) {

					if (this.vocDocFreq.containsKey(word)) {
						this.vocDocFreq
						.put(word, this.vocDocFreq.get(word) + 1);
					} else
						this.vocDocFreq.put(word, 1);

				}

			}

		}

	}

	@Override
	protected Instances determineOutputFormat(Instances inputFormat) {

		ArrayList<Attribute> att = new ArrayList<Attribute>();

		// Adds all attributes of the inputformat
		for (int i = 0; i < inputFormat.numAttributes(); i++) {
			att.add(inputFormat.attribute(i));
		}

		// calculates the word frequency vectors and the vocabulary
		this.computeWordVecsAndVoc(inputFormat);

		for (String word : this.vocDocFreq.keySet()) {

			Attribute a = new Attribute(this.prefix + word);

			att.add(a); // adds an attribute for each word using a prefix

			// pw.println("word: " + word + " bytes: "
			// + Arrays.toString(word.getBytes()) + " attribute name: "
			// + a.name() + " HashValue:" + this.vocDocFreq.get(word));

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
			this.computeWordVecsAndVoc(instances);
		}

		// System.out.println("++++" + instances);

		int i = 0;
		for (Map<String, Integer> wordVec : this.wordVecs) {
			double[] values = new double[result.numAttributes()];

			// copy previous attributes values
			for (int n = 0; n < instances.numAttributes(); n++)
				values[n] = instances.instance(i).value(n);

			// add words using the frequency as attribute value
			for (String word : wordVec.keySet()) {
				// we only add the value if the word was previously included
				// into the vocabulary, otherwise we discard it
				if (result.attribute(this.prefix + word) != null)
					values[result.attribute(this.prefix + word).index()] = wordVec
					.get(word);

			}


			Instance inst;
			if(this.sparseInstances)
				inst=new SparseInstance(1, values);
			else
				inst=new DenseInstance(1, values);


			inst.setDataset(result);
			// copy possible strings, relational values...
			copyValues(inst, false, instances, result);

			result.add(inst);
			i++;

		}

		return result;
	}


	/** Adds the corresponding pos tags prefix to each token **/
	protected List<String> addPOSprefix(List<String> tokens) {

		Sentence sentence = new Sentence();
		sentence.tokens = tokens;
		ModelSentence ms = new ModelSentence(sentence.T());
		this.tagger.featureExtractor.computeFeatures(sentence, ms);
		this.tagger.model.greedyDecode(ms, false);

		ArrayList<String> tags = new ArrayList<String>();

		for (int t = 0; t < sentence.T(); t++) {
			String tag = this.tagger.model.labelVocab.name(ms.labels[t]);
			tags.add(tag+"-"+tokens.get(t));
		}

		return tags;
	}


	public int getTextIndex() {
		return textIndex;
	}


	public void setTextIndex(int textIndex) {
		this.textIndex = textIndex;
	}


	public String getPrefix() {
		return prefix;
	}


	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}


	public boolean isToLowerCase() {
		return toLowerCase;
	}

	public void setToLowerCase(boolean toLowerCase) {
		this.toLowerCase = toLowerCase;
	}



	public boolean isSparseInstances() {
		return sparseInstances;
	}


	public void setSparseInstances(boolean sparseInstances) {
		this.sparseInstances = sparseInstances;
	}



	public boolean isPosPrefix() {
		return posPrefix;
	}



	public void setPosPrefix(boolean posPrefix) {
		this.posPrefix = posPrefix;
	}

	public boolean isSentPrefix() {
		return sentPrefix;
	}



	public void setSentPrefix(boolean sentPrefix) {
		this.sentPrefix = sentPrefix;
	}




	public static void main(String[] args) {
		runFilter(new TwitterNlpWordToVector(), args);
	}

}
