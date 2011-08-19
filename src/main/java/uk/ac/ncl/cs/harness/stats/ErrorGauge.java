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

package uk.ac.ncl.cs.harness.stats;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Node;
import uk.ac.ncl.cs.harness.util.SetOfTwo;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class ErrorGauge {

    public static final int FP_RATE = 0, FN_RATE = 1,TP = 2,FP = 3,TN = 4,FN = 5;
    
    /**
     *
     * @param graphs
     * @param trueGraph
     * @return
     */
    public static Map<Graph,double[]> calculate(Set<Graph> graphs, Graph trueGraph) {

        Map<Graph,double[]> ratesMap = new HashMap<Graph, double[]>();

        for (Graph graph : graphs) {
            double[] rates = calculate(graph,trueGraph);
            ratesMap.put(graph, rates);
        }

        return ratesMap;
    }


    public static double[] calculate(Graph graph, Graph trueGraph) {
        return calculate(graph,trueGraph.getEdges());
    }

    /**
     * 
     * @param graph
     * @param trueGraph
     * @return
     */
    public static double[] calculate(Graph graph, Collection<Edge> trueEdges) {

        //extract true edges for given subgraph
        Set<Edge> realEdges = new HashSet<Edge>(512);
        for (Edge e : trueEdges) {
            SetOfTwo<Node> ab = e.getConnectedNodes();
            if (graph.contains(ab.getA()) && graph.contains(ab.getB())) {
                realEdges.add(e);
            }
        }


        int n = graph.getNumNodes();
        int all = (n*n - n) / 2;
        int real = realEdges.size();
        int nreal = all - real;

        Set<Edge> set = new HashSet<Edge>(graph.getEdges());
        int posEdges = set.size();

        set.retainAll(realEdges);
        int tp = set.size();
        int fp = posEdges - tp;
        int fn = real - tp;
        int tn = nreal - fp;

        double fpRate = nreal == 0 ? 0.0 : (double)fp / (double)nreal;
        double fnRate = real == 0 ? 0.0 : (double)fn / (double)real;
        
        return new double[] {fpRate,fnRate,tp,fp,tn,fn};
    }

//    public static String measureDeviation(Map<Graph,double[]> trueRates, Map<Graph,double[]> expRates) {
//
//        StringBuilder b = new StringBuilder(
//                "Graph\tfpr\tfnr\tfpr_meas\tfnr_meas\tfpr_dist\tfnr_dist\tfpr_dev\tfnr_dev\n");
//
//        for (Graph g : trueRates.keySet()) {
//            double fpr = trueRates.get(g)[FP_RATE];
//            double fnr = trueRates.get(g)[FN_RATE];
//            double fpr_meas = expRates.get(g)[FP_RATE];
//            double fnr_meas = expRates.get(g)[FN_RATE];
//            double fpr_diff = Math.abs(fpr - fpr_meas);
//            double fnr_diff = Math.abs(fnr - fnr_meas);
//            double fpr_dev = fpr_diff / fpr;
//            double fnr_dev = fnr_diff / fnr;
//
//            b.append(g.getName()).append("\t");
//            b.append(fpr).append("\t");
//            b.append(fnr).append("\t");
//            b.append(fpr_meas).append("\t");
//            b.append(fnr_meas).append("\t");
//            b.append(fpr_diff).append("\t");
//            b.append(fnr_diff).append("\t");
//            b.append(fpr_dev).append("\t");
//            b.append(fnr_dev).append("\n");
//        }
//
//        return b.toString();
//    }



//    public enum Index {
//        FP_RATE(0),FN_RATE(1),TP(3),FP(4),TN(5),FN(6);
//
//        private int index;
//
//        private Index(int index) {
//            this.index = index;
//        }
//
//        public int i() {
//            return index;
//        }
//
//
//    }

}
