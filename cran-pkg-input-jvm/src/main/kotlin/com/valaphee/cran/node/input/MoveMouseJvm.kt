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

package com.valaphee.cran.node.input

import com.valaphee.cran.graph.jvm.Scope
import com.valaphee.cran.node.Node
import com.valaphee.cran.spec.NodeProc
import jdk.incubator.vector.IntVector

/**
 * @author Kevin Ludwig
 */
@NodeProc("jvm")
object MoveMouseJvm : MouseNodeJvm() {
    override fun process(node: Node, scope: Scope) = if (node is MoveMouse) {
        val `in` = scope.controlPath(node.`in`)
        val inSensitivity = scope.dataPath(node.inSensitivity)
        val inMove = scope.dataPath(node.inMove)
        val out = scope.controlPath(node.out)

        val moves = mutableListOf<Int>().apply {
            var moveIndex = 0
            repeat(Byte.MAX_VALUE.toInt()) {
                val move = (it * 0.25).toInt()
                while (moveIndex <= move) {
                    this += move
                    moveIndex++
                }
            }
        }.toIntArray()

        `in`.declare {
            val move = inMove.getOfType<IntVector>()
            val moveAbs = move.abs().min(moves.size - 1)
            val moveX = moves[moveAbs.lane(0)]
            val moveY = moves[moveAbs.lane(1)]
            write((if (move.lane(0) >= 0) moveX else -moveX).toByte(), (if (move.lane(1) >= 0) moveY else -moveY).toByte())
            out()
        }

        true
    } else false
}
