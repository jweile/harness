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

import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import java.util.ArrayList;
import java.util.List;
import uk.ac.ncl.cs.harness.extsupport.Extension;
import uk.ac.ncl.cs.harness.extsupport.ExtensionProperty;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Neighbour;
import uk.ac.ncl.cs.harness.graph.Node;
import uk.ac.ncl.cs.harness.stats.RandomEngineRegistry;
import uk.ac.ncl.cs.harness.util.SetOfTwo;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
@Extension(id="reductionist")
public class ReductionistExperiment implements Experiment {

    /**
     * The experiment's sensitivity.
     */
    private double sensitivity;

    /**
     * The experiment's negative prediction rate.
     */
    private double npr;

    /**
     * A poisson distributed random number generator.
     */
    private Poisson poisson;

    /**
     * A uniform random number generator.
     */
    private Uniform uniform;

    /**
     * constructor
     */
    public ReductionistExperiment() {
        poisson = new Poisson(250, RandomEngineRegistry.getEngine());
        uniform = new Uniform(RandomEngineRegistry.getEngine());
    }


    /**
     * Sets the sensitivity of the experiment.
     * @param sensitivity The desired sensitivity value.
     */
    @ExtensionProperty(id="sensitivity")
    public void setSensitivity(double sensitivity) {
        if (sensitivity < 0.0 || sensitivity > 1.0) {
            throw new IllegalArgumentException(
                    "Precondition violation: 0 <= sensitivity <= 1");
        }
        this.sensitivity = sensitivity;
    }


    /**
     * sets the negative prediction rate.
     * @throws IllegalArgumentException if preconditions are not met.
     */
    @ExtensionProperty(id="npr")
    public void setNpr(double fpr) throws IllegalArgumentException {
        if (fpr < 0.0 || fpr >= 1.0) {
            throw new IllegalArgumentException(
                    "Precondition violation: 0 <= fpr < 1");
        }
        this.npr = fpr;
    }


    /**
     * Performs the experiment on the true biolocial graph <code>trueGraph</code>
     * The results are written into the given graph <code>emptyGraph</code>
     * @param trueGraph the true biological template graph
     * @param emptyGraph an empty graph which will be filled with the experiment's
     * results.
     * @see uk.ac.ncl.cs.harness.experiments.Experiment
     */
    @Override
    public void perform(Graph trueGraph, Graph outputGraph) {

        List<Node> nodes = new ArrayList<Node>(trueGraph.getNodes());

        //pick a gene
        Node favoriteNode = nodes.get(uniform.nextIntFromTo(0, nodes.size()-1));
        Node favoriteLocal = outputGraph.createNode(favoriteNode.getId());

        for (Neighbour neighbour : favoriteNode.getNeighbours()) {
            double r = uniform.nextDouble();
            if (r < sensitivity) {
                Node neighbourLocal = outputGraph.createNode(neighbour.getNode().getId());
                outputGraph.createEdge(new SetOfTwo<Node>(favoriteLocal, neighbourLocal));
            }
        }

        //simulate number of false positives
        double tp = (double) outputGraph.getNumEdges();
        double fp_mean = npr * tp / (1.0 - npr);

        int fp = poisson.nextInt(fp_mean);

        //create false positives
        int count = 0;
        int collisions = 0;
        while (count < fp) {

            int neighbour_id = uniform.nextIntFromTo(0, nodes.size()-1);
            Node neighbour = nodes.get(neighbour_id);

            if (favoriteNode.equals(neighbour)) {
                continue;
            }

            SetOfTwo<Node> pair = new SetOfTwo<Node>(favoriteNode,neighbour);
            if (outputGraph.containsEdge(pair)) {
                if (++collisions > 500) {
                    throw new RuntimeException("Sampling error!");
                }
                continue;
            }

            Node neighbourLocal = outputGraph.createNode(neighbour.getId());
            outputGraph.createEdge(new SetOfTwo<Node>(favoriteLocal, neighbourLocal));
            count++;

        }

    }

//    /**
//     * Log a warning message.
//     * @param msg The message.
//     */
//    private void warn(String msg) {
//        Logger.getLogger(this.getClass().getCanonicalName()).warning(msg);
//    }

    /**
     * Returns a <code>String</code> containing a summary of the experiments configuration.
     * @return a <code>String</code> containing a summary of the experiments configuration.
     */
    @Override
    public String configSummary() {
        return "\"" + this.getClass().getSimpleName() +
                "\" {sens: " + sensitivity+ "; npr: "+npr+ "}";
    }

}
