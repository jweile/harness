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

package uk.ac.ncl.cs.harness.workflow;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import uk.ac.ncl.cs.harness.workflow.variables.Variable;

/**
 * Stores a set of properties, which can potentially be mutable variables.
 *
 * @author Jochen Weile, M.Sc.
 */
public class Properties {

    /**
     * The key of to the ID property.
     */
    public static final String ID_PROPERTY = "ID_PROP";

    /**
     * The key to the "replicates" property.
     */
    public static final String REPLICAS_PROPERTY = "REPL_PROP";

    /**
     * internal map that stores the properties under their string keys, but
     * references intermediate ObjRef objects, which resolve potential variables.
     */
    private Map<String,ObjRef> map = new HashMap<String, ObjRef>();

    /**
     * sets a property, this can either be an actual value or a variable.
     * @param key
     * @param val
     */
    public void setProperty(String key, Object val) {
        map.put(key, new ObjRef(val));
    }

    /**
     * gets a property
     * @param key the property key
     * @return the property value (either a static value or the current state
     * of the referenced variable.
     */
    public Object getProperty(String key) {
        return map.get(key).getValue();
    }

    /**
     * gets the set of all stored property keys.
     * @return
     */
    public Set<String> getKeys() {
        return map.keySet();
    }

    /**
     * internal helper class that provides a black box which abstracts over static
     * values or variable references.
     */
    private class ObjRef {

        /**
         * a static value.
         */
        private Object value;

        /**
         * whether or not this is a variable reference.
         */
        private boolean isRef;

        /**
         * a variable.
         */
        private Variable var;

        /**
         * this constructor automatically infers whether this is a variable reference
         * or a static value.
         * @param o the reference.
         */
        public ObjRef(Object o) {
            if (o instanceof Variable) {
                var = (Variable) o;
                isRef = true;
            } else {
                value = o;
                isRef = false;
            }
        }

        /**
         * retrieves value. automatically de-references potential variables.
         */
        public Object getValue() {
            if (isRef) {
                return var.getCurrentValue();
            } else {
                return value;
            }
        }

    }
}
