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

package com.valaphee.flow.radio

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Scope
import com.valaphee.flow.node.Arr
import com.valaphee.flow.node.Node
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeType("Radio/Deinterleave")
class Deinterleave(
    type: String,
    @get:In (""  , Arr) @get:JsonProperty("in"    ) val `in` : Int,
    @get:Out("Re", Arr) @get:JsonProperty("out_re") val outRe: Int,
    @get:Out("Im", Arr) @get:JsonProperty("out_im") val outIm: Int,
) : Node(type) {
    private val states = mutableMapOf<Scope, State>()

    override fun initialize(scope: Scope) {
        val state = states.getOrPut(scope) { State() }
        val `in` = scope.dataPath(`in`)
        val outRe = scope.dataPath(outRe)
        val outIm = scope.dataPath(outIm)

        outRe.set {
            state.re?.also { state.re = null } ?: run {
                val (inRe, inIm) = deinterleave(`in`.getOfType(), 2)
                state.re = inRe
                state.im = inIm
                inRe
            }
        }
        outIm.set {
            state.im?.also { state.im = null } ?: run {
                val (inRe, inIm) = deinterleave(`in`.getOfType(), 2)
                state.re = inRe
                state.im = inIm
                inIm
            }
        }
    }

    override fun shutdown(scope: Scope) {
        states.remove(scope)
    }

    private class State {
        var re: FloatArray? = null
        var im: FloatArray? = null
    }

    companion object {
        fun deinterleave(value: FloatArray, count: Int): List<FloatArray> {
            check(value.size % count == 0)

            val size = value.size / count
            return List(count) { n -> FloatArray(size) { i -> value[n + i * count] } }
        }
    }
}
