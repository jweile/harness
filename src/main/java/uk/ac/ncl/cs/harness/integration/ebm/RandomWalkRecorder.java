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

package uk.ac.ncl.cs.harness.integration.ebm;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.ac.ncl.cs.harness.graph.Graph;

/**
 * Records a random walk in the MCMC integrator
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
@Deprecated
public class RandomWalkRecorder {

    /**
     * Assigns an ordinal number to each graph. Used internally to compute associated
     * indices in storage arrays. (E.g if there are four graphs each of them gets a
     * unique number between 0 and 3.
     */
    private Map<Graph,Integer> graphOrder = new HashMap<Graph, Integer>(8);

    /**
     * The data content. A list of arrays, each of which contains two fields for each graph
     * plus one. Stores the false positive and false negative rates for each graph in each cycle.
     * The last field is used to store the likelihood of teh cycle.
     * E.g. given two graphs "0" and "1" each array contains
     * [fp_0, fn_0, fp_1, fn_1, likelihood]
     */
    private List<double[]> data = new ArrayList<double[]>(1024);

    private int skip = 1;

    private int burnIn;

    public RandomWalkRecorder(int burnIn) {
        this.burnIn = burnIn;
    }

    

//    /**
//     * array storing the means for each value.
//     */
//    private double[] means;

    /**
     * Records a states error rates and likelihood.
     * @param rates map linking each graph to its error rate object.
     * @param logLikelihoodAnalogue logarithm of score proportional to state likelihood
     */
    public void record(Map<Graph,ErrorRates> rates, double logLikelihoodAnalogue) {

        //if this is the first execution, initialize required fields.
        lazyInit(rates.keySet());

//        double n = data.size();
//        double factor = n / (n+1);

        // create a new entry array
        double[] entry = new double[rates.size() * 2 + 1];

        //fill in the values for each graph.
        for (Graph g : rates.keySet()) {
            int index_base = graphOrder.get(g) * 2;
            
            double fpr = rates.get(g).getFalsePositiveRate();
            int fp_index = index_base + RateType.FP.offset();
            entry[fp_index] = fpr;
//            if (Double.isNaN(means[fp_index])) {
//                means[fp_index] = fpr;
//            } else {
//                means[fp_index] = factor * (means[fp_index] + (fpr / n));
//            }

            double fnr = rates.get(g).getFalseNegativeRate();
            int fn_index = index_base + RateType.FN.offset();
            entry[fn_index] = fnr;
//            if (Double.isNaN(means[fn_index])) {
//                means[fn_index] = fnr;
//            } else {
//                means[fn_index] = factor * (means[fn_index] + (fnr / n));
//            }

        }

        //fill in the likelihood
        entry[entry.length -1] = logLikelihoodAnalogue;

        //store entry
        data.add(entry);
    }

    /**
     * Initializes the graph order field if not already done.
     * @param the set of graphs to work with.
     */
    private void lazyInit(Set<Graph> graphs) {

//        means = new double[graphs.size() * 2];
//        Arrays.fill(means, Double.NaN);

        if (graphOrder.isEmpty()) {
            int i = 0;
            for (Graph g : graphs) {
                graphOrder.put(g,i++);
            }
        }
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getSkip() {
        return skip;
    }

    
    

    public DoubleArrayList extractSeries(Graph graph, RateType rateType) {

        int valueIndex = graphOrder.get(graph) * 2 + rateType.offset();

        DoubleArrayList series = new DoubleArrayList();
        for (int i = burnIn; i < data.size(); i += skip) {
            series.add(data.get(i)[valueIndex]);
        }

        return series;
    }

    public double getMean(Graph graph, RateType rateType) {

        DoubleArrayList series = extractSeries(graph, rateType);

        return Descriptive.mean(series);
    }
    
    public double getAutocorrelation(Graph graph, RateType rateType, int lag) {
        DoubleArrayList series = extractSeries(graph, rateType);
        
        double mean = Descriptive.mean(series);
        double var = Descriptive.moment(series,2,mean);
        
        return Descriptive.autoCorrelation(series, lag, mean, var);
        
    }


    
//    /**
//     *
//     * @param graph
//     * @param rateType
//     * @param numBins
//     * @return
//     */
//    public double[] getDistribution(Graph graph, RateType rateType, int numBins) {
//
//        double binWidth = 1.0 / (double)numBins;
//
//        double[] distribution = new double[numBins+1];
//
//        int valueIndex = graphOrder.get(graph) * 2 + rateType.getIndex();
//        for (double[] entry : data) {
//            double value = entry[valueIndex];
//
//            int binIndex = (int) Math.floor(value / binWidth);
//
//            double logLikelihood = entry[entry.length - 1];
//
//            //FIXME: Precision loss! No other way of doing it though!
//            distribution[binIndex] += Math.exp(logLikelihood);
//        }
//
//        return distribution;
//    }



    /**
     * Helper class defines an array index offset for each type of error rate.
     */
    public enum RateType {
        FP(0),FN(1);

        private int offset;

        private RateType(int index) {
            this.offset = index;
        }

        public int offset() {
            return offset;
        }

    }
}
