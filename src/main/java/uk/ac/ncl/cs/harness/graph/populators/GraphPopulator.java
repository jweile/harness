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

package uk.ac.ncl.cs.harness.graph.populators;

import uk.ac.ncl.cs.harness.extsupport.Configurable;
import uk.ac.ncl.cs.harness.graph.Graph;

/**
 * <p>Populators create nodes and edges in graphs according to specific rules.
 * For example a possible populator could import network data from a file and use it to populate
 * a graph object, or it could create nodes and edges on a random basis to form certain
 * graph classifications, like random graphs or scale free graphs.</p>
 *
 * <p>The <code>GraphPopulator</code> interface can be implemented as a possible extension to the
 * harness. All implementing classes should carry the <code>@Extension</code> annotation
 * as well as mark all their vital bean setters with <code>@ExtensionProperty</code>
 * annotations.</p>
 *
 * <p>To populate a graph with a populator, obtain an instance from the
 * <code>ExtensionRegistry</code> and invoke its <code>populate(..)</code> method
 * using the graph to fill as the parameter.</p>
 *
 * @author Jochen Weile, M.Sc.
 */

public interface GraphPopulator extends Configurable  {

    /**
     * populates the given graph .
     * @param graph the graph to populate.
     */
    void populate(Graph graph);

}
