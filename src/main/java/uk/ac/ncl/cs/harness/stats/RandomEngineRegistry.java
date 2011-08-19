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

package uk.ac.ncl.cs.harness.stats;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import cern.jet.random.engine.RandomSeedGenerator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class RandomEngineRegistry {

    private static RandomSeedGenerator seeds = new RandomSeedGenerator();

    private static Map<Thread,RandomEngine> engines = new HashMap<Thread, RandomEngine>(128);

    public static RandomEngine getEngine() {
        Thread t = Thread.currentThread();
        RandomEngine engine = engines.get(t);
        if (engine == null) {
            engine = new MersenneTwister(seeds.nextSeed());
            engines.put(t, engine);
        }
        return engine;
    }

    public static void deregister() {
        Thread t = Thread.currentThread();
        engines.remove(t);
    }


}
