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

package uk.ac.ncl.cs.harness.graph;

import junit.framework.TestCase;
import uk.ac.ncl.cs.harness.LoggingInit;
import uk.ac.ncl.cs.harness.util.SetOfTwo;

/**
 *
 * @author jweile
 */
public class MapGraphTest extends TestCase {
    
    public MapGraphTest(String testName) {
        super(testName);
    }

    private Graph graph;

    @Override
    protected void setUp() throws Exception {

        LoggingInit.init();

        super.setUp();
        
        graph = new MapGraph();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of setName method, of class MapGraph.
     */
    public void testName() {
        String name = "test";
        graph.setName(name);
        assertEquals(name, graph.getName());
    }


    /**
     * Test of createNode method, of class MapGraph.
     */
    public void testNodesAndEdges() {
        Node a = graph.createNode("A");
        Node b = graph.createNode("B");

        assertEquals(2,graph.getNumNodes());

        SetOfTwo<Node> ab = new SetOfTwo<Node>(a,b);
        Edge e = graph.createEdge(ab);

        assertTrue(graph.contains(a));
        assertTrue(graph.contains(b));
        assertTrue(graph.contains(e));

        assertEquals(1, graph.getNumEdges());

        Neighbour neighbourOfA = graph.getNeighboursOfNode(a).iterator().next();
        assertEquals(b, neighbourOfA.getNode());
        assertEquals(e, neighbourOfA.getConnectingEdge());
    }

    public void testAlienHandling() {
        Graph g2 = new MapGraph();
        g2.setName("g2");

        Node g2a = g2.createNode("A");
        Node g2c = g2.createNode("C");

        SetOfTwo<Node> ac = new SetOfTwo<Node>(g2a,g2c);

        Edge e = graph.createEdge(ac);
        assertNull(e);
    }

    public void testEdge() {

        Node a = graph.createNode("A");
        Node b = graph.createNode("B");

        SetOfTwo<Node> ab = new SetOfTwo<Node>(a,b);
        Edge e = graph.createEdge(ab);
        
        SetOfTwo<Node> nodes = e.getConnectedNodes();
        assertEquals(ab,nodes);

        String probname = "testprob";
        double probval = 0.5;

        e.addProbability(probname, probval);
        assertEquals(1, e.getProbabilities().size());
        assertEquals(probval, e.getProbability(probname).getValue());
    }

}
