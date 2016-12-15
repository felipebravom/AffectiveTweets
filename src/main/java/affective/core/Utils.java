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
 *    Copyright (C) 1999-2016 University of Waikato, Hamilton, New Zealand
 *
 */


package affective.core;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cmu.arktweetnlp.Twokenize;


public class Utils {


	// tokenizes and cleans the content 
	static public List<String> tokenize(String content,boolean toLowerCase, boolean cleanTokens) {

		if(toLowerCase)
			content=content.toLowerCase();


		if(!cleanTokens)
			return Twokenize.tokenizeRawTweetText(content);
		else{
			// if a letters appears two or more times it is replaced by only two
			// occurrences of it
			content = content.replaceAll("([a-z])\\1+", "$1$1");
		}

		List<String> tokens = new ArrayList<String>();

		for (String word : Twokenize.tokenizeRawTweetText(content)) {
			String cleanWord = word;


			if(cleanTokens){
				// Replace URLs to a generic URL
				if (word.matches("http.*|ww\\..*")) {
					cleanWord = "http://www.url.com";
				}

				// Replaces user mentions to a generic user
				else if (word.matches("@.*")) {
					cleanWord = "@user";
				}


			}

			tokens.add(cleanWord);
		}
		return tokens;
	}


	
	/** Adds a negation prefix to the tokens that follow a negation word until the next punctuation mark.
	 * @param tokens the list of tokens to negate
	 * @param set the set with the negated words to use
	 * @return the negated tokens 
	 */  
	static public List<String> negateTokens(List<String> tokens,Set<String> set) {
		List<String> negTokens = new ArrayList<String>();

		// flag indicating negation state
		boolean inNegation=false;

		for(String token:tokens){

			// when we find a negation word for the first time
			if(set.contains(token) && !inNegation){
				inNegation=true;		
				negTokens.add(token);
				continue;
			}

			// if we are in a negation context with add a prefix
			if(inNegation){
				negTokens.add("NEGTOKEN-"+token);
				// the negation context ends whend finding a punctuation match
				if(token.matches("[\\.|,|:|;|!|\\?]+"))
					inNegation=false;
			}
			else{
				negTokens.add(token);
			}						
		}
		return negTokens;

	}



}









