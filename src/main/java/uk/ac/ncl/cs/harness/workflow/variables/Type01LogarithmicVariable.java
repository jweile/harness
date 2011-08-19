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
 * This variable logarithmically approaches 1,
 * taking the values 0.9, 0.99, 0.999 etc.
 *
 * @author Jochen Weile, M.Sc.
 */
public final class Type01LogarithmicVariable extends Variable<Double> {

    /**
     * The variable's TAG, used by the protocol parser
     */
    public static final String TAG = "0-1";

    /**
     * number of iterations to perform
     */
    private int iterations;

    /**
     * current iteration
     */
    private int currIt;

    /**
     * constructor with id and number iterations to perform.
     * @param id the variable's ID.
     * @param iterations number of iterations to perform.
     */
    public Type01LogarithmicVariable(String id, int iterations) {
        super(id);
        this.iterations = iterations;

        reset();
    }

    /**
     * resets to start value
     */
    @Override
    public void reset() {
        currIt = 1;
    }

    /**
     * whether or not the variable can perform another step operation.
     * @return whether or not the variable can perform another step operation.
     */
    @Override
    public boolean canStep() {
        return currIt <= iterations;
    }

    /**
     * perform a step
     */
    @Override
    public void step() {
        if (canStep()) {
            currIt++;
        }
    }

    /**
     * returns the variable's current value.
     * @return the variable's current value.
     */
    @Override
    public Double getCurrentValue() {
        return 1.0 - Math.pow(10.0, -currIt);
    }

    @Override
    public int getNumTotalIterations() {
        return iterations;
    }

    

}
