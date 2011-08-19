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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.stats.IncrementalAverage;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class RateAverager {

    private static final int FPR_INDEX = 0;
    private static final int FNR_INDEX = 1;

    private IncrementalAverage[][] averages;

    private Map<Graph,Integer> graph2Index = new HashMap<Graph, Integer>();

    public RateAverager(Set<Graph> graphs) {
        averages = new IncrementalAverage[2][graphs.size()];

        int i = 0;
        for (Graph g : graphs) {
            graph2Index.put(g,i++);
        }
    }

    public void update(Map<Graph,ErrorRates> rates) {
        for (Graph graph : rates.keySet()) {
            int graphIndex = graph2Index.get(graph);
            double fpr = rates.get(graph).getFalsePositiveRate();
            double fnr = rates.get(graph).getFalseNegativeRate();
            __update(FPR_INDEX,graphIndex, fpr);
            __update(FNR_INDEX,graphIndex, fnr);
        }
    }

    private void __update(int rateIndex, int graphIndex, double d) {
        if (averages[rateIndex][graphIndex] == null) {
            averages[rateIndex][graphIndex] = new IncrementalAverage(d);
        } else {
            averages[rateIndex][graphIndex].updateWith(d);
        }
    }

    public double getFprAverage(Graph graph) {
        int graphIndex = graph2Index.get(graph);
        return averages[FPR_INDEX][graphIndex].getAverage();
    }

    public double getFnrAverage(Graph graph) {
        int graphIndex = graph2Index.get(graph);
        return averages[FNR_INDEX][graphIndex].getAverage();
    }
}
