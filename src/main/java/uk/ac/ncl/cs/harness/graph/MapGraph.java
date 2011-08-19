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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ncl.cs.harness.extsupport.Extension;
import uk.ac.ncl.cs.harness.util.SetOfTwo;

/**
 * This class is a basic implementation of the <code>Graph</code> interface which
 * stores all elements in maps. It is comparably inefficient for small graphs, but
 * increasingly efficient for large, sparse graphs.
 *
 * @author Jochen Weile, M.Sc.
 */
@Extension(id="mapgraph")
public final class MapGraph implements Graph {


    /**
     * The Graph's name
     */
    private String name;

    /**
     * Node id linked to node
     */
    private Map<String,Node> nodes = new HashMap<String, Node>();

    /**
     * Key (pair of node) linked to edge
     */
    private Map<SetOfTwo<Node>,Edge> edges = new HashMap<SetOfTwo<Node>, Edge>();

    /**
     * Node linked its neighbours
     */
    private Map<Node,Set<Neighbour>> neighbours = new HashMap<Node, Set<Neighbour>>();

    /**
     * gets the graph name
     * @return
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * sets the graph name
     * @param name
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Creates a new node
     * @param id
     * @return
     */
    @Override
    public Node createNode(String id) {
        if (nodes.get(id) != null) {
            warn("Tried to create duplicate node \""+id+"\" in graph \""+getName()+"\".");
            return nodes.get(id);
        } else {
            Node node = new Node(id, this);
            nodes.put(id,node);
            neighbours.put(node, new HashSet<Neighbour>());
            return node;
        }
    }


    /**
     * Creates a new edge connecting the two nodes in the Pair pair.
     *
     * If one or both of the nodes is alien to the graph, the method tries
     * to find equivalent nodes first.
     *
     * @param pair
     * @return
     */
    @Override
    public Edge createEdge(SetOfTwo<Node> nodes) {
        return createEdge(nodes, false);
    }


    /**
     * Creates a new edge connecting the two nodes in the Pair pair.
     *
     * If one or both of the nodes is alien to the graph, the method tries
     * to find equivalent nodes first. If these do not exist, it can create them
     * if the <code>createNodes</code>flag is set.
     *
     * If the edge exists already, a warning is logged and the original edge is
     * returned.
     *
     * @param pair
     * @return the new edge, or null if the required nodes do not exist and
     * createNodes was false.
     */
    private Edge createEdge(SetOfTwo<Node> pair, boolean createNodes) {

        //check if the given nodes are alien, if so: replace them
        if (!pair.getA().getOwningGraph().equals(this)
                || !pair.getB().getOwningGraph().equals(this)) {
            
            Node a = pair.getA().getOwningGraph().equals(this) ?
                        pair.getA() :
                        getNode(pair.getA(), createNodes);
            Node b = pair.getB().getOwningGraph().equals(this) ?
                        pair.getB() :
                        getNode(pair.getB(), createNodes);

            //if no local equivalents could be found or created log a warning and return null.
            if (a == null || b == null) {
                warn("Edge "+pair+" not created: No local node equivalents present.");
                return null;
            }

            pair = new SetOfTwo(a,b);
        }

        //check for duplicate edge
        if (edges.get(pair) != null) {

            warn("Tried to create duplicate edge "+pair+"in graph \""+getName()+"\".");
            return edges.get(pair);

        } else {

            Edge edge = new Edge(pair.getA(), pair.getB(), this);
            edges.put(pair, edge);

            Set<Neighbour> neigh_a = neighbours.get(pair.getA());
            neigh_a.add(new Neighbour(pair.getB(), edge));
            Set<Neighbour> neigh_b = neighbours.get(pair.getB());
            neigh_b.add(new Neighbour(pair.getA(), edge));

            return edge;
        }
    }



    /**
     * Returns all the graph's nodes
     * @return
     */
    @Override
    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    /**
     * returns all the graph's edges
     * @return
     */
    @Override
    public Collection<Edge> getEdges() {
        return Collections.unmodifiableCollection(edges.values());
    }

    /*
     * returns whether the graph contains an edge equivalent to the one given
     *
     */
    @Override
    public boolean contains(Edge edge) {
        return containsEdge(edge.getConnectedNodes());
    }


    @Override
    public boolean containsEdge(SetOfTwo<Node> nodePair) {
        return edges.containsKey(nodePair);
    }


    /**
     * returns whether the graph contains a node with the same id as the one given.
     * @param node
     * @return
     */
    @Override
    public boolean contains(Node node) {
        return nodes.containsKey(node.getId());
    }

    /**
     * returns the equivalent to the given node. If none exists it can be created
     * if the <code>create</code> flag is set.
     */
    private Node getNode(Node peer, boolean create) {
        Node node = nodes.get(peer.getId());

        if (node == null && create) {
            node = createNode(peer.getId());
        }

        return node;
    }

    @Override
    public int getNumEdges() {
        return edges.size();
    }

    @Override
    public int getNumNodes() {
        return nodes.size();
    }


    /**
     * helper enum to define the creation conditions of equivalent edges.
     * @see uk.ac.ncl.cs.harness.graph.MapGraph.getEquivalent(Edge,CreateCondition)
     */
    private enum CreateCondition {
        DO_NOT_CREATE, CREATE_IF_NODES_EXIST, ALWAYS_CREATE
    }

    /**
     * returns the equivalent to the given edge. If none exists it can be created,
     * depending on the <code>CreateCondition</code>.
     *
     * @param peer the alien edge
     * @param create
     * @return  the equivalent to the given edge
     */
    private Edge getEdge(Edge peer, CreateCondition create) {
        Edge edge = edges.get(peer.getConnectedNodes());
        if (edge == null && create != CreateCondition.DO_NOT_CREATE) {
            edge = createEdge(peer.getConnectedNodes(), create == CreateCondition.ALWAYS_CREATE);
        }
        return edge;
    }

    /**
     * returns the set of neighbours for the given node
     * @param node the node
     * @return the set of neighbours for the node.
     */
    @Override
    public Set<Neighbour> getNeighboursOfNode(Node node) {
        return Collections.unmodifiableSet(neighbours.get(node));
    }


    /**
     * logs a warning message
     * @param msg the message.
     */
    private void warn(String msg) {
        Logger.getLogger(this.getClass().getCanonicalName()).log(Level.WARNING, msg, new Throwable(){});
    }
}
