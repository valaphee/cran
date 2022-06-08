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
@NodeDef("jvm", NotEqual::class)
object NotEqualJvm : NodeJvm<NotEqual> {
    override fun initialize(node: NotEqual, scope: Scope) {
        val inA = scope.dataPath(node.inA)
        val inB = scope.dataPath(node.inB)
        val out = scope.dataPath(node.out)

        out.set { inA.get() != inB.get() }
    }
}