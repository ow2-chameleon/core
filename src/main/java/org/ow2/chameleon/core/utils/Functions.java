/*
 * Copyright 2013 OW2 Chameleon
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ow2.chameleon.core.utils;

import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.List;

/**
 * Some functional utilities built on top of Guava.
 */
public class Functions {

    /**
     * Maps a given <em>set</em> to another <em>set</em> by applying the <em>mapping</em> function to all elements of
     * the input <em>set</em>. The output <em>set</em> does not contain {@literal null} objects. So if the mapping
     * function returns {@literal null} for an element of the input <em>set</em>, it is ignored. So this methods is
     * somewhat similar to <em>mapAndFilter</em>.
     * @param from the input set
     * @param mapping the mapping function
     * @param <I> the input type (argument type of the mapping function)
     * @param <O> the output type (return type of the mapping function)
     * @return a new Iterable containing all transformed element from the input set,
     * except when the mapping function has returned {@literal null}.
     */
    public static <I, O> Iterable<O> map(Iterable<I> from, Function<I, O> mapping) {
        List<O> output = new ArrayList<O>();
        for (I in : from) {
            O result = mapping.apply(in);
            if (result != null) {
                output.add(result);
            }
        }
        return output;
    }

}
