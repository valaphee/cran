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

package com.valaphee.cran.node.logic

import com.valaphee.cran.graph.Scope
import com.valaphee.cran.node.NodeJvm
import com.valaphee.cran.spec.NodeDef

/**
 * @author Kevin Ludwig
 */
@NodeDef("jvm", Not::class)
object NotJvm : NodeJvm<Not> {
    override fun initialize(node: Not, scope: Scope) {
        val `in` = scope.dataPath(node.`in`)
        val out = scope.dataPath(node.out)

        out.set { !`in`.getOfType<Boolean>() }
    }
}
