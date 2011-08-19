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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import uk.ac.ncl.cs.harness.io.ResourcePaths;

/**
 * Logging system initialisation. Calling <code>LoggingInit.init();</code>
 * will read the local logging.config file.
 *
 * @author Jochen Weile, M.Sc.
 */
public class LoggingInit {

    /**
     * Points to the logging config file.
     */
    public static final String FILE_NAME = "logging.config";

    /**
     * Reads the logging config file and applies its settings.
     */
    public static void init() {

        try {

            InputStream in = ClassLoader.getSystemResourceAsStream(FILE_NAME);
            
            if (in == null) {

                System.err.println("WARNING: Unable to obtain logging "
                        + "configuration file from classpath! Trying to "
                        + "use IDE file...");

                File file = new File(ResourcePaths.RESOURCES, FILE_NAME);
                in = new FileInputStream(file);

            }

            LogManager logManager = LogManager.getLogManager();
            logManager.readConfiguration(new BufferedInputStream(in));

        } catch (IOException ex) {
            throw new RuntimeException("Could not find logging configuration "
                    + "file! Report this as a bug.",ex);
        }

    }

}
