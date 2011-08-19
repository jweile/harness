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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Node;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.workflow.ConcurrentWorkflow;
import uk.ac.ncl.cs.harness.workflow.Protocol;
import uk.ac.ncl.cs.harness.experiments.Experiment;
import uk.ac.ncl.cs.harness.graph.populators.GraphPopulator;
import uk.ac.ncl.cs.harness.integration.IntegrationMethod;
import uk.ac.ncl.cs.harness.io.ProtocolParser;
//import uk.ac.ncl.cs.harness.stats.QuadraticLoss;

/**
 *
 * @author jweile
 */
public aspect Logging {

    public Logger logger = Logger.getLogger("uk.ac.ncl.cs.harness.Logging");

    after() returning(Node n) : call(Node Graph.createNode(String)) {
        logger.log(Level.FINEST, "Node created: \"{0}\"",n.toString());
    }

    after() returning(Edge e) : call(Edge Graph.createEdge(..)) {
        if (e != null) {
            logger.log(Level.FINEST, "Edge created: {0}", e.toString());
        }
    }

    before(Protocol protocol) : call(* ConcurrentWorkflow.performParallelCycles(Protocol)) && args(protocol) {
        logger.log(Level.INFO, "Performing parallel cycles");
        protocol.logVariables();
    }

    before() : call(* ConcurrentWorkflow.run(..)) {
        logger.log(Level.INFO, "Executing protocol");
    }

    before() : call(* Experiment.perform(..)) {
        Experiment e = (Experiment)thisJoinPoint.getTarget();
        logger.log(Level.INFO, "Performing experiment {0}", e.configSummary());
    }

    before() : call(* GraphPopulator.populate(..)) {
        GraphPopulator p = (GraphPopulator)thisJoinPoint.getTarget();
        logger.log(Level.INFO, "Populating graph {0}", p.configSummary());
    }

    before() : call(* IntegrationMethod.start(..)) {
        IntegrationMethod i = (IntegrationMethod)thisJoinPoint.getTarget();
        logger.log(Level.INFO, "Performing integration: {0}", i.configSummary());
    }

    before(File file) : call(* ProtocolParser.parse(File)) && args(file) {
        logger.log(Level.INFO, "Reading protocol file: {0}", file.getAbsolutePath());
    }

//    after() returning(double loss) : call(double QuadraticLoss.calculate(..)) {
//        logger.log(Level.INFO, "Loss: {0}", loss);
//    }
}
