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
import weka.core.Instances;
import weka.filters.AbstractFilterTest;
import weka.filters.Filter;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests ASA. Run from the command line with: <p/>
 * java weka.filters.unsupervised.attribute.ASATest
 *
 * @author FracPete and eibe
 * @version $Revision: 9568 $
 */
public class ASATest extends AbstractFilterTest {

    public ASATest(String name) {
        super(name);
    }

    /** Creates a default ASA filter */
    public Filter getFilter() {
        return new ASA();
    }

    /**
     * ASA is not suitable for use in a FilteredClassifier, so this just creates a dummy
     * FilteredClassifier so that the tests run through.
     *
     * @return the configured FilteredClassifier
     */
    protected FilteredClassifier getFilteredClassifier() {
        FilteredClassifier	result;

        result = new FilteredClassifier();

        result.setFilter(new weka.filters.AllFilter());
        result.setClassifier(new weka.classifiers.rules.ZeroR());

        return result;
    }

    /**
     * Called by JUnit before each test method. Sets up the Instances object to use based on 
     * one of the datasets that comes with the package.
     *
     * @throws Exception if an error occurs reading the example instances.
     */
    protected void setUp() throws Exception {
        super.setUp();

        m_Instances = (new weka.core.converters.ConverterUtils.DataSource("data/sent140test.arff.gz")).getDataSet();
	m_Instances.setClassIndex(m_Instances.numAttributes() - 1);
    }

    /**
     * Sets up the standard test using one of the lexicons that comes with the package.
     */
    public void testTypical() {
        Instances icopy = new Instances(m_Instances);

        m_Filter = getFilter();
	((ASA)m_Filter).setLexicon(new java.io.File("lexicons/arff_lexicons/NRC-AffectIntensity-Lexicon.arff"));

        Instances result = useFilter();
    }

    public static Test suite() {
        return new TestSuite(ASATest.class);
    }

    public static void main(String[] args){
        junit.textui.TestRunner.run(suite());
    }
}
