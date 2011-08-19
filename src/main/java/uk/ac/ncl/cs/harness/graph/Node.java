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

import java.util.Set;

/**
 * This class represents a node in a graph.
 *
 * @author Jochen Weile, M.Sc.
 */
public class Node implements Comparable<Node> {

    /**
     * the node's ID.
     */
    private final String id;

    /**
     * The graph to which this node belongs.
     */
    private final Graph owningGraph;

    /**
     * package-private constructor to be only used from graph implementations.
     * @param id the node ID
     * @param owningGraph the graph to which this node belongs.
     */
    Node(String id, Graph owningGraph) {
        if(id == null) throw new NullPointerException("Can't create a node with null id");
        if(owningGraph == null) throw new NullPointerException("Can't create a node with null graph");

        this.id = id;
        this.owningGraph = owningGraph;
    }

    /**
     * gets the ID of this node
     * @return the id of the node
     */
    public String getId() {
        return id;
    }

    /**
     * gets the set of all neighbours of this node in the network.
     * @return the set of all neighbours of this node in the network
     */
    public Set<Neighbour> getNeighbours() {
        return owningGraph.getNeighboursOfNode(this);
    }

    /**
     * returns the node's degree (number of connecting edges)
     * @return the node's degree
     */
    public int degree() {
        return owningGraph.getNeighboursOfNode(this).size();
    }

    /**
     * returns the graph to which this node belongs.
     * @return the graph to which this node belongs.
     */
    public Graph getOwningGraph() {
        return owningGraph;
    }

    /**
     * equals method
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Node other = (Node) obj;
        return id.equals(other.id);
//        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
//            return false;
//        }
//        return true;
    }

    /**
     * hash code function
     */
    @Override
    public int hashCode() {
//        int hash = 5;
//        hash = 59 * hash + (this.id != null ? this.id.hashCode() : 0);
//        return hash;
        return this.id.hashCode();
    }

    /**
     * string representation of this node.
     */
    @Override
    public String toString() {
        return id;
    }

    /**
     * comparable interface implementation
     */
    public int compareTo(Node anotherNode) {
        return id.compareTo(anotherNode.getId());
    }


}
