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

package uk.ac.ncl.cs.harness.workflow.variables;

/**
 * <p>Variables can iterate over a number of values. Example usage:</p>
 * <pre><code>
 * Variable v = new Type01LogarithmicVariable("test",4);
 * while (v.canStep() {
 *    double val = v.getCurrentValue();
 *    step();
 * }
 * </code></pre>
 *
 * @author Jochen Weile, M.Sc.
 */
public abstract class Variable<T> {

    /**
     * the variable's ID
     */
    private String id;

    /**
     * Creates a variable with ID id
     */
    protected Variable(String id) {
        this.id = id;
    }

    /**
     * gets the variable's ID
     */
    public String getId() {
        return id;
    }

    /**
     * resets to start value
     */
    public abstract void reset();

    /**
     * whether or not the variable can perform another step operation.
     */
    public abstract boolean canStep();

    /**
     * step to the next value if possible
     */
    public abstract void step();

    /**
     * returns the variable's current value.
     */
    public abstract T getCurrentValue();

    /**
     * returns the number of total iterations through which this variable cycles.
     */
    public abstract int getNumTotalIterations();


}
