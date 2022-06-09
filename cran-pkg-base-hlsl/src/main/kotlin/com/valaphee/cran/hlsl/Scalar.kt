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

package com.valaphee.cran.hlsl

import com.valaphee.cran.Hlsl
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.math.scalar.Add
import com.valaphee.cran.node.math.scalar.Divide
import com.valaphee.cran.node.math.scalar.Multiply
import com.valaphee.cran.node.math.scalar.Subtract
import com.valaphee.cran.spec.NodeImpl

/**
 * @author Kevin Ludwig
 */
@NodeImpl("hlsl")
object Scalar : Implementation {
    override fun initialize(node: Node, hlsl: Hlsl) = when (node) {
        is Add -> {
            hlsl.defineVariable(node.out, "float", "${hlsl.declare(node.in1)} + ${hlsl.declare(node.in2)}")

            true
        }
        is Subtract -> {
            hlsl.defineVariable(node.out, "float", "${hlsl.declare(node.in1)} - ${hlsl.declare(node.in2)}")

            true
        }
        is Multiply -> {
            hlsl.defineVariable(node.out, "float", "${hlsl.declare(node.in1)} * ${hlsl.declare(node.in2)}")

            true
        }
        is Divide -> {
            hlsl.defineVariable(node.out, "float", "${hlsl.declare(node.in1)} / ${hlsl.declare(node.in2)}")

            true
        }
        else -> false
    }
}
