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

import cern.jet.random.Uniform;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import uk.ac.ncl.cs.harness.extsupport.Extension;
import uk.ac.ncl.cs.harness.extsupport.ExtensionProperty;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Node;
import uk.ac.ncl.cs.harness.stats.RandomEngineRegistry;
import uk.ac.ncl.cs.harness.util.SetOfTwo;

/**
 * A basic implementation of the <code>Experiment</code> interface. The experiment
 * introduces errors according to given sensitivity and specifity values.
 *
 * @author Jochen Weile, M.Sc.
 */
@Extension(id="simpleexperiment")
public final class SimpleExperiment implements Experiment {


    /**
     * The experiment's sensitivity.
     */
    private double sensitivity;

    /**
     * The experiment's specificity.
     */
    private double specificity;

    /**
     * A random number generator.
     */
    private Uniform uniform = new Uniform(0.0, 1.0, RandomEngineRegistry.getEngine());


    /**
     * Gets the sensitivity.
     * @return the sensitivity.
     */
    public double getSensitivity() {
        return sensitivity;
    }

    /**
     * Gets the specificity.
     * @return the specificity.
     */
    public double getSpecificity() {
        return specificity;
    }

    /**
     * Sets the sensitivity of the experiment.
     * @param sensitivity The desired sensitivity value.
     */
    @ExtensionProperty(id="sensitivity")
    public void setSensitivity(double sensitivity) {
        this.sensitivity = sensitivity;
    }

    /**
     * Sets the specificity of the experiment.
     * @param specificity The desired specificity value.
     */
    @ExtensionProperty(id="specificity")
    public void setSpecificity(double specificity) {
        this.specificity = specificity;
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

        List<Node> nodes = new ArrayList<Node>();

        for (Node node : trueGraph.getNodes()) {
            nodes.add(outputGraph.createNode(node.getId()));
        }

        for (int i = 0; i < nodes.size(); i++) {
            Node node_i = nodes.get(i);
            for (int j = i ; j < nodes.size(); j++) {
                Node node_j = nodes.get(j);

                SetOfTwo<Node> nodePair = new SetOfTwo<Node>(node_i, node_j);

                /*
                 * P(TP) = sensitivity
                 * P(FP) = 1- specificity
                 */
                double threshold = trueGraph.containsEdge(nodePair) ?
                    sensitivity :
                    1.0 - specificity;

                if (uniform.nextDouble() < threshold) {
                    Edge e = outputGraph.createEdge(nodePair);
                    if (e == null) {
                        warn("Edge creation failed: "+nodePair);
                    }
                }

            }
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
                "\" {sens: " + sensitivity+ "; spec: "+specificity+ "}";
    }

}
