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

package uk.ac.ncl.cs.harness.integration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uk.ac.ncl.cs.harness.extsupport.Extension;
import uk.ac.ncl.cs.harness.exceptions.ConfigurationException;
import uk.ac.ncl.cs.harness.graph.*;
import uk.ac.ncl.cs.harness.stats.ErrorGauge;

/**
 * This is an implementation of Sam Lycett's probabilistic network integration
 * algorithm. (Publication pending)
 * 
 * @author Jochen Weile, M.Sc.; (algorithm designed by Samantha Lycett, Ph.D.)
 */
@Extension(id="lycett")
public final class LycettIntegration extends IntegrationMethod {

    /**
     * The logarithm of the prior odds that an edge exists in the network.
     */
    private static final double LOG_PRIOR_ODDS = 0.0;

    /**
     * starts the integration process.
     * @throws ConfigurationException if the plugin has been misconfigured.
     */
    @Override
    public void start() throws ConfigurationException {

        if (evidentialGraphs == null || evidentialGraphs.isEmpty()) {
            throw new ConfigurationException("No evidental graphs given!");
        }

        if (goldStandards == null || goldStandards.isEmpty()) {
            throw new ConfigurationException("No gold standards given!");
        }
        
        performUnion();

        for (Graph goldStandard : goldStandards) {
            evaluateForGoldStandard(goldStandard);
        }

        combineGoldStandards();
    }

    /**
     * generates an existence probability for each edge in the integrated graph
     * based on the given gold standard <code>goldStandard</code>.
     * @param goldStandard the gold standard to evaluate against.
     */
    private void evaluateForGoldStandard(Graph goldStandard) {

        Map<Graph,Double> scores = evaluateEvidentals(evidentialGraphs, goldStandard);

        for (Edge edge : getIntegratedGraph().getEdges()) {

            double logOdds = LOG_PRIOR_ODDS;
            for (Graph evidentalGraph : evidentialGraphs) {
                if (evidentalGraph.contains(edge)) {
                    logOdds += scores.get(evidentalGraph);
                }
            }

            double prob = Math.exp(logOdds) / (1.0 + Math.exp(logOdds));

            edge.addProbability(goldStandard.getName(), prob);

        }

    }

    /**
     * generates a score for each given evidential graph on how well it corresponds
     * to the given gold standard
     * @param evidentalGraphs the set of evidential graphs
     * @param goldStandard
     * @return
     */
    private Map<Graph, Double> evaluateEvidentals(Set<Graph> evidentalGraphs, Graph goldStandard) {

        Map<Graph,Double> scores = new HashMap<Graph, Double>();

//        int n = goldStandard.getNumNodes();
//        int maxEdges = (n*n - n) / 2;

        for (Graph evidentalGraph : evidentalGraphs) {

//            int pos = evidentalGraph.getNumEdges();
//            int real = goldStandard.getNumEdges();
//            int nreal = maxEdges - real;
//
//            int tp = intersectionCount(evidentalGraph, goldStandard);
//            int fp = pos - tp;
////            int fn = real - tp;
////            int tn = maxEdges - (tp + fp + fn);
//
//            double sens = ((double)tp) / ((double)real);
//            double unspec = ((double)fp) / ((double)nreal);

            double[] err = ErrorGauge.calculate(evidentalGraph, goldStandard);

            double sens = err[ErrorGauge.TP] / (err[ErrorGauge.TP] + err[ErrorGauge.FN]);
            double unspec = err[ErrorGauge.FP] / (err[ErrorGauge.FP] + err[ErrorGauge.TN]);

            double bayesFactor = sens / unspec;
            
            scores.put(evidentalGraph, Math.log(bayesFactor));

            ratesMap.put(evidentalGraph,err);

        }

        return scores;

    }

    /**
     * combines the results of the single gold standards into a single
     * probability for each edge.
     */
    private void combineGoldStandards() {

        for (Edge edge : getIntegratedGraph().getEdges()) {

            double pComb = 1.0;
            for (Probability p : edge.getProbabilities()) {
                pComb *= 1.0 - p.getValue();
            }
            pComb = 1.0 - pComb;

            edge.addProbability(Probability.MAIN_KEY, pComb);

        }

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

    /**
     * returns a string summarising the integration method's current configuration.
     * @return a string summarising the integration method's current configuration.
     */
    @Override
    public String configSummary() {
        return "\"" + this.getClass().getSimpleName() + "\"";
    }



}
