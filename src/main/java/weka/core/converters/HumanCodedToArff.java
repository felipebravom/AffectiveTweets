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
 *    HumanCodedToArff.java
 *    Copyright (C) 1999-2018 University of Waikato, Hamilton, New Zealand
 *
 */


package weka.core.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * Builds an arff dataset from the 6HumanCoded collection of tweets for sentiment
 * analysis. 
 * 
 * @author Felipe Bravo-Marquez (fbravoma@waikato.ac.nz)
 * @version 1.0
 */
public class HumanCodedToArff extends TweetCollectionToArff {

	/* (non-Javadoc)
	 * @see weka.core.converters.TweetCollectionToArff#createDataset(java.lang.String)
	 */
	@Override
	public Instances createDataset(String collectionPath) throws Exception {

		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		// The content of the tweet
		attributes.add(new Attribute("content", (ArrayList<String>) null));
		
		attributes.add(new Attribute("pos"));
		attributes.add(new Attribute("neg"));
		
		

		
		
		Instances dataset = new Instances(
				"6HumanCoded Dataset", attributes, 0); // The
																				// last
																				// attribute

		BufferedReader bf = new BufferedReader(new FileReader(collectionPath));
		String line=bf.readLine();
		while ((line = bf.readLine()) != null) {
			String parts[] = line.split("\t");

			if(parts.length==3){
				String content=parts[2];
				int pos=Integer.parseInt(parts[0].trim());
				int neg=Integer.parseInt(parts[1].trim());
				
								
				
				double values[] = new double[3];

				values[0] = dataset.attribute(0).addStringValue(content);
				values[1] = pos;
				values[2] = neg;

				Instance inst = new DenseInstance(1, values);
				dataset.add(inst);

			}

		}


		bf.close();

		return dataset;
	}

	/**
	 * Main method for testing this class.
	 * 
	 * should contain the path of input dataset and the name of
	 *            target file scheme (see Evaluation)
	 * @param args arguments           
	 */
	static public void main(String args[]) {

		if (args.length == 2) {

			TweetCollectionToArff ta = new HumanCodedToArff();

			try {
				Instances dataset = ta.createDataset(args[0]);
				ArffSaver saver = new ArffSaver();
				saver.setInstances(dataset);

				saver.setFile(new File(args[1]));
				saver.writeBatch();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
