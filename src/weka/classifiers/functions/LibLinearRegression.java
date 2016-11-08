package weka.classifiers.functions;

import weka.core.Capabilities;
import weka.core.Capabilities.Capability;

public class LibLinearRegression extends LibLINEAR  {

	/**
	 *  Extends LibLinear Libraries to support regression models 
	 */
	private static final long serialVersionUID = -6462140841851912977L;


	   public Capabilities getCapabilities() {
		   Capabilities result = super.getCapabilities();
		   result.enable(Capability.NUMERIC_CLASS);
		
		   
		   return result;
		   
	   }


}
