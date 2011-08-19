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

package uk.ac.ncl.cs.harness;

import java.util.Map;
import uk.ac.ncl.cs.harness.workflow.ConcurrentWorkflow;
import uk.ac.ncl.cs.harness.workflow.Protocol;
import uk.ac.ncl.cs.harness.workflow.variables.Variable;

/**
 * Progress monitoring aspect.
 * @author jweile
 */
public aspect Progress {

    private int currCycle = 0;

    private int oldPercent = 0;

    private long startTime;

    before() : call(* ConcurrentWorkflow.run(Protocol)) {
        startTime = System.currentTimeMillis();
    }

    after() : execution(* ConcurrentWorkflow.run(Protocol)) {
        System.out.println("Total time: "+formatTime(System.currentTimeMillis() - startTime));
    }

    before() : call(* ConcurrentWorkflow.run(Protocol)) {
        currCycle = 0;
        oldPercent = 0;
        numCycles = -1;
    }

    after(Protocol protocol) : call(* ConcurrentWorkflow.performParallelCycles(Protocol)) && args(protocol) {
        updateProgress(protocol);
    }

    private void updateProgress(Protocol protocol) {
        currCycle++;
        int nc = getOrInferNumCycles(protocol.getVariables());

        int currPercent = 100 * currCycle / nc;

        if (currPercent > oldPercent) {
            long elapsed = System.currentTimeMillis() - startTime;
            long remaining = (elapsed * 100L / (long)currPercent) - elapsed;
            System.out.println("Progress: "+currPercent+"%. ETA: "+formatTime(remaining));
            oldPercent = currPercent;
        }
    }

    private int numCycles = -1;
    private int getOrInferNumCycles(Map<String, Variable> vars) {
        if (numCycles == -1) {
            int nc = 1;
            if (vars != null) {
                for (Variable var : vars.values()) {
                    nc *= var.getNumTotalIterations();
                }
            }
            numCycles = nc;
        }
        return numCycles;
    }

    public String formatTime(long millis) {
        String[] symbols = {"wks","days","hrs","mins","secs"};
        long[] multiples = {7L,24L,60L,60L};
        long[] units = new long[5];
        units[4] = 1000;
        for (int i = 3; i >= 0; i--)
            units[i] = units[i+1] * multiples[i];

        StringBuilder b = new StringBuilder();
        long rest = millis;
        for (int i = 0; i < 5; i++) {
            int num = (int)(rest / units[i]);
            if (num > 0)
                b.append(num).append(symbols[i]).append(" ");
            rest %= units[i];
        }
        if (b.length() == 0) {
            b.append("Due");
        }
        return b.toString();
    }

   
}
