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

package uk.ac.ncl.cs.harness.graph;

import java.util.Collection;
import java.util.Set;
import uk.ac.ncl.cs.harness.util.SetOfTwo;

/**
 *
 * <p>This interface represents an undirected graph.</p>
 * 
 * <p>The Graph interface can be implemented as a possible extension to the
 * harness. All implementing classes should carry the <code>@Extension</code> annotation
 * as well as mark all their vital bean setters with <code>@ExtensionProperty</code>
 * annotations.</p>
 *
 * @author Jochen Weile, M.Sc.
 */
public interface Graph {

    /**
     * sets the name of the graph.
     * @param name the name
     */
    void setName(String name);

    /**
     * gets the name of the graph.
     * @return the name
     */
    String getName();

    /**
     * creates a node with the given id
     * @param id the node's id
     * @return the node
     */
    Node createNode(String id);

    /**
     * Creates an edge between the given pair of nodes.
     * @param nodes the pair of nodes which is to be connected by an edge.
     * @return the edge
     */
    Edge createEdge(SetOfTwo<Node> nodes);

    /**
     * gets the set of all nodes contained in the graph.
     * @return the set of all nodes contained in the graph.
     */
    Collection<Node> getNodes();

    /**
     * gets the set of all edges in the graph.
     * @return the set of all edges in the graph.
     */
    Collection<Edge> getEdges();

    /**
     * gets the number of edges.
     * @return the number of edges.
     */
    int getNumEdges();

    /**
     * gets the number of nodes.
     * @return the number of nodes.
     */
    int getNumNodes();

    /**
     * returns whether the graph contains the given node.
     * @param node the given node.
     * @return whether the graph contains the given node.
     */
    boolean contains(Node node);

    /**
     * returns whether the graph contains a given edge
     * @param edge the edge
     * @return whether the graph contains a given edge
     */
    boolean contains(Edge edge);

    /**
     * returns whether the graph contains an edge for the given pair of nodes
     * @param nodePair a pair of nodes.
     * @return whether the graph contains an edge for the given pair of nodes
     */
    boolean containsEdge(SetOfTwo<Node> nodePair);

    /**
     * returns all neighbours of the given node
     * @param node the node
     * @return the set of neighbours for the given node.
     */
    Set<Neighbour> getNeighboursOfNode(Node node);

}
