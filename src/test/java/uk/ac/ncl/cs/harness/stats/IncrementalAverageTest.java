/*
 *  Copyright (C) 2011 Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ncl.cs.harness.stats;

import junit.framework.TestCase;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class IncrementalAverageTest extends TestCase {
    
    public IncrementalAverageTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

     public void testAverage() {
         IncrementalAverage av = new IncrementalAverage(1.0);

         av.updateWith(1.0);
         assertAlmostEqual(1.0, av.getAverage());

         av.updateWith(1.0);
         assertAlmostEqual(1.0, av.getAverage());

         av.updateWith(0.0);
         assertAlmostEqual(3.0 / 4.0, av.getAverage());

         av.updateWith(0.0);
         assertAlmostEqual(3.0 / 5.0, av.getAverage());

         av.updateWith(1.0);
         assertAlmostEqual(4.0 / 6.0, av.getAverage());

         av.updateWith(0.0);
         assertAlmostEqual(4.0 / 7.0, av.getAverage());
     }

     private void assertAlmostEqual(double expected, double found) {
         double diff = Math.abs(expected - found);
         double maxDiff = .000000000000001;
         if (diff > maxDiff) {
             throw new AssertionError("expected: "+expected+" \u00B1"+maxDiff+" but was: "+found);
         }
     }

}
