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

package uk.ac.ncl.cs.harness.experiments;

import uk.ac.ncl.cs.harness.extsupport.Configurable;
import uk.ac.ncl.cs.harness.graph.Graph;

/**
 * <p>The experiment interface can be implemented as a possible extension to the
 * harness. All implementing classes should carry the <code>@Extension</code> annotation
 * as well as mark all their vital bean setters with <code>@ExtensionProperty</code>
 * annotations.</p>
 * 
 * <p>An experiment models the process of a true biological graph being measured by
 * a wet-lab experiment. A new graph is generated in the process, which contains
 * certain errors as compared to the original graph. The details of how these errors
 * are introduced depends on the implementation of the experiment.</p>
 *
 * <p>In order to apply an experiment to a given template graph, obtain an
 * <code>Experiment</code> instance from the <code>ExtensionRegistry</code> and
 * invoke the <code>perform(..)</code> method using the true graph and a new empty graph
 * as parameters. the empty graph will then be filled according to the experiment's
 * specifications.</p>
 *
 * @author Jochen Weile, M.Sc.
 */
public interface Experiment extends Configurable {

    /**
     * Performs the experiment on the true biolocial graph <code>trueGraph</code>
     * The results are written into the given graph <code>emptyGraph</code>
     * @param trueGraph the true biological template graph
     * @param emptyGraph an empty graph which will be filled with the experiment's
     * results.
     */
    void perform(Graph trueGraph, Graph emptyGraph);

}
