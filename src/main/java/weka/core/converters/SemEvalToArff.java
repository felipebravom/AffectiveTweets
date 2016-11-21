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

public class SemEvalToArff extends TweetCollectionToArff {

	@Override
	public Instances createDataset(String collectionPath) throws Exception {

		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		// The content of the tweet
		attributes.add(new Attribute("content", (ArrayList<String>) null));

		// The target label
		ArrayList<String> label = new ArrayList<String>();
		label.add("positive");
		label.add("neutral");
		label.add("negative");

		attributes.add(new Attribute("Class", label));
		Instances dataset = new Instances(
				"Twitter Sentiment Analysis SemEval Dataset", attributes, 0); // The
																				// last
																				// attribute

		BufferedReader bf = new BufferedReader(new FileReader(collectionPath));
		String line;
		while ((line = bf.readLine()) != null) {
			String parts[] = line.split("\t");

			String content = parts[3];
			String target = parts[2];

			double values[] = new double[2];

			// add the content
			values[0] = dataset.attribute(0).addStringValue(content);

			// add the label
			if (target.equals("positive")) {
				values[1] = dataset.attribute(1).indexOfValue("positive");
			} else if (target.equals("neutral") || target.equals("objective")
					|| target.equals("objective-OR-neutral")) {
				values[1] = dataset.attribute(1).indexOfValue("neutral");
			} else {
				values[1] = dataset.attribute(1).indexOfValue("negative");
			}

			Instance inst = new DenseInstance(1, values);
			dataset.add(inst);

		}

		// set the class index
		dataset.setClassIndex(dataset.numAttributes() - 1);

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

			TweetCollectionToArff ta = new SemEvalToArff();

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
