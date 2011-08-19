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

import java.util.Map;
import uk.ac.ncl.cs.harness.graph.Graph;
import static uk.ac.ncl.cs.harness.stats.ErrorGauge.*;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class ErrorRateLossAverager {

    private IncrementalAverage average;

    public void update(Map<Graph,double[]> trueRates, Map<Graph,double[]> expRates) {

        double loss = 0.0;

        for (Graph g : trueRates.keySet()) {

            double fpr = trueRates.get(g)[FP_RATE];
            double fnr = trueRates.get(g)[FN_RATE];

            double fpr_meas = expRates.get(g)[FP_RATE];
            double fnr_meas = expRates.get(g)[FN_RATE];

            double fpr_diff = fpr - fpr_meas;
            double fnr_diff = fnr - fnr_meas;

            loss += fpr_diff * fpr_diff + fnr_diff * fnr_diff;

        }

        double n = trueRates.size() * 2;
        loss = Math.sqrt(loss / n);

        if (average == null) {
            average = new IncrementalAverage(loss);
        } else {
            average.updateWith(loss);
        }

    }

    public double getAverage() {
        return average.getAverage();
    }

}
