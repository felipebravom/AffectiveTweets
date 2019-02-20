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

package weka;

import junit.framework.Test;
import junit.framework.TestSuite;

import weka.filters.unsupervised.attribute.ASATest;
import weka.filters.unsupervised.attribute.PTCMTest;
import weka.filters.unsupervised.attribute.LexiconDistantSupervisionTest;
import weka.filters.unsupervised.attribute.TweetNLPPOSTaggerTest;
import weka.filters.supervised.attribute.PMILexiconExpanderTest;
import weka.filters.unsupervised.attribute.TweetCentroidTest;
import weka.filters.unsupervised.attribute.LabelWordVectorsTest;

import weka.filters.unsupervised.attribute.TweetToSentiStrengthFeatureVectorTest;
import weka.filters.unsupervised.attribute.TweetToSparseFeatureVectorTest;
import weka.filters.unsupervised.attribute.TweetToLexiconFeatureVectorTest;
import weka.filters.unsupervised.attribute.TweetToInputLexiconFeatureVectorTest;
import weka.filters.unsupervised.attribute.TweetToEmbeddingsFeatureVectorTest;
import weka.filters.unsupervised.attribute.TweetToWordListCountFeatureVectorTest;

/**
 * Test class for all tests in this directory. Run from the command line with:
 * <p>
 * java weka.AllPackageTests
 * 
 * @author Eibe Frank
 * @version $Revision: 10160 $
 */
public class AllPackageTests extends TestSuite {

  public static Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTestSuite(ASATest.class);
    suite.addTestSuite(PTCMTest.class);
    suite.addTestSuite(LexiconDistantSupervisionTest.class);

    suite.addTestSuite(PMILexiconExpanderTest.class);
    suite.addTestSuite(TweetCentroidTest.class);
    suite.addTestSuite(LabelWordVectorsTest.class);

    suite.addTestSuite(TweetToSparseFeatureVectorTest.class);
    suite.addTestSuite(TweetToLexiconFeatureVectorTest.class);
    suite.addTestSuite(TweetToInputLexiconFeatureVectorTest.class);
    suite.addTestSuite(TweetToSentiStrengthFeatureVectorTest.class);
    suite.addTestSuite(TweetToEmbeddingsFeatureVectorTest.class);
    suite.addTestSuite(TweetNLPPOSTaggerTest.class);
    suite.addTestSuite(TweetToWordListCountFeatureVectorTest.class);    


    return suite;
  }

  public static void main(String[] args) {

    junit.textui.TestRunner.run(suite());
  }
}
