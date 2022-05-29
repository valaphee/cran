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

package com.valaphee.flow.radio.filter

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Scope
import com.valaphee.flow.node.Arr
import com.valaphee.flow.node.Int
import com.valaphee.flow.node.Node
import com.valaphee.flow.spec.Const
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeType("Radio/Filter/Half-band low-pass filter")
class HalfBandLowPassFilter(
    type: String,
    @get:Const("Order", Int) @get:JsonProperty("order") val order: Int,
    @get:In   (""     , Arr) @get:JsonProperty("in"   ) val `in` : Int,
    @get:Out  (""     , Arr) @get:JsonProperty("out"  ) val out  : Int
) : Node(type) {
    private val states = mutableMapOf<Scope, State>()

    override fun initialize(scope: Scope) {
        val taps = checkNotNull(tapsByOrder[order])
        val state = states.getOrPut(scope) { State(order) }
        val `in` = scope.dataPath(`in`)
        val out = scope.dataPath(out)

        out.set {
            val _in = `in`.getOfType<FloatArray>()
            val _out = FloatArray(_in.size / 2)

            repeat(_out.size) {
                state.delays         [state.delayIndex         ] = _in[it * 2]
                state.delaysMiddleTap[state.delayMiddleTapIndex] = _in[it * 2 + 1]
                if (++state.delayMiddleTapIndex > state.delaysMiddleTap.size) state.delayMiddleTapIndex = 0

                var i = state.delayIndex
                var j = state.delayIndex + state.delays.size - 1
                taps.forEach { tap ->
                    _out[it] += (state.delays[i] + state.delays[j]) * tap
                    if (--i > state.delays.size) i = 0
                    if (--j < 0) j = 0
                }
                _out[it] += state.delaysMiddleTap[state.delayMiddleTapIndex]
            }

            _in
        }
    }

    override fun shutdown(scope: Scope) {
        states.remove(scope)
    }

    private class State(
        order: Int
    ) {
        val delays = FloatArray(order / 2)
        val delaysMiddleTap = FloatArray(order / 4)
        var delayIndex = 0
        var delayMiddleTapIndex = 0
    }

    companion object {
        private val tapsByOrder = mutableMapOf(
            8  to floatArrayOf(-0.045567308121f,  0.550847429795f),
            12 to floatArrayOf( 0.018032677037f, -0.114591559026f,  0.597385968973f),
            32 to floatArrayOf(-0.020465752391f,  0.021334704213f, -0.032646869627f, 0.048752407464f, -0.072961784639f, 0.113978914053f, -0.203982998267f, 0.633841612044f)
        )
    }
}
