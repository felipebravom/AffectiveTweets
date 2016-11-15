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
 *    Spearman.java
 *    Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 */


package weka.classifiers.evaluation;

import java.util.ArrayList;
import java.util.List;


import fantail.core.Correlation;
import weka.core.Instance;
import weka.core.Utils;

/**
 *  Provides Spearmans's rho rank correlation coefficient for evaluating regression schemes. Implementation taken from Quan Sun's Fantail project.
 *  More info at: https://en.wikipedia.org/wiki/Spearman%27s_rank_correlation_coefficient
 * @author Felipe Bravo-Marquez (fjb11@students.waikato.ac.nz)
 * @version $Revision: 1 $
 */
public class Spearman extends AbstractEvaluationMetric implements StandardEvaluationMetric {



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
	    return "spearman";
	  }

	  /**
	   * A brief description of the metrics.
	   * @return a brief description of the metrics.
	   */
	  public String getMetricDescription() { return "The Spearman rho statistics."; }

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
		// Do nothing
	  }

	  /**
	   * Returns the (short) names of the statistics that are made available.
	   * @return a list of short names
	   */
	  public List<String> getStatisticNames() {

	    ArrayList<String> names = new ArrayList<String>();
	    names.add("spearman");

	    return names;
	  }

	  /**
	   * Produces string providing textual summary of statistics.
	   * @return the string produced
	   */
	  public String toSummaryString() {

	    return       "Spearman's rho                     " + Utils.doubleToString(getStatistic("spearman"), 12, 4) + "\n";
	  }

	  /**
	   * Returns the value of the statistic based on the given short name.
	   * @param name the short name
	   * @return the value of the statistic
	   */
	  public double getStatistic(String name) {

	    if (!name.equals("spearman")) {
	      throw new UnknownStatisticException("Statistic " + name + " is unknown.");
	    }
	    
	    
	    ArrayList<Prediction> pred=this.m_baseEvaluation.m_Predictions;
	    double [] predictedVals=new double[pred.size()];
	    double [] actualVals=new double[pred.size()];
	    
	    for(int i=0;i<pred.size();i++){
	    		actualVals[i]=pred.get(i).actual();
	    		predictedVals[i]=pred.get(i).predicted();
	    }
    
    		
	    return Correlation.spearman(actualVals, predictedVals);
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
