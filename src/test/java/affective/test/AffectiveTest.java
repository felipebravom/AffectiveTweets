package affective.test;



import java.util.ArrayList;

import org.junit.Assert;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.TweetToSparseFeatureVector;
import junit.framework.TestCase;

public class AffectiveTest extends TestCase {


	public static void addInstance(Instances dataset, String content, String sent){
		double values[] = new double[2];
		values[0] = dataset.attribute(0).addStringValue(content);
		values[1] = dataset.attribute(1).indexOfValue(sent);	
		dataset.add(new DenseInstance(1, values));

	}

	public static Instances createDataset(){

		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		// The content of the tweet
		attributes.add(new Attribute("content", (ArrayList<String>) null));

		ArrayList<String> label = new ArrayList<String>();
		label.add("negative");
		label.add("positive");

		attributes.add(new Attribute("Class", label));	


		Instances dataset = new Instances("Testing dataset", attributes, 0);	

		addInstance(dataset,"i love you","positive");
		addInstance(dataset,"i hate you","negative");
		addInstance(dataset,"she is sad","negative");
		addInstance(dataset,"she is happy","positive");		



		return dataset;

	}



	public static void testSparseFeatureVector() throws Exception {
		Instances data = createDataset();		
		Filter f = new TweetToSparseFeatureVector();
		f.setInputFormat(data);		
		Instances newData = Filter.useFilter(data, f);		
		Assert.assertEquals(newData.numAttributes(), 10);
	}


	static public void main(String args[]) throws Exception{
		Instances data = createDataset();

		Filter f = new TweetToSparseFeatureVector();
		f.setInputFormat(data);

		System.out.println(data);

		Instances newData = Filter.useFilter(data, f);

		System.out.println(newData.numAttributes());

		System.out.println(newData);


	}





}
