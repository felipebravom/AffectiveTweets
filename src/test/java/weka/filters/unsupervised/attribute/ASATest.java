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
 * Copyright (C) 2019 University of Waikato, Hamilton, New Zealand
 */

package weka.filters.unsupervised.attribute;

import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.TestInstances;
import weka.filters.AbstractFilterTest;
import weka.filters.Filter;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests ASA. Run from the command line with: <p/>
 * java weka.filters.unsupervised.attribute.ASATest
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 9568 $
 */
public class ASATest
        extends AbstractFilterTest {

    public ASATest(String name) {
        super(name);
    }

    /** Creates a default ASA */
    public Filter getFilter() {
        return new ASA();
    }

    /**
     * returns the configured FilteredClassifier. Since the base classifier is
     * determined heuristically, derived tests might need to adjust it.
     *
     * @return the configured FilteredClassifier
     */
    protected FilteredClassifier getFilteredClassifier() {
        FilteredClassifier	result;

        result = new FilteredClassifier();

        result.setFilter(getFilter());
        result.setClassifier(new weka.classifiers.functions.SMO());

        return result;
    }

    /**
     * returns data generated for the FilteredClassifier test
     *
     * @return		the dataset for the FilteredClassifier
     * @throws Exception	if generation of data fails
     */
    protected Instances getFilteredClassifierData() throws Exception{
        TestInstances	test;
        Instances		result;

        test = TestInstances.forCapabilities(m_FilteredClassifier.getCapabilities());
        test.setClassIndex(TestInstances.CLASS_IS_LAST);

        result = test.generate();

        return result;
    }

    /**
     * Called by JUnit before each test method. This implementation creates
     * the default filter to test and loads a test set of Instances.
     *
     * @throws Exception if an error occurs reading the example instances.
     */
    protected void setUp() throws Exception {
        super.setUp();

        m_Instances = (new weka.core.converters.ConverterUtils.DataSource("data/sem-eval-train-dev-test-2013.arff.gz")).getDataSet();
	m_Instances.setClassIndex(m_Instances.numAttributes() - 1);
    }

    /**
     * performs a typical test
     */
    public void testTypical() {
        Instances icopy = new Instances(m_Instances);

        m_Filter = getFilter();
	((ASA)m_Filter).setLexicon(new java.io.File("lexicons/arff_lexicons/metaLexEmo.arff"));

        Instances result = useFilter();
    }

    public static Test suite() {
        return new TestSuite(ASATest.class);
    }

    public static void main(String[] args){
        junit.textui.TestRunner.run(suite());
    }
}
