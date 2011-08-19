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

import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ncl.cs.harness.extsupport.Extension;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Node;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Probability;
import uk.ac.ncl.cs.harness.integration.ebm.ErrorRates;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
@Extension(id="naiveEBM")
public class NaiveEBM extends IntegrationMethod {

    private Map<Graph,ErrorRates> errorRates;

    private Map<Edge,Double> probabilities = new HashMap<Edge,Double>(1000);

    @Override
    public void start() {
        performUnion();

        initErrorRates();

        double prior = 2.0 / ((double)integratedGraph.getNumNodes() - 1.0);

        for (int i = 0; i < 1000; i++) {

            updateProbabilities(prior);

            updateErrorRates();

            logErrorRates();
        }

        assignFinalValues();

        invalidateRatesMap();

    }

//    private void generateTopologicalConsensus() {
//        for (Node node : evidentialGraphs.iterator().next().getNodes()) {
//            integratedGraph.createNode(node.getId());
//        }
//
//        for (Graph eviGraph : evidentialGraphs) {
//            for (Edge edge : eviGraph.getEdges()) {
//                if (!integratedGraph.contains(edge)) {
//                    integratedGraph.createEdge(edge.getConnectedNodes());
//                }
//            }
//        }
//    }

    private void updateProbabilities(double prior) {

        double logPriorOdds = Math.log(prior / (1.0 - prior));

        for (Edge edge : integratedGraph.getEdges()) {

            double k = logPriorOdds;

            for (Graph eviGraph : evidentialGraphs) {

                ErrorRates bf = errorRates.get(eviGraph);
                if (eviGraph.contains(edge)) {
                    k += bf.getLogPositiveBayesFactor();
                } else {
                    k += bf.getLogNegativeBayesFactor();
                }

            }

            double p = Math.exp(k) / (1.0 + Math.exp(k));
            probabilities.put(edge,p);
        }
    }

    private void updateErrorRates() {

        double pSum = 0.0;
        for (Entry<Edge,Double> pEntry : probabilities.entrySet()) {
            pSum += pEntry.getValue();
        }
        
        int n = integratedGraph.getNumNodes();
        int maxEdges = (n * n - n) / 2;
        double pInvSum = maxEdges - pSum;

        for (Graph eviGraph : evidentialGraphs) {

            double localPSum = 0.0;
            for (Edge edge : eviGraph.getEdges()) {
                localPSum += probabilities.get(edge);
            }

            double localPInvSum = (double)eviGraph.getNumEdges() - localPSum;

            double pFP = localPInvSum / pInvSum;
            double pFN = localPSum / pSum;

            errorRates.put(eviGraph, new ErrorRates(pFP,pFN));
        }
    }

    private void initErrorRates() {

        errorRates = new HashMap<Graph,ErrorRates>(evidentialGraphs.size());
        
        for (Graph eviGraph : evidentialGraphs) {
            errorRates.put(eviGraph, new ErrorRates(Math.random(), Math.random()));
        }

    }

    private void logErrorRates() {
        Logger logger = Logger.getLogger(this.getClass().getName());
        StringBuilder b = new StringBuilder("Rates:\t");
        for (Graph eviGraph : evidentialGraphs) {
            ErrorRates rates = errorRates.get(eviGraph);
            b.append(String.format("%.8f",rates.getFalsePositiveRate()))
                    .append("\t")
                    .append(String.format("%.8f",rates.getFalseNegativeRate()))
                    .append("\t");
        }
        logger.log(Level.INFO, b.toString());
    }

    @Override
    public String configSummary() {
        return "\"" + this.getClass().getSimpleName() + "\"";
    }

    private void assignFinalValues() {
        for (Edge edge : integratedGraph.getEdges()) {
            edge.addProbability(Probability.MAIN_KEY, probabilities.get(edge));
        }
    }



    private void invalidateRatesMap() {

        double[] inv = new double[6];
        java.util.Arrays.fill(inv, Double.NaN);

        for (Graph g : evidentialGraphs) {
            ratesMap.put(g, inv);
        }

    }

}

