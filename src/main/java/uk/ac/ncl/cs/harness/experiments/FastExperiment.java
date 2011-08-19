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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import uk.ac.ncl.cs.harness.extsupport.Extension;
import uk.ac.ncl.cs.harness.extsupport.ExtensionProperty;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Node;
import uk.ac.ncl.cs.harness.stats.RandomEngineRegistry;
import uk.ac.ncl.cs.harness.util.SetOfTwo;

/**
 * A basic implementation of the <code>Experiment</code> interface. The experiment
 * introduces errors according to given sensitivity and FP-rate values.
 *
 * @author Jochen Weile, M.Sc.
 */
@Extension(id="fastexperiment")
public final class FastExperiment implements Experiment {

    /**
     * The experiment's sensitivity.
     */
    private double sensitivity;

    /**
     * The experiment's false positive rate.
     */
    private double fpr;

    /**
     * A poisson distributed random number generator.
     */
    private Poisson poisson;
    /**
     * A uniform random number generator.
     */
    private Uniform uniform;

    public FastExperiment() {
//        MersenneTwister r = new MersenneTwister(RandomSeeds.getInstance().nextSeed());
        poisson = new Poisson(250, RandomEngineRegistry.getEngine());
        uniform = new Uniform(RandomEngineRegistry.getEngine());
    }

    /**
     * Gets the sensitivity.
     * @return the sensitivity.
     */
    public double getSensitivity() {
        return sensitivity;
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
     * gets the False positive rate
     */
    public double getFpr() {
        return fpr;
    }

    /**
     * sets the false positive rate.
     * @throws IllegalArgumentException if preconditions are not met.
     */
    @ExtensionProperty(id="fpr")
    public void setFpr(double fpr) throws IllegalArgumentException {
        if (fpr < 0.0 || fpr >= 1.0) {
            throw new IllegalArgumentException(
                    "Precondition violation: 0 <= fpr < 1");
        }
        this.fpr = fpr;
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

        List<Node> nodes = new ArrayList<Node>(trueGraph.getNumNodes());

        //copy nodes
        for (Node node : trueGraph.getNodes()) {
            nodes.add(outputGraph.createNode(node.getId()));
        }

        for (Edge e: trueGraph.getEdges()) {
            double r = uniform.nextDouble();
            if (r < sensitivity) {
                outputGraph.createEdge(e.getConnectedNodes());
            }
        }

        //simulate number of false positives
        double tp = (double) outputGraph.getNumEdges();
        double fp_mean = fpr * tp / (1.0 - fpr);

        int fp = poisson.nextInt(fp_mean);
//        while (Math.abs((double)fp - fp_mean) > fp_mean) {
//            fp = pd.nextInt();//try not to deviate more than 100%
//            Logger.getLogger(this.getClass().getName())
//                        .fine("Resampling!");
//        }


        //create false positives
        int count = 0;
        int collisions = 0;
        while (count < fp) {

            int id_a = uniform.nextIntFromTo(0, nodes.size()-1);
            int id_b = uniform.nextIntFromTo(0, nodes.size()-1);

            Node a = nodes.get(id_a);
            Node b = nodes.get(id_b);
            
            if (a == b) {
//                Logger.getLogger(this.getClass().getName())
//                        .fine("Self-loop sampled!");
                continue;
            }

            SetOfTwo<Node> ab = new SetOfTwo<Node>(a,b);
            if (outputGraph.containsEdge(ab)) {
//                Logger.getLogger(this.getClass().getName())
//                        .fine("Sample collision!");
                if (++collisions > 500) {
                    throw new RuntimeException("Sampling error!");
                }
                continue;
            }

            outputGraph.createEdge(ab);
            count++;

        }

    }

    /**
     * Log a warning message.
     * @param msg The message.
     */
    private void warn(String msg) {
        Logger.getLogger(this.getClass().getCanonicalName()).warning(msg);
    }

    /**
     * Returns a <code>String</code> containing a summary of the experiments configuration.
     * @return a <code>String</code> containing a summary of the experiments configuration.
     */
    @Override
    public String configSummary() {
        return "\"" + this.getClass().getSimpleName() +
                "\" {sens: " + sensitivity+ "; fpr: "+fpr+ "}";
    }

}
