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

import java.util.Map;
import java.util.Set;
import uk.ac.ncl.cs.harness.exceptions.ConfigurationException;
import uk.ac.ncl.cs.harness.extsupport.Configurable;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Node;

/**
 * <p>The <code>IntegrationMethod</code> interface can be implemented as a possible extension to the
 * harness. All implementing classes should carry the <code>@Extension</code> annotation
 * as well as mark all their vital bean setters with <code>@ExtensionProperty</code>
 * annotations.</p>
 *
 * <p>Implementations of the integration methods usually take a set of evidential
 * (optionally also a set of gold standard graphs) and try to infer the probabilities
 * of edge existence in the union of these evidential graphs. To use an integration
 * method, obtain an instance from the <code>ExtensionRegistry</code> supply it with
 * the required evidential graphs, gold standards and other possible parameters. Also,
 * you should provide an empty graph that will be filled to represent the integration result,
 * using the method <code>setIntegratedGraph(..)</code>. Then execute the
 * <code>start()</code> method.</p>
 *
 * @author Jochen Weile, M.Sc.
 */
public abstract class IntegrationMethod implements Configurable  {

    /**
     * an empty graph that will be filled with the integration results.
     */
    protected Graph integratedGraph;

    /**
     * the set of evidential graphs.
     */
    protected Set<Graph> evidentialGraphs;

    /**
     * a set of gold standards. this may be empty.
     */
    protected Set<Graph> goldStandards;

    /**
     * stores error rates (fp/fn) for evaluation by the workflow.
     */
    protected Map<Graph, double[]> ratesMap;

    /**
     * gets the integrated graph.
     * @return the integrated graph.
     */
    public Graph getIntegratedGraph() {
        return integratedGraph;
    }

    /**
     * sets the empty graph that will be filled with integration results
     * @param graph an empty graph.
     */
    public void setIntegratedGraph(Graph graph) {
        this.integratedGraph = graph;
    }

    /**
     * sets the set of evidential graphs.
     * @param evidentialGraphs the set of evidential graphs.
     */
    public void setEvidentialGraphs(Set<Graph> evidentialGraphs) {
        this.evidentialGraphs = evidentialGraphs;
    }

    /**
     * sets the set of gold standards
     * @param goldStandards the set of gold standards.
     */
    public void setGoldStandards(Set<Graph> goldStandards) {
        this.goldStandards = goldStandards;
    }

    /**
     * gets the set of evidential graphs.
     * @return the set of evidential graphs.
     */
    public Set<Graph> getEvidentialGraphs() {
        return evidentialGraphs;
    }

    /**
     * gets the set of gold standards.
     * @return the set of gold standards.
     */
    public Set<Graph> getGoldStandards() {
        return goldStandards;
    }


    /**
     * Sets the rates map. Called by the workflow.
     * @param ratesMap the error rates map
     */
    public void setRatesMap(Map<Graph, double[]> ratesMap) {
        this.ratesMap = ratesMap;
    }

    /**
     * Starts the integration process.
     * @throws ConfigurationException if the integration method has been
     * misconfigured.
     */
    public abstract void start() throws ConfigurationException;

    /**
     * Fills the empty <code>integratedGraph</code> with the union of all
     * evidential graphs.
     */
    protected void performUnion() {
        
        for (Graph evidential : evidentialGraphs) {

            for (Node n : evidential.getNodes()) {
                if (!integratedGraph.contains(n)) {
                    integratedGraph.createNode(n.getId());
                }
            }
        
            for (Edge e : evidential.getEdges()) {
                if (!integratedGraph.contains(e)) {
                    integratedGraph.createEdge(e.getConnectedNodes());
                }
            }
        }
    }

}
