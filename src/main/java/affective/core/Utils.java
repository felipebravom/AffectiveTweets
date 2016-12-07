package affective.core;

import java.util.ArrayList;
import java.util.List;
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




	





}
