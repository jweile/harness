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

import cern.jet.random.Beta;
import cern.jet.random.Uniform;
import java.io.IOException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ncl.cs.harness.extsupport.Extension;
import uk.ac.ncl.cs.harness.extsupport.ExtensionProperty;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Node;
import uk.ac.ncl.cs.harness.graph.Probability;
import uk.ac.ncl.cs.harness.integration.ebm.BurnIn;
import uk.ac.ncl.cs.harness.integration.ebm.EdgeAverager;
import uk.ac.ncl.cs.harness.integration.ebm.ErrorRates;
import uk.ac.ncl.cs.harness.integration.ebm.RateAverager;
import uk.ac.ncl.cs.harness.io.OutputController;
import uk.ac.ncl.cs.harness.stats.ErrorGauge;
import uk.ac.ncl.cs.harness.stats.RandomEngineRegistry;
import uk.ac.ncl.cs.harness.util.SetOfTwo;

/**
 *
 * A Markov Chain Monte Carlo (MCMC)-based fully bayesian integration
 * method.
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
@Extension(id="mcmcEBM")
public final class McmcFBM extends IntegrationMethod {

    /**
     * Maps each input graph to its current error rate estimates
     */
    private Map<Graph,ErrorRates> errorRates;

    /**
     * Maps each edge of the consensus graph to its current posterior
     * existence probability estimate.
     */
    private Map<Edge,Double> probabilities = new TreeMap<Edge, Double>();

    /**
     * Set of edges in the consensus graph edges
     */
    private Set<Edge> intermediateGraph = new HashSet<Edge>(1024);

    /**
     * Beta distributed sampler
     */
    private Beta beta;

    /**
     * Uniform distributed sampler
     */
    private Uniform uniform;

    /**
     * Number of cycles to perform
     */
    private int maxCycles;

    private int minCycles;

    private boolean writeErrorRatesToFile = true;

    private int acLag = 10;

    private double acThreshold = .2;

    private int burnIn = 100;

    private int tpOffset = 1, tnOffset = 1, fpOffset = 1, fnOffset = 1 ;


    @ExtensionProperty(id="fnOffset")
    public void setFnOffset(int fnOffset) {
        this.fnOffset = fnOffset;
    }

    @ExtensionProperty(id="fpOffset")
    public void setFpOffset(int fpOffset) {
        this.fpOffset = fpOffset;
    }

    @ExtensionProperty(id="tnOffset")
    public void setTnOffset(int tnOffset) {
        this.tnOffset = tnOffset;
    }

    @ExtensionProperty(id="tpOffset")
    public void setTpOffset(int tpOffset) {
        this.tpOffset = tpOffset;
    }



    /**
     * Determines whether likelihood should be computed for each cycle
     * Disabled by default. Enabling will affect runtime.
     */
    private boolean computeLikelihood = false;

    private String makeErrorRateFileName() {
        String tname = Thread.currentThread().getName();
        return "mcmc"+tname;
    }

    /**
     * Constructor. Initializes samplers.
     */
    public McmcFBM() {
        beta = new Beta(1,1, RandomEngineRegistry.getEngine());
        uniform = new Uniform(RandomEngineRegistry.getEngine());
    }

    /**
     * Maximal Number of cycles to perform.
     * This maps to the extension property "numCycles".
     * @param maxCycles  Number of cycles to perform.
     */
    @ExtensionProperty(id="maxCycles")
    public void setMaxCycles(int maxCycles) {
        this.maxCycles = maxCycles;
    }

    /**
     *
     * @param minCycles
     */
    @ExtensionProperty(id="minCycles")
    public void setMinCycles(int minCycles) {
        this.minCycles = minCycles;
    }



    /**
     * 
     * @param writeErrorRatesToFile
     */
    @ExtensionProperty(id="writeErrorRatesFile")
    public void setWriteErrorRatesToFile(boolean writeErrorRatesToFile) {
        this.writeErrorRatesToFile = writeErrorRatesToFile;
    }

    /**
     *
     * @param acLag
     */
    @ExtensionProperty(id="acLag")
    public void setAcLag(int acLag) {
        this.acLag = acLag;
    }

    /**
     * 
     * @param acThreshold
     */
    @ExtensionProperty(id="acThreshold")
    public void setAcThreshold(double acThreshold) {
        this.acThreshold = acThreshold;
    }

    @ExtensionProperty(id="burnIn")
    public void setBurnIn(int burnIn) {
        this.burnIn = burnIn;
    }

    



    /**
     * Determines whether likelihood should be computed for each cycle.
     * Disabled by default. Enabling will affect runtime.
     * @param computeLikelihood Determines whether likelihood should be computed for each cycle.
     */
    @ExtensionProperty(id="computeLikelihood")
    public void setComputeLikelihood(boolean computeLikelihood) {
        this.computeLikelihood = computeLikelihood;
    }


    /**
     * Starts the integration process.
     */
    @Override
    public void start() {

        BurnIn burnInRecorder = new BurnIn(burnIn, evidentialGraphs.size(), acLag, acThreshold);

        EdgeAverager edgeAverager = new EdgeAverager();

        RateAverager rateAverager = new RateAverager(evidentialGraphs);

        //generate consensus graph
        performUnion();

        //calculate inital error rates ("theta") for each input graph
        initErrorRates();

        //prior probability for an edge to exist ("q")
        double prior = 2.0 / ((double)integratedGraph.getNumNodes() - 1.0);

        boolean isBurnInPhase = true;
        int skip = 1;

        //Main loop.
        for (int i = 0; i < maxCycles; i++) {

            //calculate posterior edge probabilites based on latest theta
            updateProbabilities(prior);

            //use posterior edge probabilites to sample a consensus graph ("G")
            sampleIntermediateGraph();

            //sample a new theta based on G
            sampleErrorRates();

            //write error rates to file
            if (writeErrorRatesToFile) {
                //compute score proportional to state likelihood.
                double l = computeLikelihood ? computeLikelihoodAnalogue() : 0.0;
                pushErrorRatesToFile(errorRates, l);
            }

            if (isBurnInPhase) {

                if (!burnInRecorder.record(errorRates)) {
                    skip = burnInRecorder.computeThinningFactor(20);
                    burnInRecorder = null;//free up memory
                    isBurnInPhase = false;
                }

            } else if (i % skip == 0) {//if it's a non-skip cycle

                rateAverager.update(errorRates);

                edgeAverager.update(intermediateGraph);
                if (edgeAverager.getNumSamples() >= minCycles) {
                    break;//then we have enough samples.
                }

            }

        }

        //store average over all intermediate thetas
        extractFinalErrorRates(rateAverager);

        //apply average theta to generate final graph
        assignFinalValues(edgeAverager.getEdgeAverages());

        //close error rates file
        if (writeErrorRatesToFile) closeErrorRateFile();

    }

    private void closeErrorRateFile() {
        try {
            OutputController.getInstance().closeResultStream(makeErrorRateFileName());
        } catch (IOException ex) {
            Logger.getLogger(McmcFBM.class.getName()).log(Level.SEVERE,
                    "Unable to close handle for results file!", ex);
        }
    }

    /**
     * calculate posterior existence probablity for each edge.
     * Works essentially like the improved lycett method
     */
    private void updateProbabilities(double prior) {

        //convert prior probability to log odds.
        double logPriorOdds = Math.log(prior / (1.0 - prior));

        // store error rates in an array for quick lookup
//        ErrorRates[] errorRateA = new ErrorRates[evidentialGraphs.size()];
//        {
//            int i = 0;
//            for (Graph eviGraph : evidentialGraphs) {
//                errorRateA[i++] = errorRates.get(eviGraph);
//            }
//        }

        //For each edge
        for (Edge edge : integratedGraph.getEdges()) {

            //K = prior + ...
            double k = logPriorOdds;

            // + sum_i (wrt evidences) of...
            for (Graph eviGraph : evidentialGraphs) {
                //only if the graph contains the node pair in question
                if (containsNodes(eviGraph,edge.getConnectedNodes())) {

                    //lambda_i (Bayes factor) depending on positive or negative reporting
                    ErrorRates bf = errorRates.get(eviGraph);

                    if(eviGraph.contains(edge)) {
                        k += bf.getLogPositiveBayesFactor();
                    } else {
                        k += bf.getLogNegativeBayesFactor();
                    }
                }
            }

            //convert log odds back to probability and store for edge
            double p = Math.exp(k) / (1.0 + Math.exp(k));
            probabilities.put(edge,p);
        }
    }



    private boolean containsNodes(Graph graph, SetOfTwo<Node> nodes) {
        return graph.contains(nodes.getA()) && graph.contains(nodes.getB());
    }


