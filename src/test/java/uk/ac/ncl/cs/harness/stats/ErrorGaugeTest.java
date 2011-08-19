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
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.MapGraph;
import uk.ac.ncl.cs.harness.graph.Node;
import uk.ac.ncl.cs.harness.util.SetOfTwo;
import static uk.ac.ncl.cs.harness.stats.ErrorGauge.*;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class ErrorGaugeTest extends TestCase {
    
    public ErrorGaugeTest(String testName) {
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

    
    public void testErrorRates() {

        Graph trueGraph = new MapGraph();

        Node n1 = trueGraph.createNode("1");
        Node n2 = trueGraph.createNode("2");
        Node n3 = trueGraph.createNode("3");
        Node n4 = trueGraph.createNode("4");

        trueGraph.createEdge(new SetOfTwo<Node>(n1,n2));
        trueGraph.createEdge(new SetOfTwo<Node>(n2,n4));
        trueGraph.createEdge(new SetOfTwo<Node>(n1,n3));
        trueGraph.createEdge(new SetOfTwo<Node>(n3,n4));

        Graph testGraph = new MapGraph();

        n1 = testGraph.createNode("1");
        n2 = testGraph.createNode("2");
        n3 = testGraph.createNode("3");
        n4 = testGraph.createNode("4");

        testGraph.createEdge(new SetOfTwo<Node>(n1,n2));
        testGraph.createEdge(new SetOfTwo<Node>(n2,n4));
        testGraph.createEdge(new SetOfTwo<Node>(n1,n3));
        testGraph.createEdge(new SetOfTwo<Node>(n2,n3));
        testGraph.createEdge(new SetOfTwo<Node>(n1,n4));

        double[] rates = ErrorGauge.calculate(testGraph, trueGraph);

        assertEquals("FP rate wrong!", 1.0, rates[FP_RATE]);
        assertEquals("FN rate wrong!", 0.25 , rates[FN_RATE]);
        assertEquals("TP wrong!", 3.0 , rates[TP]);
        assertEquals("FP wrong!", 2.0 , rates[FP]);
        assertEquals("TN wrong!", 0.0 , rates[TN]);
        assertEquals("FN wrong!", 1.0 , rates[FN]);

    }


}
