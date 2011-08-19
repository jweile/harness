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

package uk.ac.ncl.cs.harness.graph.populators;

import junit.framework.TestCase;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.MapGraph;

/**
 *
 * @author jweile
 */
public class ScaleFreePopulatorTest extends TestCase {

    private GraphPopulator pop;

    public ScaleFreePopulatorTest(String testName) {
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


    /**
     * Test of populate method, of class ScaleFreePopulator.
     */
    public void testPopulate() {

        int seed = 4;
        int numNodes = 500;


        Graph graph = new MapGraph();
        graph.setName("test");

        pop = new ScaleFreePopulator();
        ((ScaleFreePopulator)pop).setSeed(seed);
        ((ScaleFreePopulator)pop).setNumberOfNodes(numNodes);

        pop.populate(graph);
        
        assertEquals("Number of nodes", numNodes, graph.getNumNodes());

        int surplusEdges = (((seed * seed)- seed) / 2) - seed;
        assertEquals("Number of edges", numNodes+surplusEdges, graph.getNumEdges());


    }


}
