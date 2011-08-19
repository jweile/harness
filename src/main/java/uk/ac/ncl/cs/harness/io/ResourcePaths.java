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

/**
 * This class caches paths to the project's resource directories.
 * 
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public interface ResourcePaths {

    public static final File RESOURCES = new File(new File(new File("src"),"main"),"resources");

    public static final File TEST_RESOURCES = new File(new File(new File("src"),"test"),"resources");

    public static final File OUTPUT_DIR = new File("output");
    
}
