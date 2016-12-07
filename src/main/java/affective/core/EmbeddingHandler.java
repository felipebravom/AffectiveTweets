package affective.core;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;

/** Embeddings files must have following format dim1<tab>dim2....<tab>word **/

public class EmbeddingHandler implements Serializable {
	
	/** For serialization **/ 
	private static final long serialVersionUID = -2458037798910799631L;
	
	private String filePath;
	private Object2ObjectMap<String, AbstractDoubleList> wordMap;
	private int dimensions;



	public EmbeddingHandler(String filePath){
		this.filePath=filePath;
		this.wordMap=new Object2ObjectOpenHashMap<String, AbstractDoubleList>() ;
	}


	public void createDict() throws Exception{
		
		FileInputStream fin = new FileInputStream(this.filePath);
		GZIPInputStream gzis = new GZIPInputStream(fin);
		InputStreamReader xover = new InputStreamReader(gzis);
		BufferedReader bf = new BufferedReader(xover);

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
		xover.close();
		gzis.close();
		fin.close();

	}
	
	public Object2ObjectMap<String, AbstractDoubleList> getWordMap() {
		return wordMap;
	}




	public int getDimensions() {
		return dimensions;
	}


	static public void main(String args[]) throws Exception{
		String file="resources/edim_lab_word2Vec.csv";
		EmbeddingHandler word2VecDict=new EmbeddingHandler(file);
		word2VecDict.createDict();
		System.out.println(word2VecDict.getDimensions());
		
		System.out.println(word2VecDict.getWordMap().get("you").toString());
		
		

	}

}
