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

package uk.ac.ncl.cs.harness.graph;

/**
 * This class models a probability on a graph's edge
 * @author Jochen Weile
 */
public class Probability {

    /**
     * Key for the edge's primary probability
     */
    public static final String MAIN_KEY = "MainProbability";

    /**
     * the probability's id.
     */
    private String key;

    /**
     * the actual probability value.
     */
    private double value;

    /**
     * constructor with id and probability value
     * @param key
     * @param value
     */
    public Probability(String key, double value) {
        this.key = key;
        this.value = value;
    }

    /**
     * gets the key
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * sets the key
     * @param key the key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * sets the prob. value.
     * @param value the prob. value.
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * gets the prob. value
     * @return the prob. value.
     */
    public double getValue() {
        return value;
    }

    /**
     * equals method based on the key
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Probability other = (Probability) obj;
        if ((this.key == null) ? (other.key != null) : !this.key.equals(other.key)) {
            return false;
        }
        return true;
    }

    /**
     * hash code based on the key
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.key != null ? this.key.hashCode() : 0);
        return hash;
    }

    

}
