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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import uk.ac.ncl.cs.harness.exceptions.ConfigurationException;
import uk.ac.ncl.cs.harness.extsupport.Extension;
import uk.ac.ncl.cs.harness.extsupport.ExtensionProperty;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Probability;
import uk.ac.ncl.cs.harness.stats.ErrorGauge;

/**
 * This is an implementation of the integration method described in
 * <a href="http://www.sciencemag.org/content/306/5701/1555.abstract">
 * Lee et al. 2006</a>.
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
@Extension(id="lee")
public class LeeIntegration extends IntegrationMethod {

    /**
     * The mysterious dependency weight value.
     */
    private double dValue;

    /**
     * Score quantification threshold.
     */
    private double threshold;

    /**
     * gets the dependency weight value.
     * @return the dependency weight value.
     */
    public double getdValue() {
        return dValue;
    }

    /**
     * sets the dependency weight value.
     * @param dValue the dependency weight value.
     */
    @ExtensionProperty(id="dValue")
    public void setdValue(double dValue) {
        this.dValue = dValue;
    }

    /**
     * gets the Score quantification threshold.
     * @return Score quantification threshold.
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * sets the Score quantification threshold.
     * @param threshold Score quantification threshold.
     */
    @ExtensionProperty(id="threshold")
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }


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

        if (goldStandards.size() > 1) {
            Logger.getLogger(this.getClass().getName())
                    .warning("More than one gold standard assigned to Lee integration!");
        }
        
        performUnion();

        Graph goldStandard = goldStandards.iterator().next();
        Map<Graph,Double> llScores = evaluateGraphs(evidentialGraphs, goldStandard);

        Map<Edge,Double> wsScores = calculateWeightedSums(integratedGraph, llScores);

        quantify(integratedGraph, wsScores, threshold);

    }

    /**
     * returns a string summarising the integration method's current configuration.
     * @return a string summarising the integration method's current configuration.
     */
    @Override
    public String configSummary() {
        return "\"" + this.getClass().getSimpleName() +
                "\" {dValue: " + dValue + "}";
    }

    /**
     * Calculates the LLS scores for each evidential graph and returns them in
     * a map object.
     * @param evidentialGraphs the graphs.
     * @return a map linking the graphs to their scores.
     */
    private Map<Graph, Double> evaluateGraphs(Set<Graph> evidentialGraphs,
            Graph goldStandard) {

        Map<Graph, Double> scores = new HashMap<Graph, Double>();

        int n = goldStandard.getNumNodes();
        int potEdges = (n*n - n) / 2;

        for (Graph g: evidentialGraphs) {

            double[] err = ErrorGauge.calculate(g,goldStandard);

//            int ple = intersectionCount(g, goldStandard);//TP
//            int nple = g.getNumEdges() - ple;//FP
            double numerator = err[ErrorGauge.TP] / err[ErrorGauge.FP];

//            int pl = goldStandard.getNumEdges();//R
//            int npl = potEdges - pl;//NR
            double denominator = (err[ErrorGauge.TP]+err[ErrorGauge.FN]) / (err[ErrorGauge.TN]+err[ErrorGauge.FP]);

            double lls = Math.log(numerator / denominator);

            scores.put(g, lls);
            
            ratesMap.put(g, err);

        }

        return scores;
    }

//    /**
//     * returns the cardinality of the intersection between the sets of edges
//     * in the two given graphs
//     * @param g1 a graph
//     * @param g2 another graph
//     * @return the cardinality of the intersection between the sets of edges
//     */
//    private int intersectionCount(Graph g1, Graph g2) {
//
//        Set<Edge> intersection = new HashSet<Edge>(g1.getEdges());
//        intersection.retainAll(g2.getEdges());
//
//        return intersection.size();
//    }

    private Map<Edge, Double> calculateWeightedSums(Graph integratedGraph, 
            final Map<Graph, Double> llScores) {

        Map<Edge, Double> wsScores = new HashMap<Edge, Double>();

        List<Graph> evidences = new ArrayList<Graph>(llScores.keySet());

        Collections.sort(evidences, new Comparator<Graph>() {
            @Override
            public int compare(Graph o1, Graph o2) {
                double s1 = llScores.get(o1);
                double s2 = llScores.get(o2);

                if (s1 > s2) {
                    return -1;
                } else if (s1 == s2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        
        for (Edge edge : integratedGraph.getEdges()) {

            double ws = 0.0;

            for (Graph evidence : evidences) {
                if (evidence.contains(edge)) {

                    if (ws == 0.0) {//first evidence
                        ws += llScores.get(evidence);
                    } else {//all other evidence
                        ws += llScores.get(evidence) / dValue;
                    }

                }
            }

            wsScores.put(edge, ws);

        }

        return wsScores;
    }

    private void quantify(Graph integratedGraph, Map<Edge, Double> wsScores, double threshold) {

        for (Edge e : integratedGraph.getEdges()) {
            if (wsScores.get(e) > threshold) {
                e.addProbability(Probability.MAIN_KEY, 1.0);
            } else {
                e.addProbability(Probability.MAIN_KEY, 0.0);
            }
        }

    }

}
