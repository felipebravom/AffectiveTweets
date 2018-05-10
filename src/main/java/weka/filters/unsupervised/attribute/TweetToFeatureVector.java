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
 *    TweetToFeatureVector.java
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.unsupervised.attribute;


import java.util.Enumeration;

import weka.core.Capabilities;
import weka.core.Option;
import weka.core.OptionMetadata;
import weka.core.SingleIndex;
import weka.core.Capabilities.Capability;
import weka.core.stemmers.NullStemmer;
import weka.core.stemmers.Stemmer;
import weka.core.stopwords.Null;
import weka.core.stopwords.StopwordsHandler;
import weka.core.tokenizers.Tokenizer;
import weka.core.tokenizers.TweetNLPTokenizer;
import weka.filters.Filter;
import weka.filters.SimpleBatchFilter;

/**
 *  <!-- globalinfo-start --> An abstract Weka filter for calculating attributes from tweets.
 * <!-- globalinfo-end -->
 * 
 *  
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */


public abstract class TweetToFeatureVector extends SimpleBatchFilter {

	/** For serialization  */
	private static final long serialVersionUID = -3704559695773991498L;


	/** The index of the string attribute to be processed. */
	protected SingleIndex m_textIndex = new SingleIndex("1");


	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;


	/** True if url and users are standarized. */
	protected boolean standarizeUrlsUsers=false;


	/** True for standarizing repeated letters. */
	protected boolean reduceRepeatedLetters=false;


	/** The tokenizer. */
	protected Tokenizer m_tokenizer=new TweetNLPTokenizer();

	/** The stemming algorithm. */
	protected Stemmer m_stemmer = new NullStemmer();


	/** Stopword handler to use. */
	protected StopwordsHandler m_stopwordsHandler = new Null();




	/* (non-Javadoc)
	 * @see weka.filters.Filter#listOptions()
	 */
	@Override
	public Enumeration<Option> listOptions() {
		return Option.listOptionsForClassHierarchy(this.getClass(), Filter.class).elements();
	}


	/* (non-Javadoc)
	 * @see weka.filters.Filter#getOptions()
	 */
	@Override
	public String[] getOptions() {	
		return Option.getOptionsForHierarchy(this, Filter.class);
	}




	/* (non-Javadoc)
	 * @see weka.filters.Filter#setOptions(java.lang.String[])
	 */
	@Override
	public void setOptions(String[] options) throws Exception {
		Option.setOptionsForHierarchy(options, this, Filter.class);
	}


	/* (non-Javadoc)
	 * @see weka.filters.Filter#getCapabilities()
	 */
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





	/* To allow determineOutputFormat to access to entire dataset
	 * (non-Javadoc)
	 * @see weka.filters.SimpleBatchFilter#allowAccessToFullInputFormat()
	 */
	public boolean allowAccessToFullInputFormat() {
		return true;
	}




	@OptionMetadata(displayName = "textIndex",
			description = "The index (starting from 1) of the target string attribute. First and last are valid values. ",
			commandLineParamName = "I", commandLineParamSynopsis = "-I <col>",
			displayOrder = 0)
	public String getTextIndex() {
		return m_textIndex.getSingleIndex();
	}
	public void setTextIndex(String textIndex) {
		this.m_textIndex.setSingleIndex(textIndex);
	}




	@OptionMetadata(displayName = "lowercase",
			description = "Lowercase the tweet's content.", commandLineParamIsFlag = true,
			commandLineParamName = "U", commandLineParamSynopsis = "-U",
			displayOrder = 1)
	public boolean isToLowerCase() {
		return toLowerCase;
	}
	public void setToLowerCase(boolean toLowerCase) {
		this.toLowerCase = toLowerCase;
	}



	@OptionMetadata(displayName = "standarize URLs and @user mentions",
			description = "Reduce the attribute space by replacing user mentions and URLs with generic tokens.", 
			commandLineParamIsFlag = true, commandLineParamName = "stan", 
			commandLineParamSynopsis = "-stan",
			displayOrder = 2)	
	public boolean isStandarizeUrlsUsers() {
		return standarizeUrlsUsers;
	}
	public void setStandarizeUrlsUsers(boolean standarizeUrlsUsers) {
		this.standarizeUrlsUsers = standarizeUrlsUsers;
	}


	@OptionMetadata(displayName = "reduceRepeatedLetters",
			description = "Reduce the attribute space by replacing sequences of letters occurring more than two "
					+ "times in a row with two occurrences of them (e.g., huuungry is reduced to huungry, loooove to loove)", 
					commandLineParamIsFlag = true, commandLineParamName = "red", 
					commandLineParamSynopsis = "-red",
					displayOrder = 2)		
	public boolean isReduceRepeatedLetters() {
		return reduceRepeatedLetters;
	}
	public void setReduceRepeatedLetters(boolean reduceRepeatedLetters) {
		this.reduceRepeatedLetters = reduceRepeatedLetters;
	}



	@OptionMetadata(displayName = "tokenizer",
			description = "The tokenizing algorithm to use on the tweets. Uses the CMU TweetNLP tokenizer as default",
			commandLineParamName = "tokenizer",
			commandLineParamSynopsis = "-tokenizer <string>", displayOrder = 3)		
	public Tokenizer getTokenizer() {
		return m_tokenizer;
	}
	public void setTokenizer(Tokenizer m_tokenizer) {
		this.m_tokenizer = m_tokenizer;
	}




	@OptionMetadata(displayName = "stemmer",
			description = "The stemming algorithm to use on the words. Default: no stemming.",
			commandLineParamName = "stemmer",
			commandLineParamSynopsis = "-stemmer <string>", displayOrder = 4)	
	public Stemmer getStemmer() {
		return m_stemmer;
	}
	public void setStemmer(Stemmer m_stemmer) {
		this.m_stemmer = m_stemmer;
	}



	@OptionMetadata(displayName = "stopwordsHandler",
			description = "The stopwords handler to use (Null means no stopwords are used).",
			commandLineParamName = "stopwords-handler",
			commandLineParamSynopsis = "-stopwords-handler <string>", displayOrder = 5)
	public StopwordsHandler getStopwordsHandler() {
		return m_stopwordsHandler;
	}
	public void setStopwordsHandler(StopwordsHandler m_stopwordsHandler) {
		this.m_stopwordsHandler = m_stopwordsHandler;
	}







}
