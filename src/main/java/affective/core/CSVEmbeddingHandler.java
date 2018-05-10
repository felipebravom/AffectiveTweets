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
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */



package affective.core;

import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import weka.core.OptionMetadata;
import weka.core.SingleIndex;
import weka.core.WekaPackageManager;


/**
 *  <!-- globalinfo-start --> 
 *  This class is used for handling word vector or embeddings stored in gzipped files.
 * 
 * <!-- globalinfo-end -->
 * 
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class CSVEmbeddingHandler extends EmbeddingHandler {

	/** For serialization **/ 
	private static final long serialVersionUID = -2458037798910799631L;

	/** Default path to where resources are stored. */
	public static String RESOURCES_FOLDER_NAME = WekaPackageManager.PACKAGES_DIR.toString() + File.separator + "AffectiveTweets" + File.separator + "resources";


	/** Embedding File Name.    **/
	protected File embeddingsFile=new File(RESOURCES_FOLDER_NAME + File.separator + "w2v.twitter.edinburgh.100d.csv.gz");


	/** The separator String    **/
	protected String separator="TAB";


	/** the index of the string attribute to be processed */
	protected SingleIndex wordNameIndex = new SingleIndex("last");




	/**
	 * Returns a string describing this filter.
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */	
	public String globalInfo() {
		return "This object handles word embeddings in csv.gz format. \n";
	}





	/* (non-Javadoc)
	 * @see affective.core.EmbeddingHandler#createDict()
	 */
	public void createDict() throws Exception {

		FileInputStream fin = new FileInputStream(this.embeddingsFile);
		GZIPInputStream gzis = new GZIPInputStream(fin);
		InputStreamReader xover = new InputStreamReader(gzis);
		BufferedReader bf = new BufferedReader(xover);

		this.separator = this.separator.equals("TAB")?"\t":this.separator;
		
		
		String line;
		boolean firstLine=true;
		while ((line = bf.readLine()) != null) {
			String parts[]=line.split(this.separator);

			AbstractDoubleList wordVector=new DoubleArrayList();
			if(firstLine){
				this.dimensions=parts.length-1;

				this.wordNameIndex.setUpper(this.dimensions);
				firstLine=false;				
			}
			
			// only consider lines with right number of dimensions
			if(parts.length-1==this.dimensions){
				for(int i=0;i<parts.length-1;i++){
					if(i!=this.wordNameIndex.getIndex())				
						wordVector.add(Double.parseDouble(parts[i]));
				}

				
				this.wordMap.put(parts[this.wordNameIndex.getIndex()], wordVector);

			}



		}
		bf.close();
		xover.close();
		gzis.close();
		fin.close();
	

	}



	@OptionMetadata(displayName = "embeddingsFile",
			description = "The file name containing the word vectors. It has to be a gzip compressed csv file",
			commandLineParamName = "K", commandLineParamSynopsis = "-K <string>",
			displayOrder = 1)
	public File getEmbeddingsFile() {
		return embeddingsFile;
	}
	public void setEmbeddingsFile(File embeddingsFile) {
		this.embeddingsFile = embeddingsFile;
	}


	@OptionMetadata(displayName = "separator",
			description = "The string to use as separator for the columns (you can use 'TAB' or '\\t' for the TAB symbol). \t Default:TAB",
			commandLineParamName = "sep", commandLineParamSynopsis = "-sep <string>",
			displayOrder = 2)	
	public String getSeparator() {
		return separator;
	}
	public void setSeparator(String separator) {
		this.separator = separator;
	}




	@OptionMetadata(displayName = "wordNameIndex",
			description = "The index (starting from 1) of the word string in the file. First and last are valid values. ",
			commandLineParamName = "I", commandLineParamSynopsis = "-I <col>",
			displayOrder = 0)	
	public String getWordNameIndex() {
		return wordNameIndex.getSingleIndex();
	}
	public void setWordNameIndex(String wordNameIndex) {
		this.wordNameIndex.setSingleIndex(wordNameIndex);
	}




}
