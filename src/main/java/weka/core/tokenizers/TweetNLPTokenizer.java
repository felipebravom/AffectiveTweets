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
 *    TwitterNLPTokenizer.java
 *    Copyright (C) 1999-2016 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core.tokenizers;


import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Type;
import weka.core.Utils;



/**
 *  <!-- globalinfo-start --> A Twitter-specific tokenizer based on the CMU TwitterNLP library: http://www.cs.cmu.edu/~ark/TweetNLP/
 * <!-- globalinfo-end -->
 * 
 * <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;InProceedings{twitterNLP,
 * 	Title                    = {Part-of-speech tagging for twitter: Annotation, features, and experiments},
 * 	Author                   = {Gimpel, Kevin and Schneider, Nathan and O'Connor, Brendan and Das, Dipanjan and Mills, Daniel and Eisenstein, Jacob and Heilman, Michael and Yogatama, Dani and Flanigan, Jeffrey and Smith, Noah A},
 * 	Booktitle                = {Proceedings of the 49th Annual Meeting of the Association for Computational Linguistics: Human Language Technologies: short papers-Volume 2},
 * 	Year                     = {2011},
 * 	Organization             = {Association for Computational Linguistics},
 * 	Pages                    = {42--47}
 *	}
 * </pre>
 <!-- technical-bibtex-end -->
 * 
 * 
 * @author Felipe Bravo-Marquez (fjb11@students.waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class TweetNLPTokenizer extends Tokenizer {

	/** For serialization.    **/
	private static final long serialVersionUID = 4352757127093531518L;


	/** the actual tokenizer */
	protected transient Iterator<String> m_tokenIterator;

	/** True if all tokens should be downcased. */
	protected boolean toLowerCase=true;

	/** True if url, users, and repeated letters are cleaned. */
	protected boolean cleanTokens=true;



	/**
	 * Returns a string describing this tokenizer.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	@Override
	public String globalInfo() {
		return "A Twitter-specific tokenizer based on the CMU TweetNLP library.\n" + getTechnicalInformation().toString();				
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
        result.setValue(TechnicalInformation.Field.AUTHOR, "Gimpel, Kevin and Schneider, Nathan and O'Connor, Brendan and Das, Dipanjan and Mills, Daniel and Eisenstein, Jacob and Heilman, Michael and Yogatama, Dani and Flanigan, Jeffrey and Smith, Noah A");
        result.setValue(TechnicalInformation.Field.TITLE, "Part-of-speech tagging for twitter: Annotation, features, and experiments");
        result.setValue(TechnicalInformation.Field.YEAR, "2011");
        result.setValue(TechnicalInformation.Field.URL, "http://www.cs.cmu.edu/~ark/TweetNLP/");
        result.setValue(TechnicalInformation.Field.NOTE, "The Weka tokenizer works with version 0.32 of TweetNLP.");

        return result;
}
	
	
//	
//	/**
//	 * Returns the revision string.
//	 * 
//	 * @return the revision
//	 */
//	@Override
//	public String getRevision() {
//		return RevisionUtils.extract("$Revision: 1 $");
//	}



	/**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 */
	@Override
	public Enumeration<Option> listOptions() {
		Vector<Option> result = new Vector<Option>();

		result.addElement(new Option("\t Lowercase content.\n"
				+ "\t(default: " + this.toLowerCase + ")", "L", 0, "-L"));

		result.addElement(new Option("\t Normalize tokens (replace goood by good, normalize URLs and @users).\n"
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

		if(this.toLowerCase)
			result.add("-L");

		if(this.cleanTokens)
			result.add("-O");

		Collections.addAll(result, super.getOptions());

		return result.toArray(new String[result.size()]);
	}


	/**
	 * Parses the options for this object.
	 * 
	 * 
	 * @param options
	 *            the options to use
	 * @throws Exception
	 *             if setting of options fails
	 */
	@Override
	public void setOptions(String[] options) throws Exception {


		this.toLowerCase=Utils.getFlag('L', options);

		this.cleanTokens=Utils.getFlag('O', options);


		super.setOptions(options);

		Utils.checkForRemainingOptions(options);


	}



	/**
	 * Tests if this enumeration contains more elements.
	 * 
	 * @return true if and only if this enumeration object contains at least one
	 *         more element to provide; false otherwise.
	 */
	public boolean hasMoreElements() {
		return this.m_tokenIterator.hasNext();	
	}

	/**
	 * Returns the next element of this enumeration if this enumeration object has
	 * at least one more element to provide.
	 * 
	 * @return the next element of this enumeration.
	 */
	@Override
	public String nextElement() {
		return this.m_tokenIterator.next();	
	}

	/**
	 * Sets the string to tokenize. Tokenization happens immediately.
	 * 
	 * @param s the string to tokenize
	 */
	@Override
	public void tokenize(String s) {

		List<String> words=affective.core.Utils.tokenize(s, this.toLowerCase, this.cleanTokens);
		this.m_tokenIterator=words.iterator();	


	}

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


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String lowerCaseTipText() {
		return "Lowercase the tweet's content.";
	}


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


	/**
	 * Returns the tip text for this property.
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String cleanTokensTipText() {
		return "Reduce the attribute space by replacing sequences of letters occurring more than two "
				+ "times in a row with two occurrences of them (e.g., huuungry is reduced to huungry, loooove to loove), "
				+ "and replacing 	user mentions and URLs with generic tokens..";		
	}


	
	  /**
	   * Returns the revision string.
	   * 
	   * @return the revision
	   */
	  public String getRevision() {
	    return RevisionUtils.extract("$Revision: 1 $");
	  }
	
	
	/**
	 * Runs the tokenizer with the given options and strings to tokenize. The
	 * tokens are printed to stdout.
	 * 
	 * @param args the commandline options and strings to tokenize
	 */
	public static void main(String[] args) {
		runTokenizer(new TweetNLPTokenizer(), args);
	}

}
