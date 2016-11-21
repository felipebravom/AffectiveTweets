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
 * Builds an arff dataset from the SemEval collection of tweets for sentiment
 * analysis. More info about the task:
 * https://www.cs.york.ac.uk/semeval-2013/task2/
 * 
 * @author Felipe Bravo-Marquez (fjb11 at students.waikato.ac.nz)
 * @version 1.0
 */

public class HumanCodedToArff extends TweetCollectionToArff {

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
	 * @param argv
	 *            should contain the path of input dataset and the name of
	 *            target file scheme (see Evaluation)
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