//    private Map<Edge, boolean[]> edge2Graphs = new HashMap<Edge, boolean[]>();
//    private boolean[] graphsForEdge(Edge edge) {
//        boolean[] gs = edge2Graphs.get(edge);
//        if(gs == null) {
//            gs = new boolean[evidentialGraphs.size()];
//            int i = 0;
//            for (Graph g : evidentialGraphs) {
//                gs[i] = g.contains(edge);
//                i++;
//            }
//            edge2Graphs.put(edge, gs);
//        }
//        return gs;
//    }

    /**
     * Sample a new theta based on current G
     */
    private void sampleErrorRates() {

        for (Graph eviGraph : evidentialGraphs) {
            
            double[] err = ErrorGauge.calculate(eviGraph, intermediateGraph);

            //sample false postive rate from beta and calculate its density value
            beta.setState(err[ErrorGauge.FP]+fpOffset, err[ErrorGauge.TN]+tnOffset);
            double fprSample = beta.nextDouble();

            //sample false negative rate from beta and calculate its density value
            beta.setState(err[ErrorGauge.FN]+fnOffset, err[ErrorGauge.TP]+tpOffset);
            double fnrSample = beta.nextDouble();

            //store samples and densities
            ErrorRates e = new ErrorRates(fprSample, fnrSample);
            errorRates.put(eviGraph, e);
        }

    }

    /**
     * Calculates inital error rates for each dataset
     */
    private void initErrorRates() {

        //init map
        errorRates = new HashMap<Graph,ErrorRates>(evidentialGraphs.size());

        //assign random beta(1,1) value.
        for (Graph eviGraph : evidentialGraphs) {
            double fpr = beta.nextDouble(fpOffset,tnOffset);
            double fnr = beta.nextDouble(fnOffset,tpOffset);
            errorRates.put(eviGraph, new ErrorRates(fpr, fnr));
        }

    }

    /**
     * sample intermediate consensus graph (G) based on p
     */
    private void sampleIntermediateGraph() {
        //for each edge that has at least one supporting evidence (others have p=0 anyway)
        for (Edge edge : integratedGraph.getEdges()) {

            //if uniform random > p(edge) create it otherwise remove it
            double p = probabilities.get(edge);
            if (uniform.nextDouble() < p) {
                intermediateGraph.add(edge);
            } else {
                intermediateGraph.remove(edge);
            }
        }
    }

    /**
     * Compute logarithm of score proportional to state likelihood.
     * @return
     */
    private double computeLikelihoodAnalogue() {

        //Compute log(P(G))
        int n = integratedGraph.getNumNodes();
        int all = (n*n - n) / 2;
        double p = (double)integratedGraph.getNumEdges() / (double)all;

        double logPriorG = intermediateGraph.size() * Math.log(p);


        //Compute log(pi(theta)) = 0
        double logPriorTheta = 0;

        //##Compute log(P(X|theta,G))##
        //log(P(X|theta,G) = ...
        double logPosteriorX = 0;
        //sum_i (wrt input graphs) of ...
        for (Graph eviGraph : evidentialGraphs) {
            //(cache logs of error rates)
            double logAlpha = Math.log(errorRates.get(eviGraph).getFalsePositiveRate());
            double logInvAlpha = Math.log(1.0 - errorRates.get(eviGraph).getFalsePositiveRate());
            double logBeta = Math.log(errorRates.get(eviGraph).getFalseNegativeRate());
            double logInvBeta = Math.log(1.0 - errorRates.get(eviGraph).getFalseNegativeRate());

            //sum_{e in E} of
            for (Edge e : integratedGraph.getEdges()) {//Warning: these are only edges that exist in at least one input graph
                //case e in E_G
                if (intermediateGraph.contains(e)) {
                    //case e in E_X_i
                    if (eviGraph.contains(e)) {
                        logPosteriorX += logInvBeta; //log(1-beta_i)
                    } else {//case e not in E_X_i
                        logPosteriorX += logBeta; //log(beta_i)
                    }
                //case e not in E_G
                } else {
                    //case e in E_X_i
                    if (eviGraph.contains(e)) {
                        logPosteriorX += logAlpha;//log(alpha_i)
                    } else {//case e not in E_X_i
                        logPosteriorX += logInvAlpha;//log(1-alpha_i)
                    }
                }
            }
            //Add missing TN rates (that were not part of the union of input edges)
            logPosteriorX += (double)(all - integratedGraph.getNumEdges()) * logInvAlpha;
        }

        //Combine results
        return logPriorG + logPriorTheta + logPosteriorX;
    }


    /**
     * Updates the error rates according to the mean values from the recorder
     * @param recorder
     */
    private void extractFinalErrorRates(RateAverager recorder) {
        for (Graph eviGraph : evidentialGraphs) {

            double fprMean = recorder.getFprAverage(eviGraph);
            double fnrMean = recorder.getFnrAverage(eviGraph);

            ratesMap.put(eviGraph, new double[]{fprMean,fnrMean,
                    Double.NaN,Double.NaN, Double.NaN,Double.NaN});
        }
    }

    /**
     * Assigns final values to the graph.
     */
    private void assignFinalValues(Map<Edge,Double> averages) {
        for (Edge edge : integratedGraph.getEdges()) {
            Double average = averages.get(edge);
            if (average == null) {
                average = 0.0;
            }
            edge.addProbability(Probability.MAIN_KEY, average);
        }
    }


    /**
     * Make log entry for current error rates.
     */
    private void pushErrorRatesToFile(Map<Graph,ErrorRates> errorRates, double likelihood) {
        StringBuilder b = new StringBuilder("Rates:\t");
        for (Graph eviGraph : evidentialGraphs) {
            ErrorRates rates = errorRates.get(eviGraph);
            b.append(String.format("%.8f",rates.getFalsePositiveRate()))
                    .append("\t")
                    .append(String.format("%.8f",rates.getFalseNegativeRate()))
                    .append("\t");
        }
        b.append(String.format("%.8f", likelihood)).append("\n");
        try {
            OutputController.getInstance().writeResultStream(makeErrorRateFileName(), b.toString());
        } catch (IOException ex) {
            throw new RuntimeException("Unable to write to results file", ex);
        }
    }


    /**
     * returns summary information about this plugin
     * @return
     */
    @Override
    public String configSummary() {
        return "\"" + this.getClass().getSimpleName() + "\"";
    }
}

