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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Probability;
import uk.ac.ncl.cs.harness.io.OutputController;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class ResultDistribution {

    private int[] real;
    private int[] nreal;

    private static int slots;
    public static synchronized void refreshSlots(int num) {
        slots = num;
    }
    public static synchronized boolean acquireSlot() {
        if (slots > 0) {
            slots--;
            return true;
        } else {
            return false;
        }
    }

    public ResultDistribution(int bins) {
        real = new int[bins+1];
        nreal = new int[bins+1];
    }

    public void compute(Graph resultGraph, Graph trueGraph) {
        double bins = real.length-1;

        for (Edge e : resultGraph.getEdges()) {

            double p = probabilityOf(e);
            int index = (int)(bins * p);

            if (trueGraph.contains(e)) {
                real[index]++;
            } else {
                nreal[index]++;
            }

        }

        Set<Edge> missing  = new HashSet<Edge>(trueGraph.getEdges());
        missing.removeAll(resultGraph.getEdges());
        real[0]+= missing.size();

        int n = trueGraph.getNumNodes();
        int all = (n * n - n) / 2;
        int tn = all - (resultGraph.getNumEdges() + missing.size());
        nreal[0]+=tn;
    }

    public void writeOutput() {

        double bins = real.length-1;

        //compile output
        StringBuilder b = new StringBuilder("p\treal\tnreal\n");
        for (int i = 0; i < real.length; i++) {
            double p = (double)i / bins;
            b.append(p).append('\t')
                    .append(real[i]).append('\t')
                    .append(nreal[i]).append('\n');
        }

        //write output to file
        String filename = Thread.currentThread().getName()+"_probDistr.tsv";
        try {
            OutputController.getInstance().writeResults(filename, b.toString());
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Unable to write probability distribution results to file: "
                    +filename, ex);
        }

    }

    private double probabilityOf(Edge e) {
        Probability p = e.getProbability(Probability.MAIN_KEY);
        return p == null ? 0.0 : p.getValue();
    }


    void updateRealAverages(IncrementalAverage[] realAverages) {
        for (int i = 0; i < real.length; i++) {
            if (realAverages[i] == null) {
                realAverages[i] = new IncrementalAverage(real[i]);
            } else {
                realAverages[i].updateWith(real[i]);
            }

        }
    }

    void updateNRealAverages(IncrementalAverage[] nrealAverages) {
        for (int i = 0; i < nreal.length; i++) {
            if (nrealAverages[i] == null) {
                nrealAverages[i] = new IncrementalAverage(nreal[i]);
            } else {
                nrealAverages[i].updateWith(nreal[i]);
            }

        }
    }
}
