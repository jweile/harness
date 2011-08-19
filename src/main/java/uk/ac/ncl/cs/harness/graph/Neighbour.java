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

/**
 *
 * This class models a neighbourhood in a graph. it stores an edge and the node
 * that this edge connects to.
 *
 * @author Jochen Weile, M.Sc.
 */
public class Neighbour {

    /**
     * The connected node.
     */
    private Node node;

    /**
     * the connecting edge.
     */
    private Edge connectingEdge;

    /**
     * constructor with target node and connecting edge
     * @param node the target node
     * @param connectingEdge the connecting edge
     */
    Neighbour(Node node, Edge connectingEdge) {
        this.node = node;
        this.connectingEdge = connectingEdge;
    }

    /**
     * returns the connecting edge.
     * @return the connecting edge.
     */
    public Edge getConnectingEdge() {
        return connectingEdge;
    }

    /**
     * returns the target node.
     * @return the target node.
     */
    public Node getNode() {
        return node;
    }

    /**
     * returns whether <code>obj</code> equals this neighbourhood.
     * @param obj another object
     * @return whether <code>obj</code> equals this neighbourhood.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Neighbour other = (Neighbour) obj;
        if (this.node != other.node && (this.node == null || !this.node.equals(other.node))) {
            return false;
        }
        if (this.connectingEdge != other.connectingEdge && (this.connectingEdge == null || !this.connectingEdge.equals(other.connectingEdge))) {
            return false;
        }
        return true;
    }

    /**
     * hashcode for theis neighbourhood
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.node != null ? this.node.hashCode() : 0);
        hash = 59 * hash + (this.connectingEdge != null ? this.connectingEdge.hashCode() : 0);
        return hash;
    }

    

}
