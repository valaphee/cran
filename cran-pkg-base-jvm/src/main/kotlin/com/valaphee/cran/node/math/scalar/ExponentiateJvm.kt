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

package com.valaphee.cran.node.math.scalar

import com.google.common.math.IntMath
import com.valaphee.cran.graph.Scope
import com.valaphee.cran.node.NodeJvm
import com.valaphee.cran.path.DataPathException
import com.valaphee.cran.spec.NodeDef
import kotlin.math.pow

/**
 * @author Kevin Ludwig
 */
@NodeDef("jvm", Exponentiate::class)
object ExponentiateJvm : NodeJvm<Exponentiate> {
    override fun initialize(node: Exponentiate, scope: Scope) {
        val inX = scope.dataPath(node.inX)
        val inN = scope.dataPath(node.inN)
        val out = scope.dataPath(node.out)

        out.set {
            val _inX = inX.get()
            val _inN = inN.get()
            when (_inX) {
                is Float -> when (_inN) {
                    is Float  -> _inX           .pow(_inN        )
                    is Double -> _inX.toDouble().pow(_inN        )
                    is Number -> _inX           .pow(_inN.toInt())
                    else      -> throw DataPathException("$_inX$_inN")
                }
                is Double -> when (_inN) {
                    is Float  -> _inX.pow(_inN.toDouble())
                    is Double -> _inX.pow(_inN           )
                    is Number -> _inX.pow(_inN.toInt()   )
                    else      -> throw DataPathException("$_inX$_inN")
                }
                is Number -> when (_inN) {
                    is Float  -> _inX.toFloat() .pow(_inN)
                    is Double -> _inX.toDouble().pow(_inN)
                    is Number -> IntMath.pow(_inX.toInt(), _inN.toInt())
                    else      -> throw DataPathException("$_inX$_inN")
                }
                else -> throw DataPathException("$_inX$_inN")
            }
        }
    }
}