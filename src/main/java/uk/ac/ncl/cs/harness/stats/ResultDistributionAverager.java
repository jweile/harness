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
import uk.ac.ncl.cs.harness.io.OutputController;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class ResultDistributionAverager {

    private IncrementalAverage[] real;
    private IncrementalAverage[] nreal;

    public ResultDistributionAverager(int numBins) {
        real = new IncrementalAverage[numBins+1];
        nreal = new IncrementalAverage[numBins+1];
    }

    public synchronized void update(ResultDistribution d) {
        d.updateRealAverages(real);
        d.updateNRealAverages(nreal);
    }

    public void writeOutput(String tag) {

        double bins = real.length-1;

        //compile output
        StringBuilder b = new StringBuilder("p\treal\tnreal\n");
        for (int i = 0; i < real.length; i++) {
            double p = (double)i / bins;
            b.append(p).append('\t')
                    .append(real[i].getAverage()).append('\t')
                    .append(nreal[i].getAverage()).append('\n');
        }

        //write output to file
        String filename = "probDistr_"+tag+".tsv";
        try {
            OutputController.getInstance().writeResults(filename, b.toString());
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Unable to write probability distribution results to file: "
                    +filename, ex);
        }

    }

    public int getNumBins() {
        return real.length-1;
    }

}
