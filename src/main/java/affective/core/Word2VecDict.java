package affective.core;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Word2VecDict {
	private String filePath;
	private Object2ObjectMap<String, AbstractDoubleList> wordMap;
	private int dimensions;



	public Word2VecDict(String filePath){
		this.filePath=filePath;
		this.wordMap=new Object2ObjectOpenHashMap<String, AbstractDoubleList>() ;
	}


	public void createDict() throws Exception{

		BufferedReader bf=new BufferedReader(new FileReader(this.filePath));

		String line;
		boolean firstLine=true;
		while ((line = bf.readLine()) != null) {
			//		System.out.println(line);
			String parts[]=line.split("\t");

			AbstractDoubleList wordVector=new DoubleArrayList();
			for(int i=0;i<parts.length-1;i++){
				wordVector.add(Double.parseDouble(parts[i]));
			}
			
			if(firstLine)
				this.dimensions=wordVector.size();

			if(wordVector.size()==this.dimensions)
				this.wordMap.put(parts[parts.length-1], wordVector);



		}
		bf.close();

	}
	
	public Object2ObjectMap<String, AbstractDoubleList> getWordMap() {
		return wordMap;
	}




	public int getDimensions() {
		return dimensions;
	}


	static public void main(String args[]) throws Exception{
		String file="resources/edim_lab_word2Vec.csv";
		Word2VecDict word2VecDict=new Word2VecDict(file);
		word2VecDict.createDict();
		System.out.println(word2VecDict.getDimensions());
		
		System.out.println(word2VecDict.getWordMap().get("you").toString());
		
		

	}

}
