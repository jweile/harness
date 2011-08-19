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

import uk.ac.ncl.cs.harness.exceptions.ConfigurationException;
import uk.ac.ncl.cs.harness.extsupport.Extension;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Probability;

/**
 * The empirical thresholded integration annotates each edge in the integrated
 * graph with 0 or 1 depending whether more or less than half the evidences
 * vouch for it.
 * 
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
@Extension(id="empThr")
public class EmpiricalThresholdedIntegration extends IntegrationMethod {

     /**
     * Starts the integration process.
     * @throws ConfigurationException if the integration method has been
     * misconfigured.
     */
    @Override
    public void start() throws ConfigurationException {

        if (evidentialGraphs == null || evidentialGraphs.isEmpty()) {
            throw new ConfigurationException("No evidental graphs given!");
        }

        performUnion();

        double numEvGs = (double)evidentialGraphs.size();

        for (Edge e : integratedGraph.getEdges()) {
            double count = 0.0;
            for (Graph evg : evidentialGraphs) {
                if (evg.contains(e)) {
                    count += 1.0;
                }
            }
            double prob = (double)count <= (double)numEvGs / 2.0 ? 0.0: 1.0;
            e.addProbability(Probability.MAIN_KEY, prob);
        }

        invalidateRatesMap();

    }

    /**
     * Returns a <code>String</code> containing a summary of the extension's
     * configuration.
     * @return a <code>String</code> containing a summary of the extension's
     * configuration.
     */
    @Override
    public String configSummary() {
        return "\"" + this.getClass().getSimpleName() + "\"";
    }

    private void invalidateRatesMap() {

        double[] inv = new double[6];
        java.util.Arrays.fill(inv, Double.NaN);

        for (Graph g : evidentialGraphs) {
            ratesMap.put(g, inv);
        }

    }

}
