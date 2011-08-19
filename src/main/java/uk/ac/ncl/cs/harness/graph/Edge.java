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

import java.util.Collections;
import uk.ac.ncl.cs.harness.util.SetOfTwo;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class represents an edge in an undirected graph.
 *
 * @author Jochen Weile, M.Sc.
 */
public class Edge implements Comparable<Edge> {

    /**
     * The nodes that are connected by the edge.
     */
    private final SetOfTwo<Node> ij;

    /**
     * the probabilities carried by the edge.
     */
    private Map<String,Probability> probabilities;

    /**
     * the graph object to which this edge belongs.
     */
    private Graph owningGraph;

    /**
     * a package-private constructor that can only be used by graph implementations.
     * The order of the connected nodes i and j does not matter, as this is an undirected edge.
     *
     * @param i a node
     * @param j another node
     * @param owningGraph the graph to which this edge (and the adjacent nodes) belong.
     */
    Edge(Node i, Node j, Graph owningGraph) {
//        if (i.getId().compareTo(j.getId()) <= 0) {
//            this.i = i;
//            this.j = j;
//        } else {
//            this.i = j;
//            this.j = i;
//        }
        ij = new SetOfTwo<Node>(i, j);
        this.owningGraph = owningGraph;
    }

    /**
     * Returns the two nodes, which are connected by this undirected edge.
     * @return a pair of nodes, whcih are connected by this edge.
     */
    public SetOfTwo<Node> getConnectedNodes() {
//        return new Pair<Node>(i,j);
        return ij;
    }

    /**
     * Adds a probability to the edge.
     * @param name the label of the probabilty.
     * @param prob the value of the probability.
     */
    public void addProbability(String name, double prob) {
        if (probabilities == null) {
            probabilities = new HashMap<String, Probability>(5);
        }
        Probability p = new Probability(name, prob);
        probabilities.put(name, p);
    }

    /**
     * Retrives all stored probabilities on this edge.
     * @return a set of all probabilities stored in this edge.
     */
    public Set<Probability> getProbabilities() {
        if (probabilities == null) {
            return Collections.emptySet();
        } else {
            return new HashSet(probabilities.values());
        }
    }

    /**
     * Retrieves the probability identified by the given ID on this edge.
     * @param id the ID of the probability.
     * @return the probability or null if it does not exist.
     */
    public Probability getProbability(String id) {
        if (probabilities == null) {
            return null;
        } else {
            return probabilities.get(id);
        }
    }

    /**
     * gets the graph object to which this edge belongs.
     * @return the graph object to which this edge belongs.
     */
    public Graph getOwningGraph() {
        return owningGraph;
    }

    /**
     * Equals method for undirected edge.
     * @param obj another object.
     * @return whether <code>obj</code> equals this edge.
     */
    @Override
    public boolean equals(Object obj) {
       if (obj != null && obj instanceof Edge) {
           Edge other = (Edge) obj;
           return ij.equals(other.ij);
       }
       return false;
    }

    /**
     * hash code for undirected edge
     * @return
     */
    @Override
    public int hashCode() {
//        int hash = 7;
//        hash = 29 * hash + (this.i.getId() != null ? this.i.getId().hashCode() : 0);
//        hash = 29 * hash + (this.j.getId() != null ? this.j.getId().hashCode() : 0);
//        return hash;
        return ij.hashCode();
    }

    /**
     * String representation for this edge.
     * @return
     */
    @Override
    public String toString() {
        return "{"+ij.getA().toString()+";"+ij.getB().toString()+"}";
    }


    @Override
    public int compareTo(Edge o) {
//        int cti = i.compareTo(o.i);
//        return cti == 0 ? j.compareTo(o.j) : cti;
        return ij.compareTo(o.ij);
    }
}
