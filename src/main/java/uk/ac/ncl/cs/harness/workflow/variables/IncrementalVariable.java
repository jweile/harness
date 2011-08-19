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
 * This is a variable that starts with a minimum value and performs increment
 * operations by a certain value at each step until a maximum value is reached.
 *
 * @author Jochen Weile, M.Sc.
 */
public final class IncrementalVariable extends Variable<Double> {

    /**
     * the start value (minimal value)
     */
    double min;

    /**
     * The end value (maximum value)
     */
    double max;

    /**
     * the incremental value.
     */
    double inc;

    /**
     * the current value.
     */
    double currVal;

    /**
     * constructor
     * @param id the id of the variable
     * @param min the start value (minimal value)
     * @param max The end value (maximum value)
     * @param inc the incremental value.
     */
    public IncrementalVariable(String id, double min, double max, double inc) {
        super(id);
        this.min = min;
        this.max = max;
        this.inc = inc;

        reset();
    }

    /**
     * resets the variable to the start value.
     */
    @Override
    public void reset() {
        currVal = min;
    }

    /**
     * whether or not the variable can perform another step operation.
     * @return whether or not the variable can perform another step operation.
     */
    @Override
    public boolean canStep() {
        return currVal+inc <= max;
    }

    /**
     * perform a step
     */
    @Override
    public void step() {
        if (canStep()) {
            currVal += inc;
        }
    }

    /**
     * returns the variable's current value.
     * @return the variable's current value.
     */
    @Override
    public Double getCurrentValue() {
        return currVal;
    }

    @Override
    public int getNumTotalIterations() {
        return (int)((max - min) / inc) + 1;
    }

    

}
