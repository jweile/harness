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
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import uk.ac.ncl.cs.harness.LoggingInit;
import uk.ac.ncl.cs.harness.io.OutputController;
import uk.ac.ncl.cs.harness.workflow.variables.IncrementalVariable;
import uk.ac.ncl.cs.harness.workflow.variables.Variable;

/**
 *
 * @author jweile
 */
public class ConcurrentWorkflowTest extends TestCase {
    
    public ConcurrentWorkflowTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LoggingInit.init();
        OutputController.getInstance().prepareOutput("surefire", null);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testStaticWorkflow() throws Exception {

        Protocol protocol = makeProtocol();

        ConcurrentWorkflow workflow = new ConcurrentWorkflow(4);
        workflow.run(protocol);

    }

    public void testDynamicWorkflow() throws Exception {

        String varName = "var";
        Map<String,Variable> variables = new HashMap<String, Variable>();
        Variable var = new IncrementalVariable(varName, 2.0, 3.0, 1.0);
        variables.put(varName, var);

        Protocol protocol = makeProtocol();
        protocol.setVariables(variables);
        protocol.getExperiments().iterator().next().setProperty(Properties.REPLICAS_PROPERTY, var);

        ConcurrentWorkflow workflow = new ConcurrentWorkflow(4);
        workflow.run(protocol);

    }

    private Protocol makeProtocol() {

        Protocol protocol = new Protocol();

        protocol.setVariables(Collections.EMPTY_MAP);

        protocol.setGraphImplementation("mapgraph");

        Properties p = new Properties();
        p.setProperty(Properties.ID_PROPERTY, "scalefree");
        p.setProperty("seed", 2);
        p.setProperty("numNodes", 20);
        protocol.setGraphPopulation(p);

        p = new Properties();
        p.setProperty(Properties.ID_PROPERTY, "simpleexperiment");
        p.setProperty(Properties.REPLICAS_PROPERTY, 2);
        p.setProperty("sensitivity", .9);
        p.setProperty("specificity", .9);
        Set<Properties> set = new HashSet<Properties>();
        set.add(p);
        protocol.setExperiments(set);
        protocol.setGoldStandards(set);

        protocol.setCycleReplicas(32);

        p = new Properties();
        p.setProperty(Properties.ID_PROPERTY, "lycett");
        protocol.setIntegrationType(p);

        return protocol;
    }

}
