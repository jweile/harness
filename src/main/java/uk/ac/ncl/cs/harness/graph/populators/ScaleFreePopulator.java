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

import cern.jet.random.Uniform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import uk.ac.ncl.cs.harness.extsupport.Extension;
import uk.ac.ncl.cs.harness.extsupport.ExtensionProperty;
import uk.ac.ncl.cs.harness.graph.Edge;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.Node;
import uk.ac.ncl.cs.harness.stats.RandomEngineRegistry;
import uk.ac.ncl.cs.harness.util.SetOfTwo;

/**
 * This is a graph populator that generates a scale-free networks with the given
 * number of seeds and a given number of nodes. the number of edges will equal the
 * number of nodes, since this is an impementation of the preferential attachment
 * algorithm.
 *
 * @author Jochen Weile, M.Sc.
 */
@Extension(id="scalefree")
public final class ScaleFreePopulator implements GraphPopulator {

//    /**
//     * random number generator.
//     */
//    private Random random = new Random();
    private Uniform uniform = new Uniform(RandomEngineRegistry.getEngine());

    /**
     * number of seed nodes to generate
     */
    private int seed;

    /**
     * number of nodes to generate
     */
    private int numberOfNodes;

    /**
     * gets the number of seed nodes.
     * @return the number of seed nodes.
     */
    public int getSeed() {
        return seed;
    }

    /**
     * sets the number of seed nodes.
     * @param seed number of seed nodes
     */
    @ExtensionProperty(id="seed")
    public void setSeed(int seed) {
        this.seed = seed;
    }

    /**
     * gets the number of nodes
     * @return the number of nodes.
     */
    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    /**
     * sets the number of nodes.
     * @param numberOfNodes the number of nodes.
     */
    @ExtensionProperty(id="numNodes")
    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }


    /**
     * populates the graph to form a scale-free network using
     * preferential attachment.
     * @param graph the graph to populate.
     */
    @Override
    public void populate(Graph graph) {

        int lastNode = 0;

        //Create seeds
        List<Node> seeds = new ArrayList<Node>();
        for (int i = 0; i < seed; i++) {
            seeds.add(graph.createNode(++lastNode +""));
        }
        for (int i = 0; i < seeds.size(); i++) {
            Node node_i = seeds.get(i);
            for (int j = 0; j < i; j++) {
                Node node_j = seeds.get(j);
                graph.createEdge(new SetOfTwo<Node>(node_i,node_j));
            }
        }

        int sumOfDegrees = graph.getNumEdges() * 2;

        for (int i = 0; i < numberOfNodes - seed; i++) {

            int r = uniform.nextIntFromTo(1, sumOfDegrees);
            int beam = 0;

            boolean created = false;

            Iterator<Node> nodes = graph.getNodes().iterator();

            while (nodes.hasNext() && !created) {

                Node node = nodes.next();

                beam += node.degree();

                if (beam >= r) {

                    Node newNode = graph.createNode(++lastNode +"");
                    Edge e = graph.createEdge(new SetOfTwo<Node>(node, newNode));

                    sumOfDegrees += 2;
                    created = true;
                }
            }
            
            if (!created) {
                Logger.getLogger(this.getClass().getCanonicalName()).severe("This is a bug! Beam: "+beam+"\tRandom: "+r);
            }
            
        }

    }

    /**
     * returns a string summarising the current configuration.
     * @return a string summarising the current configuration.
     */
    @Override
    public String configSummary() {
        return "\"" + this.getClass().getSimpleName() +
                "\" {nodes: " + numberOfNodes + "; seeds: "+ seed + "}";
    }

    

}
