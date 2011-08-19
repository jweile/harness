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

package uk.ac.ncl.cs.harness.exceptions;

/**
 * Indicates that a problem has occurred within the graph.
 * @author Jochen Weile, M.Sc.
 */
public class GraphException extends RuntimeException {

    /**
     * Constructor with cause.
     * @param cause The cause of the exception.
     */
    public GraphException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor with message and cause.
     * @param message A message.
     * @param cause The cause of the exception.
     */
    public GraphException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor with message.
     * @param message A message.
     */
    public GraphException(String message) {
        super(message);
    }

    /**
     * Empty constructor.
     */
    public GraphException() {
    }

    

}
