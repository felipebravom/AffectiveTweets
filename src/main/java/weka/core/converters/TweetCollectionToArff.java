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
 *    TweetCollectionToArff.java
 *    Copyright (C) 1999-2016 University of Waikato, Hamilton, New Zealand
 *
 */


package weka.core.converters;

import weka.core.Instances;


/**
* Builds an arff dataset from a collection of tweets in a given file.
* @author Felipe Bravo-Marquez (fjb11 at students.waikato.ac.nz)
* @version 1.0
*/

public abstract class TweetCollectionToArff {
	
	
	/**
	 * Creates the dataset.
	 * @param collectionPath the file wit he the input collection
	 * @return the Instances weka object
	 * @throws Exception if something goes wrong
	 */
	public abstract Instances createDataset(String collectionPath) throws Exception;
}
