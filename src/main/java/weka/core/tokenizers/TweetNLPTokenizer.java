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
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core.tokenizers;


import java.util.Iterator;
import java.util.List;

import cmu.arktweetnlp.Twokenize;

import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformation.Type;




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
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class TweetNLPTokenizer extends Tokenizer {

	/** For serialization.    **/
	private static final long serialVersionUID = 4352757127093531518L;


	/** the actual tokenizer */
	protected transient Iterator<String> m_tokenIterator;

	

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

		List<String> words=Twokenize.tokenizeRawTweetText(s);
		this.m_tokenIterator=words.iterator();	


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
