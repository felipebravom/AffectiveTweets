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
 *    Utils.java
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */

package affective.core;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.stemmers.Stemmer;
import weka.core.stopwords.StopwordsHandler;
import weka.core.tokenizers.Tokenizer;


/**
 * <!-- globalinfo-start --> Provides static functions for String processing.
 * <!-- globalinfo-end -->
 * 
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 2 $
 */

public class Utils {

	

	/**
	 * Adds a negation prefix to the tokens that follow a negation word until
	 * the next punctuation mark.
	 * 
	 * @param tokens
	 *            the list of tokens to negate
	 * @param set
	 *            the set with the negated words to use
	 * @return the negated tokens
	 */
	static public List<String> negateTokens(List<String> tokens, Set<String> set) {
		List<String> negTokens = new ArrayList<String>();

		// flag indicating negation state
		boolean inNegation = false;

		for (String token : tokens) {

			// when we find a negation word for the first time
			if (set.contains(token) && !inNegation) {
				inNegation = true;
				negTokens.add(token);
				continue;
			}

			// if we are in a negation context with add a prefix
			if (inNegation) {
				negTokens.add("NEGTOKEN-" + token);
				// the negation context ends whend finding a punctuation match
				if (token.matches("[\\.|,|:|;|!|\\?]+"))
					inNegation = false;
			} else {
				negTokens.add(token);
			}
		}
		return negTokens;

	}







	/**
	 * Calculates a sequence of word-clusters from a list of tokens and a dictionary.
	 * 
	 * @param tokens the input tokens 
	 * @param dict the dictionary with the word clusters
	 * @return a list of word-clusters
	 */	
	public static List<String> clustList(List<String> tokens, Map<String,String> dict){
		List<String> clusters=new ArrayList<String>();
		for(String token:tokens){
			if(dict.containsKey(token)){
				clusters.add(dict.get(token));
			}

		}	
		return clusters;
	}


	/**
	 * Calculates a vector of attributes from a list of tokens
	 * 
	 * @param tokens the input tokens 
	 * @param prefix the prefix of each vector attribute
	 * @param freqWeights true for considering term-frequency weights (booleans weights are used otherwise)
	 * @return an Object2IntMap object mapping the attributes to their values
	 */		
	public static Object2IntMap<String> calculateTermFreq(List<String> tokens, String prefix, boolean freqWeights) {
		Object2IntMap<String> termFreq = new Object2IntOpenHashMap<String>();

		// Traverse the strings and increments the counter when the token was
		// already seen before
		for (String token : tokens) {
			// add frequency weights if the flat is set
			if(freqWeights)
				termFreq.put(prefix+token, termFreq.getInt(prefix+token) + 1);
			// otherwise, just consider boolean weights
			else{
				if(!termFreq.containsKey(token))
					termFreq.put(prefix+token, 1);
			}
		}

		return termFreq;
	}


	/**
	 * Calculates token n-grams from a sequence of tokens.
	 * 
	 * @param tokens the input tokens from which the word n-grams will be calculated
	 * @param n the size of the word n-gram
	 * @return a list with the word n-grams
	 */
	public static List<String> calculateTokenNgram(List<String> tokens,int n){
		List<String> tokenNgram=new ArrayList<String>();
		if(tokens.size()>=n){			
			for(int i=0;i<=tokens.size()-n;i++){
				String ngram="";
				for(int j=i;j<i+n;j++){
					ngram+=tokens.get(j);
					if(j<i+n-1)
						ngram+="-";
				}				
				tokenNgram.add(ngram);
			}
		}
		return tokenNgram;		
	}


	/**
	 * Calculates character n-grams from a String.
	 * 
	 * @param content the input String
	 * @param n the size of the character n-gram
	 * @return a list with the character n-grams
	 */
	public static List<String> extractCharNgram(String content,int n){
		List<String> charNgram=new ArrayList<String>();
		if(content.length()>=n){
			for(int i=0;i<content.length()-n;i++){
				String cgram="";
				for(int j=i;j<i+n;j++){
					cgram+=content.charAt(j);
				}				
				charNgram.add(cgram);

			}
		}

		return charNgram;		
	}



	/**
	 * Tokenizes a String
	 * @param content the content
	 * @param toLowerCase true for lowercasing the content
	 * @param standarizeUrlsUsers true for standarizing urls and users
	 * @param reduceRepeatedLetters true for reduing repeated letters
	 * @param tokenizer the tokenizer
	 * @param stemmer the stemmer
	 * @param stop the stopwords handler
	 * @return a list of tokens
	 */
	static public List<String> tokenize(String content, boolean toLowerCase, boolean standarizeUrlsUsers, boolean reduceRepeatedLetters, Tokenizer tokenizer, Stemmer stemmer, StopwordsHandler stop) {

		if (toLowerCase)
			content = content.toLowerCase();

		// if a letters appears two or more times it is replaced by only two
		// occurrences of it
		if (reduceRepeatedLetters)
			content = content.replaceAll("([a-z])\\1+", "$1$1");


		List<String> tokens = new ArrayList<String>();

		tokenizer.tokenize(content);
		for(;tokenizer.hasMoreElements();){
			String token=tokenizer.nextElement();
			if(!stop.isStopword(token)){

				if (standarizeUrlsUsers) {
					// Replace URLs to a generic URL
					if (token.matches("http.*|ww\\..*|www\\..*")) {
						token="http://www.url.com";
					}
					// Replaces user mentions to a generic user
					else if (token.matches("@.*")) {
						token="@user";
					}

				}				

				tokens.add(stemmer.stem(token));
			}
		}

		return tokens;

	}



}
