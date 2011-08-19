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

package uk.ac.ncl.cs.harness.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.xml.sax.SAXException;
import uk.ac.ncl.cs.harness.exceptions.ConfigurationException;
import uk.ac.ncl.cs.harness.workflow.Properties;
import uk.ac.ncl.cs.harness.workflow.Protocol;
import uk.ac.ncl.cs.harness.workflow.variables.IncrementalVariable;
import uk.ac.ncl.cs.harness.workflow.variables.Type01LogarithmicVariable;
import uk.ac.ncl.cs.harness.workflow.variables.Variable;

/**
 * Parses a given protocol XML file to create a <code>Protocol</code> 
 * object.
 *
 * @author Jochen Weile, M.Sc.
 */
public class ProtocolParser {

    /**
     * a map linking variable identifiers to variables.
     */
    private Map<String,Variable> vars;

    /**
     * The xml namespace.
     */
    private Namespace ns;

    /**
     * this method validates a given protocol xml file.
     * @param protocolFile the protocol xml file to validate
     * @throws ConfigurationException if the file is invalid or could not be read.
     */
    public void validate(File protocolFile) throws ConfigurationException {

        Source xmlFile = new StreamSource(protocolFile);

        SchemaFactory schemaFactory = SchemaFactory
            .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        try {
            InputStream schemaStream = ClassLoader.getSystemResourceAsStream("harness.xsd");
            if (schemaStream == null) {
                Logger.getLogger(this.getClass().getName())
                        .warning("Protocol XSD not found in classpath. Trying to use IDE file...");
                schemaStream = new FileInputStream(new File(ResourcePaths.RESOURCES,"harness.xsd"));
            }
            Source schemaSource = new StreamSource(schemaStream);
            Schema schema = schemaFactory.newSchema(schemaSource);

            Validator validator = schema.newValidator();
            try {
                
                validator.validate(xmlFile);
                
            } catch (IOException e) {
                throw new ConfigurationException("Protocol XML file could not be read!",e);
            } catch (SAXException e) {
                throw new ConfigurationException("Protocol XML file is not valid!",e);
            }
        } catch (SAXException e) {
            throw new RuntimeException("XSD file is corrupt. Report this as a bug!",e);
        } catch (IOException e) {
            throw new RuntimeException("XSD file is not accessable. Report this as a bug!",e);
        }

    }

    /**
     * Parses a given protocol XML file to create a <code>Protocol</code>
     * object.
     *
     * @param file a protocol XML file
     * @return a protocol object representing the contents of the given file.
     * @throws ConfigurationException if the XML file contains invalid contents.
     * @throws IOException if the file cannot be read.
     */
    public Protocol parse(File file) throws ConfigurationException, IOException {

        Protocol protocol = new Protocol();

        try {

            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(file);

            Element protocolXML = document.getRootElement();
            ns = protocolXML.getNamespace();

            vars = parseVariables(protocolXML.getChild("variables",ns));
            protocol.setVariables(vars);

            String graphImplementation = protocolXML.getChild(
                    "graphImplementation",ns).getAttributeValue("type");
            protocol.setGraphImplementation(graphImplementation);

            Element populationXML = protocolXML.getChild("graphPopulation",ns);
            Properties populationProps = parseProperties(populationXML
                    .getChild("properties",ns));
            populationProps.setProperty(Properties.ID_PROPERTY,
                    populationXML.getAttributeValue("type"));
            protocol.setGraphPopulation(populationProps);

            Element integrationXML = protocolXML.getChild("integration",ns);

            int cycleReplicas = Integer.parseInt(integrationXML.getAttributeValue("cycleReplicas"));
            protocol.setCycleReplicas(cycleReplicas);

            Properties integrationProps = parseProperties(integrationXML
                    .getChild("properties",ns));
            integrationProps.setProperty(Properties.ID_PROPERTY, integrationXML
                    .getAttributeValue("type"));
            protocol.setIntegrationType(integrationProps);

            Set<Properties> experiments = parseExperiments(integrationXML
                    .getChild("evidentialGraphs",ns));
            protocol.setExperiments(experiments);

            Set<Properties> goldStandards = parseExperiments(integrationXML
                    .getChild("goldStandards",ns));
            protocol.setGoldStandards(goldStandards);


        } catch (JDOMException e) {
            throw new ConfigurationException("Invalid protocol file!");
        }

        return protocol;
    }

