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

import com.valaphee.cran.graph.Scope
import com.valaphee.cran.node.NodeJvm
import com.valaphee.cran.path.DataPathException
import com.valaphee.cran.spec.NodeDef
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.absoluteValue

/**
 * @author Kevin Ludwig
 */
@NodeDef("jvm", Absolute::class)
object AbsoluteJvm : NodeJvm<Absolute> {
    override fun initialize(node: Absolute, scope: Scope) {
        val `in` = scope.dataPath(node.`in`)
        val out = scope.dataPath(node.out)

        out.set {
            val _in = `in`.get()
            when (_in) {
                is Byte       -> _in.toInt().absoluteValue.toByte()
                is Short      -> _in.toInt().absoluteValue.toShort()
                is Int        -> _in        .absoluteValue
                is Long       -> _in        .absoluteValue
                is BigInteger -> _in        .abs()
                is Float      -> _in        .absoluteValue
                is Double     -> _in        .absoluteValue
                is BigDecimal -> _in        .abs()
                else          -> throw DataPathException("|$_in|")
            }
        }
    }
}
