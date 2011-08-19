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

package uk.ac.ncl.cs.harness.extsupport;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import uk.ac.ncl.cs.harness.exceptions.ConfigurationException;
import uk.ac.ncl.cs.harness.experiments.Experiment;
import uk.ac.ncl.cs.harness.graph.Graph;
import uk.ac.ncl.cs.harness.graph.populators.GraphPopulator;
import uk.ac.ncl.cs.harness.integration.IntegrationMethod;
import uk.ac.ncl.cs.harness.workflow.Properties;

/**
 * <p>The extension registry is a singleton object that can be queried for extensions
 * to the harness. Extensions are subclasses of <code>Experiment</code>, <code>Graph</code>,
 * <code>GraphPopulator</code> or <code>IntegrationMethod</code>. All extensions are
 * registered with their ID (as defined by the <code>@Extension</code> annotation.</p>
 * 
 * <p>All extensions currently found in the classpath are automatically registered.
 * To query for a specific extension use the appropriate querying method. For example:
 * <code>ExtensionRegistry.getInstance().getGraphImplementation("mapgraph");</code> will
 * return the graph implementation with the ID <code>mapgraph</code>.</p>
 *
 *
 * @author Jochen Weile, M.Sc.
 */
public class ExtensionRegistry {

    /**
     * The singleton object.
     */
    private static ExtensionRegistry instance;

    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(ExtensionRegistry.class.getName());

    /**
     * The experiments registry links experiment IDs to their respective classes.
     */
    private Map<String,Class<? extends Experiment>> experiments =
            new HashMap<String, Class<? extends Experiment>>();

    /**
     * The populators registry links graph populator IDs to their respective classes.
     */
    private Map<String,Class<? extends GraphPopulator>> populators =
            new HashMap<String, Class<? extends GraphPopulator>>();

    /**
     * The graphimpls registry links graph implementation IDs to their respective classes.
     */
    private Map<String,Class<? extends Graph>> graphimpls =
            new HashMap<String, Class<? extends Graph>>();

    /**
     * The integrations registry links integration method IDs to their respective classes.
     */
    private Map<String,Class<? extends IntegrationMethod>> integrations =
            new HashMap<String, Class<? extends IntegrationMethod>>();

    /**
     * private constructor, to enforce singleton.
     */
    private ExtensionRegistry() {
        try {
            scan();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to scan classpath!",ex);
        }
    }

    /**
     * Returns the singleton instance.
     * @return the singleton instance.
     */
    public static ExtensionRegistry getInstance() {
        if (instance == null) {
            instance = new ExtensionRegistry();
        }
        return instance;
    }

    /**
     * Scans the current classpath for extensions and registers them.
     * @throws IOException If elements of the classpath cannot be read.
     */
    private void scan() throws IOException {

        ClassLoader classLoader = this.getClass().getClassLoader();

        MetadataReaderFactory mdf = new SimpleMetadataReaderFactory();

        AnnotationTypeFilter genericFilter =
                new AnnotationTypeFilter(Extension.class);
        AssignableTypeFilter experimentFilter =
                new AssignableTypeFilter(Experiment.class);
        AssignableTypeFilter populatorFilter =
                new AssignableTypeFilter(GraphPopulator.class);
        AssignableTypeFilter graphImplFilter =
                new AssignableTypeFilter(Graph.class);
        AssignableTypeFilter integrationFilter =
                new AssignableTypeFilter(IntegrationMethod.class);

        PathMatchingResourcePatternResolver resolver =
                new PathMatchingResourcePatternResolver();

        Resource[] resources = resolver
                .getResources("classpath*:uk/ac/ncl/cs/harness/**/*.class");

        for (Resource resource : resources) {

            MetadataReader mdr = mdf.getMetadataReader(resource);

            if (genericFilter.match(mdr, mdf)) {
                
                if (experimentFilter.match(mdr, mdf)) {

                    try {

                        Class<? extends Experiment> clazz =
                                    classLoader.loadClass(mdr.getClassMetadata()
                                    .getClassName()).asSubclass(Experiment.class);

                        Extension extension = clazz.getAnnotation(Extension.class);

                        if (extension != null) {

                            experiments.put(extension.id(), clazz);

                            logger.log(Level.FINE, "Registered experiment: {0} -> {1}",
                                    new Object[]{extension.id(),
                                    mdr.getClassMetadata().getClassName()});
                        }

                    } catch (ClassNotFoundException ex) {
                        logger.log(Level.SEVERE, "This is a bug!", ex);
                    }

                } else if (populatorFilter.match(mdr, mdf)) {

                    try {

                        Class<? extends GraphPopulator> clazz =
                                classLoader.loadClass(mdr.getClassMetadata()
                                .getClassName()).asSubclass(GraphPopulator.class);

                        Extension extension = clazz.getAnnotation(Extension.class);

                        if (extension != null) {

                            populators.put(extension.id(), clazz);

                            logger.log(Level.FINE, "Registered populator: {0} -> {1}",
                                    new Object[]{extension.id(),
                                    mdr.getClassMetadata().getClassName()});
                        }

                    } catch (ClassNotFoundException ex) {
                        logger.log(Level.SEVERE, "This is a bug!", ex);
                    }

                } else if (graphImplFilter.match(mdr, mdf)) {

                    try {
                        Class<? extends Graph> clazz =
                                classLoader.loadClass(mdr.getClassMetadata()
                                .getClassName()).asSubclass(Graph.class);

                        Extension extension = clazz.getAnnotation(Extension.class);

                        if (extension != null) {

                            graphimpls.put(extension.id(), clazz);

                            logger.log(Level.FINE, "Registered graph: {0} -> {1}",
                                    new Object[]{extension.id(),
                                    mdr.getClassMetadata().getClassName()});
                        }

                    } catch (ClassNotFoundException ex) {
                        logger.log(Level.SEVERE, "This is a bug!", ex);
                    }

                } else if (integrationFilter.match(mdr, mdf)) {

                    try {

                        Class<? extends IntegrationMethod> clazz =
                                classLoader.loadClass(mdr.getClassMetadata()
                                .getClassName()).asSubclass(IntegrationMethod.class);

                        Extension extension = clazz.getAnnotation(Extension.class);

                        if (extension != null) {
                            integrations.put(extension.id(), clazz);

                            logger.log(Level.FINE, "Registered integration: {0} -> {1}",
                                    new Object[]{extension.id(),
                                    mdr.getClassMetadata().getClassName()});
                        }

                    } catch (ClassNotFoundException ex) {
                        logger.log(Level.SEVERE, "This is a bug!", ex);
                    }

                }
            }

        }

    }