    /**
     * parses the variable section of the file
     * @param varListXML XML DOM element for the variable list
     * @return a map linking variable ids to the variables
     * @throws ConfigurationException if the element is malformed
     */
    private Map<String,Variable> parseVariables(Element varListXML) throws ConfigurationException {

        if (varListXML == null || varListXML.getChildren().isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        vars = new HashMap<String,Variable>();

        Iterator varIt = varListXML.getChildren().iterator();
        while (varIt.hasNext()) {

            Variable var = null;

            Element varXML = (Element) varIt.next();
            String varType = varXML.getName();
            String varId = varXML.getAttributeValue("id");

            if (varType.equals("incrementalVariable")) {

                /*
                 * we don't check for null arguments or invalid
                 * types here because they should have been
                 * detected by the xsd validation already.
                 */
                String minStr = varXML.getAttributeValue("min");
                double min = Double.parseDouble(minStr);

                String maxStr = varXML.getAttributeValue("max");
                double max = Double.parseDouble(maxStr);

                String incStr = varXML.getAttributeValue("inc");
                double inc = Double.parseDouble(incStr);

                var = new IncrementalVariable(varId, min, max, inc);

            } else if (varType.equals("logarithmicVariable")) {

                String rangeStr = varXML.getAttributeValue("range");

                String iterStr = varXML.getAttributeValue("iterations");
                int iterations = Integer.parseInt(iterStr);

                if (rangeStr.equals(Type01LogarithmicVariable.TAG)) {
                    var = new Type01LogarithmicVariable(varId, iterations);
//              } else if (rangeStr.equals(???.TAG)) {//TODO insert more variable types here
                } else {
                    throw new ConfigurationException("Unknown logarithmic variable range: "
                            +rangeStr);
                }

            } else {
                //This should not happen, as it would be detected in the validation step.
                throw new ConfigurationException("Unknown variable type:"+varType);
            }

            vars.put(varId, var);
            
        }

        return vars;
    }

    /**
     * parses a propertyList XML element
     * @param propertiesXML the propertyList XML DOM element (can be null)
     * @return a properties object containing the parsed contents. If a
     * null-pointer is given to the method, an empty properties object is returned.
     * @throws ConfigurationException if the XML element is malformed.
     */
    private Properties parseProperties(Element propertiesXML) throws ConfigurationException {
        Properties properties = new Properties();

        if (propertiesXML != null) {

            Iterator propIt = propertiesXML.getChildren().iterator();
            while(propIt.hasNext()) {
                Element propertyXML = (Element) propIt.next();

                boolean isVarRef = propertyXML.getName().equals("variableProperty");

                String key = propertyXML.getAttributeValue("key");
                String type = propertyXML.getAttributeValue("type");

                if (isVarRef) {
                    String varRef = propertyXML.getAttributeValue("var-ref");
                    if (vars.containsKey(varRef)) {
                        properties.setProperty(key, vars.get(varRef));
                    } else {
                        throw new ConfigurationException("Undefined variable: "+varRef);
                    }
                } else {
                    try {
                        String value = propertyXML.getAttributeValue("value");
                        properties.setProperty(key, applyType(type, value));
                    } catch (NumberFormatException nfe) {
                        throw new ConfigurationException("Cannot assign type "+
                                type+" to variable "+key, nfe);
                    }
                }
            }
            
        }

        return properties;
    }

    /**
     * casts a string value to the correct type corresponding to the given xsd type.
     * @param type the xsd type
     * @param value the data value
     * @return the correctly cast object.
     */
    private Object applyType(String type, String value) {
        //TODO: extend to cover more xsd types
        if (type.equals("xsd:integer") || type.equals("xsd:int")) {
            return Integer.parseInt(value);
        } else if (type.equals("xsd:double") || type.equals("xsd:float")) {
            return Double.parseDouble(value);
        } else if (type.equals("xsd:boolean")) {
            return Boolean.parseBoolean(value);
        } else if (type.equals("xsd:string")) {
            return value;
        } else {
//            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
//                    "Unknown property value datatype: {0}", type);
            throw new NumberFormatException("Unknown datatype "+type);
        }
    }

    /**
     * parses experiment lists like the list of gold standards or the list
     * of evidential experiments.
     * @param experimentsXML the experiments XML DOM element. can be null
     * @return a set of properties describing the experiments. or an empty set
     * if the given experiments xml element is null.
     * @throws ConfigurationException if the XML is malformed
     */
    private Set<Properties> parseExperiments(Element experimentsXML)
            throws ConfigurationException {
        Set<Properties> expPropSet = new HashSet<Properties>();

        if (experimentsXML != null) {

            Iterator expIt = experimentsXML.getChildren().iterator();
            while (expIt.hasNext()) {

                Element expXML = (Element) expIt.next();

                Properties props = parseProperties(expXML.getChild("properties",ns));

                props.setProperty(Properties.ID_PROPERTY,
                        expXML.getAttributeValue("type"));

                //FIXME: unsafe!
                Element replicas = expXML.getChild("variableReplicas",ns);
                if (replicas != null) {
                    String varRef = replicas.getAttributeValue("var-ref");
                    if (vars.containsKey(varRef)) {
                        props.setProperty(Properties.REPLICAS_PROPERTY,vars.get(varRef));
                    } else {
                        throw new ConfigurationException("Undefined variable: "+varRef);
                    }
                } else {
                    replicas = expXML.getChild("staticReplicas",ns);
                    String valStr = replicas.getAttributeValue("value");
                    int value = Integer.parseInt(valStr);
                    props.setProperty(Properties.REPLICAS_PROPERTY,value);
                }

                expPropSet.add(props);

            }
        }

        return expPropSet;
    }

}
