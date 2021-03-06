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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import uk.ac.ncl.cs.harness.LoggingInit;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.MapGraph;
import uk.ac.ncl.cs.harness.graph.populators.ScaleFreePopulator;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class ReductionistExperimentTest extends TestCase {

    /**
     * the graph
     */
    private Graph graph;

    public ReductionistExperimentTest(String testName) {
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

        Logger.getLogger(this.getClass().getName())
                .log(Level.INFO, "Template edges: {0}",
                     graph.getNumEdges());
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

        
        double sens = .9;
        double npr = .5;

        ReductionistExperiment experiment = new ReductionistExperiment();
        experiment.setSensitivity(sens);
        experiment.setNpr(npr);

        Graph experimentalGraph = new MapGraph();
        experimentalGraph.setName("experimental");
        experiment.perform(graph, experimentalGraph);

        int tp = intersectionCount(graph, experimentalGraph);
        int pos = experimentalGraph.getNumEdges();

        double actualSens = (double)tp / (double)graph.getNumEdges();
        double actualFpr = (double)(pos - tp) / (double)pos;


        Logger.getLogger(this.getClass().getName())
                .log(Level.INFO, "Number of edges: {0}",
                     new Object[]{pos});

        Logger.getLogger(this.getClass().getName())
                .log(Level.INFO, "Sensitivity discrepancy: {0} -> {1}",
                     new Object[]{sens, actualSens});

        Logger.getLogger(this.getClass().getName())
                .log(Level.INFO, "FPR discrepancy: {0} -> {1}",
                     new Object[]{npr, actualFpr});


        Logger.getLogger(this.getClass().getName())
                .log(Level.INFO, "Experimental edges: {0}",
                     experimentalGraph.getNumEdges());
    }

     /**
     * returns the cardinality of the intersection between the sets of edges
     * in the two given graphs
     * @param g1 a graph
     * @param g2 another graph
     * @return the cardinality of the intersection between the sets of edges
     */
    private int intersectionCount(Graph g1, Graph g2) {

        Set<Edge> intersection = new HashSet<Edge>(g1.getEdges());
        intersection.retainAll(g2.getEdges());

        return intersection.size();
    }


}
