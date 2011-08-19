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

import cern.colt.list.DoubleArrayList;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Probability;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class FastROC {

    public List<Point2D.Double> calculate(Graph integratedGraph, Graph trueGraph, double step) {

        int v = trueGraph.getNumNodes();
        int all = (v * v - v) / 2;
        int baseNeg = all - integratedGraph.getNumEdges();

        Set<Edge> set = new HashSet<Edge>(integratedGraph.getEdges());// O(|E|)
        set.retainAll(trueGraph.getEdges());//set = tp  //O(|E|)
        int baseFN = trueGraph.getNumEdges() - set.size();
        int baseTN = baseNeg - baseFN;

        DoubleArrayList reals = new DoubleArrayList();
        DoubleArrayList nreals = new DoubleArrayList();

        for (Edge e : integratedGraph.getEdges()) {//O(|E|)
            double p = e.getProbability(Probability.MAIN_KEY).getValue();
            if (trueGraph.contains(e)) {
                reals.add(p);
            } else {
                nreals.add(p);
            }
        }

        reals.sort();//O(|E|*log(|E|))
        nreals.sort();

        List<Point2D.Double> rocPoints = new ArrayList<Point2D.Double>();

        int fn = 0, tn = 0;
        for (double t = 0.0; t < 1.0; t += step) {//O(|E|)
            while (fn < reals.size() && reals.get(fn) <= t) {
                fn++;
            }
            double tpr = 1.0 - ((double)(fn + baseFN) / (double)trueGraph.getNumEdges());

            while (tn < nreals.size() && nreals.get(tn) <= t) {
                tn++;
            }
            double fpr = 1.0 - ((double)(tn + baseTN) / (double)(all - trueGraph.getNumEdges()));

            rocPoints.add(new Point2D.Double(fpr, tpr));
        }

        return rocPoints;

    }

}
