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
import java.util.logging.Logger;
import junit.framework.TestCase;
import uk.ac.ncl.cs.harness.LoggingInit;
import uk.ac.ncl.cs.harness.exceptions.ConfigurationException;
import uk.ac.ncl.cs.harness.workflow.Properties;
import uk.ac.ncl.cs.harness.workflow.Protocol;

/**
 *
 * @author jweile
 */
public class ProtocolParserTest extends TestCase {
    
    public ProtocolParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        LoggingInit.init();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testValidation() throws Exception {

        File protocolFile = new File(ResourcePaths.TEST_RESOURCES,
                "protocol.xml");

        ProtocolParser parser = new ProtocolParser();

        parser.validate(protocolFile);

        protocolFile = new File(ResourcePaths.TEST_RESOURCES,
                "invalid_protocol.xml");

        ConfigurationException conf = null;
        try {
            parser.validate(protocolFile);
        } catch(ConfigurationException e) {
            conf = e;
            Logger.getLogger(this.getClass().getName()).info(e.getMessage());
        }
        assertNotNull(conf);

    }

    public void testParsing() throws Exception {

        File protocolFile = new File(ResourcePaths.TEST_RESOURCES, "protocol.xml");

        ProtocolParser parser = new ProtocolParser();

        Protocol protocol = parser.parse(protocolFile);

        assertEquals("Variables", 1, protocol.getVariables().size());

        assertEquals("Graph impl","mapgraph",protocol.getGraphImplementation());

        assertEquals("Population props", 3, protocol.getGraphPopulation().getKeys().size());

        assertEquals("Integration props", 1, protocol.getIntegrationType().getKeys().size());

        assertEquals("Experiment types", 1, protocol.getExperiments().size());
        for (Properties p : protocol.getExperiments()) {
            assertEquals("Experiment props", 4, p.getKeys().size());
        }

        assertEquals("Gold standard types", 1, protocol.getGoldStandards().size());
        for (Properties p : protocol.getGoldStandards()) {
            assertEquals("Gold standard props", 4, p.getKeys().size());
        }
    }

}
