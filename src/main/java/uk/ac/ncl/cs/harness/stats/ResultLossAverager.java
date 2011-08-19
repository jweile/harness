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

import java.util.HashSet;
import java.util.Set;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Probability;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class ResultLossAverager {

    private IncrementalAverage real;
    private IncrementalAverage nreal;

    public void update(Graph trueGraph, Graph integratedGraph) {

        double realLoss = 0.0;
        double nrealLoss = 0.0;

        for (Edge e : integratedGraph.getEdges()) {
            Probability pObj = e.getProbability(Probability.MAIN_KEY);
            double p = pObj == null ? 0.0 : pObj.getValue();
            if (trueGraph.contains(e)) {
                double diff = 1.0 - p;
                realLoss += diff * diff;
            } else {
                nrealLoss += p * p;
            }
        }

        Set<Edge> fns = new HashSet<Edge>(trueGraph.getEdges());
        fns.removeAll(integratedGraph.getEdges());
        realLoss += fns.size();

        int n = trueGraph.getNumNodes();
        int all = ((n * n) - n) / 2;
        int nrealAll = all - trueGraph.getNumEdges();

        realLoss = Math.sqrt(realLoss / (double)trueGraph.getNumEdges());
        nrealLoss = Math.sqrt(nrealLoss / (double)nrealAll);

        updateReal(realLoss);
        updateNReal(nrealLoss);

    }

//    public static double calculate(Graph trueGraph, Graph integratedGraph) {
//
//        double ql = 0.0;
//
//        for (Edge e : integratedGraph.getEdges()) {
//            double r = trueGraph.contains(e) ? 1.0 : 0.0;
//            double p = e.getProbability(Probability.MAIN_KEY).getValue();
//            double d = r - p;
//            ql += d * d;
//        }
//
//        Set<Edge> set = new HashSet<Edge>(trueGraph.getEdges());
//        set.removeAll(integratedGraph.getEdges());
//
//        ql += (double)set.size();
//
//        double n = (double) trueGraph.getNumNodes();
//        double numPotEdges = ((n * n) - n) / 2.0;
//        ql /= numPotEdges;
//
//        return ql;
//    }

    private synchronized void updateReal(double d) {
        if (real == null) {
            real = new IncrementalAverage(d);
        } else {
            real.updateWith(d);
        }
    }

    private synchronized void updateNReal(double d) {
        if (nreal == null) {
            nreal = new IncrementalAverage(d);
        } else {
            nreal.updateWith(d);
        }
    }

    public double getRealLoss() {
        return real.getAverage();
    }

    public double getNRealLoss() {
        return nreal.getAverage();
    }


}
