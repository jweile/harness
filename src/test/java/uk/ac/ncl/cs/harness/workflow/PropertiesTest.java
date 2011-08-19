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

package uk.ac.ncl.cs.harness.workflow;

import junit.framework.TestCase;
import uk.ac.ncl.cs.harness.LoggingInit;
import uk.ac.ncl.cs.harness.workflow.variables.IncrementalVariable;

/**
 *
 * @author jweile
 */
public class PropertiesTest extends TestCase {
    
    public PropertiesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LoggingInit.init();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testProperties() {
        
        Properties properties = new Properties();
        
        IncrementalVariable var2 = new IncrementalVariable("2", 2.0, 3.0, 1.0);
        
        properties.setProperty("1", 1);
        properties.setProperty("2", var2);
        
        assertEquals(1, properties.getProperty("1"));
        assertEquals(2.0, properties.getProperty("2"));
        
        var2.step();
        
        assertEquals(3.0, properties.getProperty("2"));

    }

}
