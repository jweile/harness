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

package uk.ac.ncl.cs.harness.workflow.variables;

import junit.framework.TestCase;

/**
 *
 * @author jweile
 */
public class IncrementalVariableTest extends TestCase {
    
    public IncrementalVariableTest(String testName) {
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

    public void testVariable() {
        double min = 1.0, max = 5.0, inc = 1.0;

        Variable<Double> var = new IncrementalVariable("test", min, max, inc);

        int i = 1;
        double currVal = min;
        while (var.canStep()) {

            assertEquals(currVal, var.getCurrentValue());
            assertTrue(var.getCurrentValue() <= max);

            var.step(); currVal += inc; i++;
        }
        assertEquals(currVal, 5.0);
        assertEquals(var.getNumTotalIterations(), i);
    }

}
