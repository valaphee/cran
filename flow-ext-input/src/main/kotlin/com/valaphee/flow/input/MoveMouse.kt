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
import com.valaphee.flow.ControlPath
import com.valaphee.flow.DataPath
import com.valaphee.flow.Num
import com.valaphee.flow.math.vector2.Vec2
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import com.valaphee.foundry.math.Int2
import kotlin.math.abs

/**
 * @author Kevin Ludwig
 */
@Node("Input/Move Mouse")
class MoveMouse(
    @get:In (""                 ) @get:JsonProperty("in"            ) override val `in`         : ControlPath,
    @get:In ("Sensitivity", Num ) @get:JsonProperty("in_sensitivity")          val inSensitivity: DataPath   ,
    @get:In ("Move"       , Vec2) @get:JsonProperty("in_move"       )          val inMove       : DataPath   ,
    @get:Out(""                 ) @get:JsonProperty("out"           )          val out          : ControlPath
) : Mouse() {
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

    override fun initialize() {
        `in`.declare {
            val move = inMove.getOrThrow<Int2>("in_move")
            val moveX = abs(move.x)
            val moveY = abs(move.y)
            val _moveX = if (moveX >= moves.size) moves.size - 1 else moves[moveX]
            val _moveY = if (moveY >= moves.size) moves.size - 1 else moves[moveY]
            write(Int2(if (move.x > 0) _moveX else -_moveX, if (move.y > 0) _moveY else -_moveY))
            out()
        }
    }
}
