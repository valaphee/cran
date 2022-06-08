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

package com.valaphee.cran.node.math.vector

import com.valaphee.cran.graph.Scope
import com.valaphee.cran.node.NodeJvm
import com.valaphee.cran.spec.NodeDef
import jdk.incubator.vector.VectorOperators
import kotlin.math.sqrt

/**
 * @author Kevin Ludwig
 */
@NodeDef("jvm", Distance::class)
object DistanceJvm : NodeJvm<Distance> {
    override fun initialize(node: Distance, scope: Scope) {
        val inP = scope.dataPath(node.inP)
        val inQ = scope.dataPath(node.inQ)
        val out = scope.dataPath(node.out)

        out.set { vectorOp(inP.get(), inQ.get(), { p, q ->
            val pSubQ = p.sub(q)
            sqrt(pSubQ.mul(pSubQ).reduceLanes(VectorOperators.ADD).toFloat()).toInt()
        }, { p, q ->
            val pSubQ = p.sub(q)
            sqrt(pSubQ.mul(pSubQ).reduceLanes(VectorOperators.ADD))
        }, { p, q ->
            val pSubQ = p.sub(q)
            sqrt(pSubQ.mul(pSubQ).reduceLanes(VectorOperators.ADD))
        }, scope.objectMapper) }
    }
}