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

package uk.ac.ncl.cs.harness.experiments;

import junit.framework.TestCase;
import uk.ac.ncl.cs.harness.LoggingInit;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.MapGraph;
import uk.ac.ncl.cs.harness.graph.populators.ScaleFreePopulator;

/**
 *
 * @author jweile
 */
public class SimpleExperimentTest extends TestCase {

    private Graph graph;

    public SimpleExperimentTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {

        LoggingInit.init();

        super.setUp();

        graph = new MapGraph();
        graph.setName("main");
        ScaleFreePopulator populator = new ScaleFreePopulator();
        populator.setSeed(3);
        populator.setNumberOfNodes(500);
        populator.populate(graph);

        System.out.println("Template edges: "+graph.getNumEdges());

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        graph = null;
    }

    /**
     * Test of perform method, of class SimpleExperiment.
     */
    public void testPerform() {
        SimpleExperiment experiment = new SimpleExperiment();
        experiment.setSensitivity(.99);
        experiment.setSpecificity(.9997);
        Graph experimentalGraph = new MapGraph();
        experimentalGraph.setName("experimental");
        experiment.perform(graph, experimentalGraph);

        System.out.println("Experimental edges: "+experimentalGraph.getNumEdges());
    }

}
