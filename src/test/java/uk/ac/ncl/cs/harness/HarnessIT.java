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

package uk.ac.ncl.cs.harness;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import junit.framework.TestCase;
import uk.ac.ncl.cs.harness.io.ResourcePaths;

/**
 *
 * @author jweile
 */
public class HarnessIT extends TestCase {
    
    public HarnessIT(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIntegration() throws Exception {

        File jarFile = new File(new File("target"),
                "harness-0.0.1-SNAPSHOT-jar-with-dependencies.jar");

        File protocolFile = new File(ResourcePaths.TEST_RESOURCES, "protocol.xml");

        String[] execArgs = new String[] {"java", "-jar", 
                                jarFile.getCanonicalPath(), "-t failsafe",
                                protocolFile.getCanonicalPath()};

        Process process = Runtime.getRuntime().exec(execArgs);

        InputStream errorStream = process.getErrorStream();
        BufferedReader errorReader = new BufferedReader(
                                        new InputStreamReader(errorStream)
                                     );

        String line;
        while ((line = errorReader.readLine()) != null) {
            System.err.println(line);
        }
        errorReader.close();

        process.waitFor();

        assertEquals(0, process.exitValue());

    }

}
