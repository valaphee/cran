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

package com.valaphee.cran.graph.glsl

import com.valaphee.cran.path.DataPathException
import com.valaphee.cran.path.glsl.Variable

/**
 * @author Kevin Ludwig
 */
class Scope {
    private val variables = mutableListOf<Variable?>()

    fun define(dataPathId: Int, name: String, code: String, vararg dependencies: Variable) {
        if (dataPathId > variables.size) repeat(dataPathId - variables.size) { variables += null }
        if (dataPathId == variables.size)
        if (variables[dataPathId] != null) throw DataPathException.AlreadySet else variables[dataPathId] = Variable(name, code, dependencies.toList())
    }

    fun variable(dataPathId: Int) = variables.getOrNull(dataPathId) ?: throw DataPathException.Undefined
}
