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
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Node;
import uk.ac.ncl.cs.harness.io.OutputController;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class DegreeSample {

    private boolean done;

    private DegreeSample() {

    }

    private static DegreeSample instance;

    public static DegreeSample getInstance() {
        if (instance == null) {
            instance = new DegreeSample();
        }
        return instance;
    }

    public synchronized void sampleDegrees(Graph g) {
        
        if (done) {
            return;
        }

        StringBuilder b = new StringBuilder();
        for (Node node : g.getNodes()) {
            int deg = node.getNeighbours().size();
            b.append(deg).append("\n");
        }

        try {
            OutputController.getInstance().writeResults("degrees.tsv", b.toString());
        } catch (IOException ex) {
            Logger.getLogger(DegreeSample.class.getName()).log(Level.SEVERE, "Unable to write degree results.", ex);
        }

        done = true;
    }



}
