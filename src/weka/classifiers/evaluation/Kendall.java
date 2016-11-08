package weka.classifiers.evaluation;

import java.util.ArrayList;
import java.util.List;


import fantail.core.Correlation;
import weka.core.Instance;
import weka.core.Utils;

public class Kendall extends AbstractEvaluationMetric implements StandardEvaluationMetric {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5805030031023432157L;


	  
	  protected ArrayList<Double> predictedValues=new ArrayList<Double>();
	  protected ArrayList<Double> instanceValues=new ArrayList<Double>();

	  /**
	   * Whether metric applies to nominal class.
	   * @return false
	   */
	  public boolean appliesToNominalClass() {
	    return false;
	  }

	  /**
	   * Whether metric applies to numeric class.
	   * @return true
	   */
	  public boolean appliesToNumericClass() {
	    return true;
	  }

	  /**
	   * The names of the metrics.
	   * @return the names of the metrics.
	   */
	  public String getMetricName() {
	    return "kendall";
	  }

	  /**
	   * A brief description of the metrics.
	   * @return a brief description of the metrics.
	   */
	  public String getMetricDescription() { return "The kendall tau statistics."; }

	  /**
	   * Update stats for a nominal class. Does nothing because metrics are for regression only.
	   * @param predictedDistribution the probabilities assigned to each class
	   * @param instance the instance to be classified
	   */
	  public  void updateStatsForClassifier(double[] predictedDistribution, Instance instance) {
	    // Do nothing
	    }

	  /**
	   * Update stats for a numeric class.
	   * @param predictedValue the value that is predicted
	   * @param instance the instance to be classified
	   */
	  public  void updateStatsForPredictor(double predictedValue, Instance instance) {

	    if (!instance.classIsMissing()) {
	      if (!Utils.isMissingValue(predictedValue)) {
	    	  this.predictedValues.add(predictedValue);
	    	  this.instanceValues.add(instance.classValue());
	      }
	    }
	  }

	  /**
	   * Returns the (short) names of the statistics that are made available.
	   * @return a list of short names
	   */
	  public List<String> getStatisticNames() {

	    ArrayList<String> names = new ArrayList<String>();
	    names.add("kendall");

	    return names;
	  }

	  /**
	   * Produces string providing textual summary of statistics.
	   * @return the string produced
	   */
	  public String toSummaryString() {

	    return       "Kentall Tau " + Utils.doubleToString(getStatistic("kendall"), 12, 4) + "\n";
	  }

	  /**
	   * Returns the value of the statistic based on the given short name.
	   * @param name the short name
	   * @return the value of the statistic
	   */
	  public double getStatistic(String name) {

	    if (!name.equals("kendall")) {
	      throw new UnknownStatisticException("Statistic " + name + " is unknown.");
	    }
	    
	    double [] predictedV = new double[this.predictedValues.size()];
	    double [] instanceV = new double[this.predictedValues.size()];
	    
	    for(int i=0;i<predictedV.length;i++){
	    		predictedV[i]=this.predictedValues.get(i);
	    		instanceV[i]=this.instanceValues.get(i);
	    	
	    }
	    		
	    return Math.sqrt(Correlation.rankKendallTauBeta(instanceV, predictedV));
	  }

	  /**
	   * Whether metric is to be maximized.
	   *
	   * @return false
	   */
	  public boolean statisticIsMaximisable(String statName) {

	    return false;
	}

}
