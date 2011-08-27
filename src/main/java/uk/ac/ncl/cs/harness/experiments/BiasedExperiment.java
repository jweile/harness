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

import cern.jet.random.Beta;
import cern.jet.random.Uniform;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import uk.ac.ncl.cs.harness.extsupport.Extension;
import uk.ac.ncl.cs.harness.extsupport.ExtensionProperty;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Node;
import uk.ac.ncl.cs.harness.stats.RandomEngineRegistry;
import uk.ac.ncl.cs.harness.util.SetOfTwo;

/**
 *
 * @author Jochen Weile, M.Sc.
 */
@Extension(id="biasedexperiment")
public final class BiasedExperiment implements Experiment {

    /**
     * A random number generator.
     */
    private Uniform uniform = new Uniform(0.0, 1.0, RandomEngineRegistry.getEngine());

    /**
     * The experiment's sensitivity.
     */
    private double sensitivity;

    /**
     * The experiment's false discovery rate.
     */
    private double fdr;
    
    /**
     * false positve rates for each non-edge.
     */
    private Map<SetOfTwo<Node>,Double> fprTable;
    
    /**
     * false negative rate for each edge.
     */
    private Map<SetOfTwo<Node>,Double> fnrTable;

    /**
     * Sets the sensitivity of the experiment.
     * @param sensitivity The desired sensitivity value.
     */
    @ExtensionProperty(id="sensitivity")
    public void setSensitivity(double sensitivity) {
        this.sensitivity = sensitivity;
    }

    /**
     * Sets the false discovery rate of the experiment.
     * @param fdr The desired false discovery rate value.
     */
    @ExtensionProperty(id="fdr")
    public void setFDR(double fdr) {
        this.fdr = fdr;
    }

    /**
     * initializes the bias table if it doesn't exist yet. (otherwise just
     * keeps using the existing one.)
     */
    private void lazyInitBiasTable(Graph trueGraph) {
    
        if (fprTable == null && fnrTable == null) {
        
            //init tables
            fprTable = new HashMap<SetOfTwo<Node>,Double>();
            fnrTable = new HashMap<SetOfTwo<Node>,Double>();
        
            //compute means for TP,FN,TN,FP
            int n = trueGraph.getNumNodes();
            int all = (n * n - n) / 2;
            double tp_mean = sensitivity * (double)trueGraph.getNumEdges();
            double fn_mean = (double)trueGraph.getNumEdges() - tp_mean;
            double fp_mean = fdr * tp_mean / (1.0 - fdr);
            double tn_mean = (double) all - (tp_mean + fn_mean + fp_mean);
            
            //init beta distributions for fnRate and fpRate
            Beta fnrBeta = new Beta(fn_mean+1,tp_mean+1, RandomEngineRegistry.getEngine());
            Beta fprBeta = new Beta(fp_mean+1,tn_mean+1, RandomEngineRegistry.getEngine());
            
            //iterate over all node pairs and sample a fpr/fnr from the above betas
            List<Node> nodes = new ArrayList<Node>(trueGraph.getNodes());
            for (int i = 0; i < nodes.size(); i++) {
                for (int j = i+1; j < nodes.size(); j++) {
                    SetOfTwo<Node> pair = new SetOfTwo<Node>(nodes.get(i),nodes.get(j));
                    if (trueGraph.containsEdge(pair)) {
                        fnrTable.put(pair,fnrBeta.nextDouble());
                    } else {
                        fprTable.put(pair,fprBeta.nextDouble());
                    }
                }
            }
            
        }
        
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
    
        lazyInitBiasTable(trueGraph);

        List<Node> nodes = new ArrayList<Node>();

        for (Node node : trueGraph.getNodes()) {
            nodes.add(outputGraph.createNode(node.getId()));
        }

        for (int i = 0; i < nodes.size(); i++) {
            Node node_i = nodes.get(i);
            for (int j = i+1 ; j < nodes.size(); j++) {
                Node node_j = nodes.get(j);

                SetOfTwo<Node> nodePair = new SetOfTwo<Node>(node_i, node_j);

                double threshold = trueGraph.containsEdge(nodePair) ?
                    1.0 - fnrTable.get(nodePair) :
                    fprTable.get(nodePair);

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
                "\" {sens: " + sensitivity+ "; fdr: "+fdr+ "}";
    }

}
