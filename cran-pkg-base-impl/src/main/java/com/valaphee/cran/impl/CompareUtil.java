/*
 * Copyright (c) 2022, Valaphee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.valaphee.cran.impl;

/**
 * @author Kevin Ludwig
 */
public class CompareUtil {
    private CompareUtil() {
    }

    public static int compare(Object inA, Object inB) {
        if (inA instanceof Comparable<?>) {
            return ((Comparable<Object>) inA).compareTo(inB);
        } else /*if (inB instanceof Comparable<?>) {
            return ((Comparable<Object>) inB).compareTo(inA);
        } else */ {
            return Integer.MAX_VALUE;
        }
    }
}
