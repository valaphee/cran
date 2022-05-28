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

package com.valaphee.flow.input

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Scope
import com.valaphee.flow.node.Num
import com.valaphee.flow.node.math.vector.Vec2
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out
import jdk.incubator.vector.IntVector

/**
 * @author Kevin Ludwig
 */
@NodeType("Input/Move Mouse")
class MoveMouse(
    type: String,
    @get:In (""                 ) @get:JsonProperty("in"            ) val `in`         : Int,
    @get:In ("Sensitivity", Num ) @get:JsonProperty("in_sensitivity") val inSensitivity: Int,
    @get:In ("Move"       , Vec2) @get:JsonProperty("in_move"       ) val inMove       : Int,
    @get:Out(""                 ) @get:JsonProperty("out"           ) val out          : Int
) : Mouse(type) {
    private val moves: IntArray

    init {
        val moves = mutableListOf<Int>()
        var moveIndex = 0
        repeat(Byte.MAX_VALUE.toInt()) {
            val move = (it * 0.25).toInt()
            while (moveIndex <= move) {
                moves += move
                moveIndex++
            }
        }
        this.moves = moves.toIntArray()
    }

    override fun initialize(scope: Scope) {
        val `in` = scope.controlPath(`in`)
        val inSensitivity = scope.dataPath(inSensitivity)
        val inMove = scope.dataPath(inMove)
        val out = scope.controlPath(out)

        `in`.declare {
            val move = inMove.getOfType<IntVector>()
            val moveAbs = move.abs().min(moves.size - 1)
            val moveX = moves[moveAbs.lane(0)]
            val moveY = moves[moveAbs.lane(1)]
            write((if (move.lane(0) >= 0) moveX else -moveX).toByte(), (if (move.lane(1) >= 0) moveY else -moveY).toByte())
            out()
        }
    }
}
