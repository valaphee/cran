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

package com.valaphee.cran.virtual

import com.valaphee.cran.Virtual
import com.valaphee.cran.node.BinaryOperation
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.logic.Equal
import com.valaphee.cran.node.logic.GreaterThan
import com.valaphee.cran.node.logic.GreaterThanOrEqual
import com.valaphee.cran.node.logic.LessThan
import com.valaphee.cran.node.logic.LessThanOrEqual
import com.valaphee.cran.node.logic.NotEqual
import com.valaphee.cran.path.DataPathException
import com.valaphee.cran.spec.NodeImpl
import kotlinx.coroutines.CoroutineScope

/**
 * @author Kevin Ludwig
 */
@NodeImpl("virtual")
object Compare : Implementation {
    override fun initialize(coroutineScope: CoroutineScope, node: Node, virtual: Virtual) = when (node) {
        is BinaryOperation -> {
            val in1 = virtual.dataPath(node.in1)
            val in2 = virtual.dataPath(node.in2)
            val out = virtual.dataPath(node.out)

            when (node) {
                is LessThan -> {
                    out.set {
                        val a = in1.get()
                        val b = in2.get()
                        val result = CompareUtil.compare(a, b)
                        if (result != Int.MAX_VALUE) result < 0 else DataPathException("$a < $b")
                    }

                    true
                }
                is LessThanOrEqual -> {
                    out.set {
                        val a = in1.get()
                        val b = in2.get()
                        val result = CompareUtil.compare(a, b)
                        if (result != Int.MAX_VALUE) result <= 0 else DataPathException("$a ≤ $b")
                    }

                    true
                }
                is Equal -> {
                    out.set { in1.get() == in2.get() }

                    true
                }
                is NotEqual -> {
                    out.set { in1.get() != in2.get() }

                    true
                }
                is GreaterThanOrEqual -> {
                    out.set {
                        val a = in1.get()
                        val b = in2.get()
                        val result = CompareUtil.compare(a, b)
                        if (result != Int.MAX_VALUE) result >= 0 else DataPathException("$a ≥ $b")
                    }

                    true
                }
                is GreaterThan -> {
                    out.set {
                        val a = in1.get()
                        val b = in2.get()
                        val result = CompareUtil.compare(a, b)
                        if (result != Int.MAX_VALUE) result > 0 else throw DataPathException("$a > $b")
                    }

                    true
                }
                else -> false
            }
        }
        else -> false
    }
}
