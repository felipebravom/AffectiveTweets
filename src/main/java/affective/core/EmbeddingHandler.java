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
 *    EmbeddingHandler.java
 *    Copyright (C) 1999-2016 University of Waikato, Hamilton, New Zealand
 *
 */



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


/**
 *  <!-- globalinfo-start --> 
 *  This class is used for handling word vector or embeddings stored in zgipped files with format:
 *  emb1<tab>emb2<tab>..word. 
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * 
 * @author Felipe Bravo-Marquez (fjb11@students.waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class EmbeddingHandler implements Serializable {
	
	/** For serialization **/ 
	private static final long serialVersionUID = -2458037798910799631L;
	
	/** File where word vectors are stored. */
	private String filePath;
	
	/** Mapping between words and their vectors. */
	private Object2ObjectMap<String, AbstractDoubleList> wordMap;
	
	/** Number of dimensions of the embeddings. */ 
	private int dimensions;


    /**
     * initializes the EmbeddingHandler
     * 
     * @param filePath the file with the embeddings
     */
	public EmbeddingHandler(String filePath){
		this.filePath=filePath;
		this.wordMap=new Object2ObjectOpenHashMap<String, AbstractDoubleList>() ;
	}

	 /**
     * initializes the dictionary
     * 
     * @param filePath the file with the embeddings
     * @throws Exception in case of wrong file
     */
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
