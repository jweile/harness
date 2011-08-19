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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import uk.ac.ncl.cs.harness.exceptions.ConfigurationException;
import uk.ac.ncl.cs.harness.experiments.Experiment;
import uk.ac.ncl.cs.harness.extsupport.ExtensionRegistry;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.populators.GraphPopulator;
import uk.ac.ncl.cs.harness.integration.IntegrationMethod;
import uk.ac.ncl.cs.harness.io.OutputController;
import uk.ac.ncl.cs.harness.stats.ResultDistributionAverager;
import uk.ac.ncl.cs.harness.stats.DegreeSample;
import uk.ac.ncl.cs.harness.stats.ErrorGauge;
import uk.ac.ncl.cs.harness.stats.ErrorRateLossAverager;
import uk.ac.ncl.cs.harness.stats.ResultLossAverager;
import uk.ac.ncl.cs.harness.stats.RandomEngineRegistry;
import uk.ac.ncl.cs.harness.stats.ResultDistribution;

/**
 * This thread executes a single workflow according to a given protocol.
 * It is initiated with a protocol instance, a semaphore (used to signal
 * the thread's completion), a list of exceptions, which is used to 
 * communicate any errors to the master control thread and a list of
 * double-precision floating point values, which is used to report the 
 * results.
 * 
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class WorkflowThread extends Thread {

    /**
     * The protocol describing the details of the workflow.
     */
    private Protocol protocol;

    /**
     * A semaphore that is used to signal the thread's termination.
     */
    private Semaphore semaphore;

    private ResultCallback callback;

    /**
     * A list of throwables that is used to communicate errors to the master thread.
     */
    private List<Throwable> thrown;


    /**
     * The constructor.
     */
    public WorkflowThread(Protocol protocol, Semaphore semaphore, 
		List<Throwable> thrown, ResultCallback callback, String name) {

        super(name);

        setDaemon(true);

        this.protocol = protocol;
        this.semaphore = semaphore;
        this.thrown = thrown;
        this.callback = callback;

    }

    
    /**
     * Executes the workflow.
     */
    @Override
    public void run() {
        try {
            //generate true graph
            Graph trueGraph = generateTemplateGraph();
            //make node degree histogram
            DegreeSample.getInstance().sampleDegrees(trueGraph);

            //perform experiments on true graph
            Set<Graph> evidentialGraphs = performExperiments(trueGraph, false);
            //measure error rates in experiments and prepare empty map for later results
            Map<Graph,double[]> trueRates = ErrorGauge.calculate(evidentialGraphs,trueGraph);
            Map<Graph,double[]> measuredRates = new HashMap<Graph, double[]>();

            //generate gold standards if nessessary
            Set<Graph> goldStandards = requiresGoldStandards(protocol) ?
                performExperiments(trueGraph, true) :
                Collections.EMPTY_SET;

            //run integration algorithm
            Graph integratedGraph = performIntegration(evidentialGraphs, goldStandards, measuredRates);

            //EVALUATION:
            //calculate loss over all edges
            ResultLossAverager losses = callback.getResultLossAverager();
            losses.update(trueGraph, integratedGraph);

            //analyze edge probability distribution
            ResultDistributionAverager avResultDistr = callback.getResultDistributionAverager();
            ResultDistribution distr = new ResultDistribution(avResultDistr.getNumBins());
            distr.compute(integratedGraph, trueGraph);
            avResultDistr.update(distr);

            //calculate loss over error rate estimate
            ErrorRateLossAverager erLossAverager = callback.getRateLossAverager();
            erLossAverager.update(trueRates, measuredRates);
            
        } catch (Throwable t) {
            thrown.add(t);
        } finally {
            semaphore.release();
            RandomEngineRegistry.deregister();
        }
    }

    /**
     * creates and populates a given graph according to the specifications in the protocol
     * @return a populated graph
     * @throws ConfigurationException
     */
    private Graph generateTemplateGraph() throws ConfigurationException {

        Properties popType = protocol.getGraphPopulation();

        GraphPopulator graphPopulator = ExtensionRegistry.getInstance().getPopulator(popType);

        if (graphPopulator != null) {
            Graph graph = instantiateGraph(getName()+"_template");

            graphPopulator.populate(graph);

            return graph;

        } else {
            throw new ConfigurationException(popType+" is no valid graph population type");
        }
    }

    /**
     * Instantiates a graph according to the protocol
     * @return an empty graph
     * @throws ConfigurationException if the protocol is misconfigured.
     */
    public Graph instantiateGraph(String name) throws ConfigurationException {

        String type = protocol.getGraphImplementation();

        Graph graph = ExtensionRegistry.getInstance().getGraphImplementation(type);
        graph.setName(name);

        if (graph != null) {
            return graph;
        } else {
            throw new ConfigurationException(type+" is no valid graph implementation type");
        }

    }

    /**
     * performs experiments on the template graph according to the protocol in order
     * to generate evidential graphs or gold-standard graphs.
     * @param trueGraph the template graph
     * @param protocol the workflow protocol
     * @param goldStandards whether to follow the gold standard protocol section
     * or the evidential experiment protocol section.
     * @return a set of graphs representing gold standards or evidential graphs.
     * @throws ConfigurationException if the protocol is misconfigured.
     */
     private Set<Graph> performExperiments(Graph trueGraph, boolean goldStandards) throws ConfigurationException {

        Set<Graph> evidentialGraphs = new HashSet<Graph>();

        Set<Properties> experimentPropsList = goldStandards ? protocol.getGoldStandards() : protocol.getExperiments();

        int num = 0;

        for (Properties experimentProps : experimentPropsList) {

            Experiment experiment = ExtensionRegistry.getInstance().getExperiment(experimentProps);
            Object replObj = experimentProps.getProperty(Properties.REPLICAS_PROPERTY);
            int replicas = replObj instanceof Double ? ((Double)replObj).intValue() : (Integer) replObj;

            if (experiment != null) {
                for (int i = 0; i < replicas; i++) {
                    String name = getName() + (goldStandards ? "_goldStandard#" : "_experiment#") + num++;
                    Graph evidentialGraph = instantiateGraph(name);
                    experiment.perform(trueGraph, evidentialGraph);
                    evidentialGraphs.add(evidentialGraph);
                }
            } else {
                throw new ConfigurationException(experimentProps+" is no valid experiment type");
            }

        }

        return evidentialGraphs;
    }

     /**
      * determines whether the current protocl uses gold standards.
      * @param protocol the current workflow protocol.
      * @return whether the current protocl uses gold standards.
      */
    private boolean requiresGoldStandards(Protocol protocol)  {

        return protocol.getGoldStandards() != null && protocol.getGoldStandards().size() > 0;

    }

    /**
     * performs the integration method on the given graphs and gold standards according to the protocol.
     * @param evidentialGraphs the evidential graphs
     * @param goldStandards the gold standard graphs
     * @param protocol the current workflow protocol.
     * @return the integrated graph.
     * @throws ConfigurationException if the protocol is misconfigured.
     */
    private Graph performIntegration(Set<Graph> evidentialGraphs, Set<Graph> goldStandards,
            Map<Graph,double[]> ratesMap) throws ConfigurationException {

        Properties integration = protocol.getIntegrationType();

        IntegrationMethod integrationMethod = ExtensionRegistry.getInstance().getIntegrationMethod(integration);

        if (integrationMethod != null) {

            if (goldStandards != null && goldStandards.size() > 0) {
                integrationMethod.setGoldStandards(goldStandards);
            }

            integrationMethod.setEvidentialGraphs(evidentialGraphs);

            Graph integratedGraph = instantiateGraph(getName()+"_result");
            integrationMethod.setIntegratedGraph(integratedGraph);
            integrationMethod.setRatesMap(ratesMap);

            integrationMethod.start();

            return integratedGraph;

        } else {
            throw new ConfigurationException(integration+" is no valid integration method");
        }
    }

}
