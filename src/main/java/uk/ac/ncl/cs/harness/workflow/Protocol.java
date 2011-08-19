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

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ncl.cs.harness.workflow.variables.Variable;

/**
 * The protocol describes the details of the workflow execution. It defines variables,
 * graph implementations and populators to use, experiments and integrations to perform.
 *
 * @author Jochen Weile, M.Sc.
 */
public class Protocol {

    /**
     * the logger.
     */
    private static final Logger logger =
            Logger.getLogger(Protocol.class.getName());

    /**
     * properties describing which graph populator to use and how to
     * configure it.
     */
    private Properties graphPopulation;

    /**
     * id of the graph implementation to use.
     */
    private String graphImplementation;

    /**
     * properties describing which integration method to use and how to
     * configure it.
     */
    private Properties integrationType;

    /**
     * set of properties describing which experiment configurations to use to
     * simulate gold standards.
     */
    private Set<Properties> goldStandards;

    /**
     * set of properties describing which experiment configurations to use to
     * simulate evidential graphs.
     */
    private Set<Properties> experiments;

    /**
     * A map linking variable IDs to the actual variables.
     */
    private Map<String, Variable> variables;

    /**
     * number of cycle replicas
     */
    private int cycleReplicas;


    /**
     * gets the graph implementation id
     * @return the graph implementation id
     */
    public String getGraphImplementation() {
        return graphImplementation;
    }

    /**
     * sets the graph implementation id
     * @param graphImplementation the graph implementation id
     */
    public void setGraphImplementation(String graphImplementation) {
        this.graphImplementation = graphImplementation;
    }

    /**
     * gets properties describing which graph populator to use and how to
     * configure it.
     * @return properties describing which graph populator to use and how to
     * configure it.
     */
    public Properties getGraphPopulation() {
        return graphPopulation;
    }

    /**
     * sets properties describing which graph populator to use and how to
     * configure it.
     * @param graphPopulation properties describing which graph populator to
     * use and how to configure it.
     */
    public void setGraphPopulation(Properties graphPopulation) {
        this.graphPopulation = graphPopulation;
    }

    /**
     * gets the set of properties describing which experiment configurations to use to
     * simulate evidential graphs.
     * @return set of properties describing which experiment configurations to use to
     * simulate evidential graphs.
     */
    public Set<Properties> getExperiments() {
        return experiments;
    }

    /**
     * sets the set of properties describing which experiment configurations to use to
     * simulate evidential graphs.
     * @param experiments set of properties describing which experiment configurations to use to
     * simulate evidential graphs.
     */
    public void setExperiments(Set<Properties> experiments) {
        this.experiments = experiments;
    }

    /**
     * gets the set of properties describing which experiment configurations to use to
     * simulate gold standards.
     * @return set of properties describing which experiment configurations to use to
     * simulate gold standards.
     */
    public Set<Properties> getGoldStandards() {
        return goldStandards;
    }

    /**
     * sets the set of properties describing which experiment configurations to use to
     * simulate gold standards.
     * @param goldStandards set of properties describing which experiment configurations to use to
     * simulate gold standards.
     */
    public void setGoldStandards(Set<Properties> goldStandards) {
        this.goldStandards = goldStandards;
    }

    /**
     * gets properties describing which integration method to use and how to
     * configure it.
     * @return properties describing which integration method to use and how to
     * configure it.
     */
    public Properties getIntegrationType() {
        return integrationType;
    }

    /**
     * sets properties describing which integration method to use and how to
     * configure it.
     * @param integrationType properties describing which integration method to use and how to
     * configure it.
     */
    public void setIntegrationType(Properties integrationType) {
        this.integrationType = integrationType;
    }

    /**
     * gets the variable map
     * @return the map of variables
     */
    public Map<String, Variable> getVariables() {
        return this.variables;
    }

    /**
     * sets the variable map
     * @param variables the variable map
     */
    public void setVariables(Map<String, Variable> variables) {
        this.variables = variables;
    }

    /**
     * gets the cycle replicas
     * @return the cycle replicas
     */
    public int getCycleReplicas() {
        return cycleReplicas;
    }

    /**
     * sets the cycle replicas
     * @param cycleReplicas the cycle replicas
     */
    public void setCycleReplicas(int cycleReplicas) {
        this.cycleReplicas = cycleReplicas;
    }

    /**
     * logs the currently active variable states.
     */
    public void logVariables() {
        StringBuilder builder = new StringBuilder("Variable configuration:\n");
        for (Variable var : variables.values()) {
            builder.append(var.getId()).append(" = ").append(var.getCurrentValue()).append("\n");
        }
        logger.log(Level.INFO, builder.toString());
    }

    public String getVariableValuesAsString() {
        StringBuilder builder = new StringBuilder();
        for (Variable var : variables.values()) {
            builder.append(var.getId()).append("=").append(var.getCurrentValue()).append(";");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length()-1);
        }
        return builder.toString();
    }

    

}
