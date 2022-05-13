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

package com.valaphee.flow.spec

/**
 * Default types, numbers are reserved for dynamic types
 *
 * There are also some special notations:
 * a=b indicates that the value b is mapped to a (multiple inputs)
 *
 * @author Kevin Ludwig
 */
object DataType {
    /**
     * Binary, can be true or false
     */
    const val Bin = "-"

    /**
     * Number, can be an integer or decimal of any size
     */
    const val Num = "~"

    /**
     * String, can also represent binary
     */
    const val Str = "\""

    /**
     * Array, can be followed by a type specifier
     */
    const val Arr = "["

    /**
     * "Object", key-value pairs
     */
    const val Obj = "{"
}
