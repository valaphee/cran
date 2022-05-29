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

package com.valaphee.flow.radio.modulation

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Scope
import com.valaphee.flow.node.Arr
import com.valaphee.flow.node.Node
import com.valaphee.flow.radio.Deinterleave
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeType("Radio/Demodulation/AM")
class AmDemodulation(
    type: String,
    @get:In ("", Arr) @get:JsonProperty("in" ) val `in`: Int,
    @get:Out("", Arr) @get:JsonProperty("out") val out : Int
) : Node(type) {
    private val states = mutableMapOf<Scope, State>()

    override fun initialize(scope: Scope) {
        val state = states.getOrPut(scope) { State() }
        val `in` = scope.dataPath(`in`)
        val out = scope.dataPath(out)

        out.set {
            val (inRe, inIm) = Deinterleave.deinterleave(`in`.getOfType(), 2)
            val size = inRe.size
            val _out = FloatArray(size)

            state.prevMax *= 0.95f

            var avg = 0.0f
            repeat(size) {
                val _inRe = inRe[it]
                val _inIm = inIm[it]
                val __out = _inRe * _inRe + _inIm * _inIm
                _out[it] = __out
                if (__out > state.prevMax) state.prevMax = _out[it]
                avg += _out[it]
            }
            avg /= size

            val gain = 0.75f / state.prevMax
            repeat(size) { _out[it] = (_out[it] - avg) * gain }

            _out
        }
    }

    override fun shutdown(scope: Scope) {
        states.remove(scope)
    }

    private class State {
        var prevMax = 0.0f
    }
}
