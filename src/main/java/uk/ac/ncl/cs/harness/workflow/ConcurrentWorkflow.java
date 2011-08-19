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

package uk.ac.ncl.cs.harness.workflow;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import uk.ac.ncl.cs.harness.exceptions.ConfigurationException;
import uk.ac.ncl.cs.harness.io.OutputController;
import uk.ac.ncl.cs.harness.stats.ResultDistributionAverager;
import uk.ac.ncl.cs.harness.stats.ErrorRateLossAverager;
import uk.ac.ncl.cs.harness.stats.ResultLossAverager;
import uk.ac.ncl.cs.harness.util.IdService;
import uk.ac.ncl.cs.harness.workflow.variables.Variable;

/**
 * Executes a workflow protocol.
 * @author Jochen Weile, M.Sc.
 */
public class ConcurrentWorkflow {

    private int numCpuSlots;

    private String[] variableIds;

    private StringBuilder results = new StringBuilder();

    /**
     *
     * @param numCpuSlots Number of CPUs to use at the same time.
     */
    public ConcurrentWorkflow(int numCpuSlots) {
        this.numCpuSlots = numCpuSlots;
    }

    

    /**
     * runs a given workflow protocol
     * @param protocol the protocol to execute.
     * @throws ConfigurationException if the protocol is misconfigured.
     */
    public void run(Protocol protocol) throws ConfigurationException {

        Map<String,Variable> vars = protocol.getVariables();
        
        initResultCache(vars);

        if (vars != null && !vars.isEmpty()) {
            String[] varIds = vars.keySet().toArray(new String[vars.keySet().size()]);
            vary(0, varIds, protocol);
        } else {
            performParallelCycles(protocol);
        }

        //Output:
        String resultString = results.toString();
        Logger.getLogger(this.getClass().getName()).info(resultString);
        try {
            OutputController.getInstance().writeResults("loss.tsv", resultString);
        } catch (IOException ex) {
            Logger.getLogger(ConcurrentWorkflow.class.getName()).log(Level.SEVERE,
                    "Unable to save results!", ex);
        }

    }

    private void initResultCache(Map<String,Variable> vars) {
        variableIds = new String[vars.size()];
        int i = 0;
        for (String varId : vars.keySet()) {
            variableIds[i++] = varId;
            results.append(varId).append("\t");
        }
        results.append("Real loss\tNot real loss\tRate loss")
               .append("\n");
    }



    /**
     * This is a recursive method that iterates over the variable with the index
     * corresponding to the current recursion depth.
     * @param index the current recursion depth (corresponding to the active variable index)
     * @param varIds an array of variable ids.
     * @param protocol the currently executing protocol.
     * @throws ConfigurationException if the protocol is misconfigured.
     */
    private void vary(int index, String[] varIds, Protocol protocol) throws ConfigurationException {

        Variable currLevelVar = protocol.getVariables().get(varIds[index]);
        currLevelVar.reset();

        boolean hasNext = true;
        while (hasNext) {

            if (index < varIds.length - 1) {
                vary(index + 1, varIds, protocol);
            } else if (index == varIds.length -1) {
                performParallelCycles(protocol);
            }

            if (currLevelVar.canStep()) {
                currLevelVar.step();
            } else {
                hasNext = false;
            }
        }

    }


    /**
     * Performs one integration cycle. Generates a "true" graph, uses the specified
     * experiments to derive evidential graphs and goldstandards feeds them to the
     * integration method and evaluates the result.
     *
     * @param protocol the currently executing protocol.
     * @throws ConfigurationException if the protocol is misconfigured.
     */
    private void performParallelCycles(Protocol protocol) throws ConfigurationException {

        Semaphore slots = new Semaphore(numCpuSlots);

        ResultCallback callback = new ResultCallback();

//        ResultLossAverager losses = new ResultLossAverager();
//        callback.setResultLossAverager(losses);
//
//        ResultDistributionAverager averageResultDistribution = new ResultDistributionAverager(100);
//        callback.setResultDistributionAverager(averageResultDistribution);
//
//        ErrorRateLossAverager erLossAverager = new ErrorRateLossAverager();
//        callback.setRateLossAverager(erLossAverager);

        List<Throwable> errors = Collections.synchronizedList(new LinkedList<Throwable>());

        for (int i = 0; i < protocol.getCycleReplicas(); i++) {

            if (errors.size() > 0) {
                throw new RuntimeException("Error in workflow thread!", errors.get(0));
            }

            try {
                slots.acquire();
            } catch (InterruptedException ex) {
                throw new RuntimeException(
                        "Master thread interrupted. Report this as a bug.",ex);
            }

            WorkflowThread t = new WorkflowThread(protocol, slots, errors, callback, "WorkflowThread#"+IdService.getUniqueId());
            t.start();

        }

        //wait until all threads are done.
        while (slots.availablePermits() < numCpuSlots) {
            try {
                if (errors.size() > 0) {
                    throw new RuntimeException("Error in workflow thread!", errors.get(0));
                }
                Thread.sleep(200);//FIXME find some better way of waiting here
            } catch (InterruptedException ex) {
                throw new RuntimeException(
                        "Master thread interrupted. Report this as a bug.",ex);
            }
        }

        if (errors.size() > 0) {
            throw new RuntimeException("Error in workflow thread!", errors.get(0));
        }

        //output results
        callback.getResultDistributionAverager().writeOutput(protocol.getVariableValuesAsString());

        cacheResults(protocol, 
                callback.getResultLossAverager().getRealLoss(),
                callback.getResultLossAverager().getNRealLoss(),
                callback.getRateLossAverager().getAverage());
    }

    


    private void cacheResults(Protocol protocol, double realLoss, double nrealLoss, double rateLoss) {

        for (String varId : variableIds) {
            results.append(protocol.getVariables().get(varId).getCurrentValue())
                    .append("\t");
        }
        results.append(String.format("%.8f",realLoss))
               .append("\t")
               .append(String.format("%.8f",nrealLoss))
               .append("\t")
               .append(String.format("%.8f",rateLoss))
               .append("\n");
        
    }

}
