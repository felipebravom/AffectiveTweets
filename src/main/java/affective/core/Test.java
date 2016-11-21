package affective.core;

import java.util.ArrayList;
import java.util.List;

public class Test {

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
	
	public static List<String> calculateCharNgram(String content,int n){
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
	
	public static void main(String[] args) {
		String[] sent={"Hello","my","dear","love"};
		List<String> sentList=new ArrayList<String>();
		for(String a:sent){
			sentList.add(a);
		}
		
//		List<String> ngrams=calculateTokenNgram(sentList,4);
//		for(String a:ngrams){
//			System.out.println(a);
//		}

		
		String hello="Hello world";
		
		
		List<String> cgrams= calculateCharNgram(hello,2);
		for(String a:cgrams){
			System.out.println(a);
		}
		
		
		
		
		
	}

}