    /**
     * Instantiates and configures a graph populator with the given properties.
     * 
     * @param properties a properties object containing the populators ID under
     * the key <code>Properties.ID_PROPERTY</code> and all other required properties
     * specific to the respective implementation (i.e. <code>numNodes</code> for the
     * scalefree populator).
     * 
     * @return The fully configured graph populator.
     * 
     * @throws ConfigurationException if the instantiation and configuration did not succeed.
     */
    public GraphPopulator getPopulator(Properties properties) throws ConfigurationException {

        String id = (String) properties.getProperty(Properties.ID_PROPERTY);

        try {

            Class<? extends GraphPopulator> clazz = populators.get(id);
            GraphPopulator pop = clazz.newInstance();

            setProperties(pop, properties);
            
            return pop;

        } catch (Exception ex) {
            throw new ConfigurationException("Cannot instantiate populator. ",ex);
        }

    }

   
    /**
     * Instantiates a graph with the given implementation id.
     * @param id The implementation ID.
     * @return a new graph object.
     * @throws ConfigurationException if the instantiaton fails.
     */
    public Graph getGraphImplementation(String id) throws ConfigurationException {

        Class<? extends Graph> clazz = graphimpls.get(id);
        if (clazz != null) {

            try {
                Graph graph = clazz.newInstance();
                return graph;
            } catch (Exception ex) {
                throw new ConfigurationException("Cannot instantiate graph implementation. ",ex);
            }

        } else {
            throw new ConfigurationException("Unknown graph implementation: "+id);
        }

    }

    /**
     * Instantiates and configures an experiment with the given properties.
     * 
     * @param properties A properties object containing the experiment's ID under
     * the key <code>Properties.ID_PROPERTY</code> and all other required properties
     * specific to the respective implementation (i.e. <code>sensitivity</code> for
     * simpleexperiment).
     * 
     * @return The fully configured experiment object.
     * 
     * @throws ConfigurationException if the instantiation and configuration did not succeed.
     */
    public Experiment getExperiment(Properties properties) throws ConfigurationException {
        String id = (String) properties.getProperty(Properties.ID_PROPERTY);

        try {

            Class<? extends Experiment> clazz = experiments.get(id);
            Experiment exp = clazz.newInstance();

            setProperties(exp, properties);

            return exp;

        } catch (Exception ex) {
            throw new ConfigurationException("Cannot instantiate experiment. ",ex);
        }
    }

    /**
     * Instantiates and configures an integration method with the given properties.
     * 
     * @param properties A properties object containing the integration method's ID under
     * the key <code>Properties.ID_PROPERTY</code> and all other required properties
     * specific to the respective implementation.
     * 
     * @return The fully configured integration method object.
     * 
     * @throws ConfigurationException if the instantiation and configuration did not succeed.
     */
    public IntegrationMethod getIntegrationMethod(Properties properties) throws ConfigurationException {

        String id = (String) properties.getProperty(Properties.ID_PROPERTY);

        try {

            Class<? extends IntegrationMethod> clazz = integrations.get(id);
            IntegrationMethod integration = clazz.newInstance();

            setProperties(integration, properties);

            return integration;

        } catch (Exception ex) {
            throw new ConfigurationException("Cannot instantiate integration method. ",ex);
        }
    }

    /**
     * Applies all properties in the given <class>Properties</code> object
     * except for the ID and Replicas properties to an extension object.
     * @param extension The extension object.
     * @param properties The properties to apply.
     * @throws Exception if the property application failed.
     */
    private void setProperties(Object extension, Properties properties) throws Exception {

       for (String propKey : properties.getKeys()) {

            if (propKey.equals(Properties.ID_PROPERTY) ||
                    propKey.equals(Properties.REPLICAS_PROPERTY)) {
                continue;
            }

            Method method = extractMethod(extension.getClass(), propKey);
            method.invoke(extension, properties.getProperty(propKey));

        }

    }

    /**
     * Identifies the method with the <code>@ExtensionProperty</code> ID that
     * matches the given key.
     * @param clazz The class from which the method is to be extracted.
     * @param propKey the desired <code>@ExtensionProperty</code> ID
     * @return the matching method.
     * @throws ConfigurationException if the method could be found.
     */
    private Method extractMethod(Class<?> clazz, String propKey) throws ConfigurationException {

        for (Method method : clazz.getMethods()) {

            ExtensionProperty ep = method.getAnnotation(ExtensionProperty.class);
            if (ep != null && ep.id().equals(propKey)) {
                return method;
            }
        }

        throw new ConfigurationException("Method property "+propKey+" unknown.");
    }

}
