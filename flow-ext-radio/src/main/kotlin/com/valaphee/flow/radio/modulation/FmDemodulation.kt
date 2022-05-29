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
import com.valaphee.flow.node.Int
import com.valaphee.flow.node.Node
import com.valaphee.flow.radio.Deinterleave
import com.valaphee.flow.spec.Const
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out
import kotlin.math.PI
import kotlin.math.atan2

/**
 * @author Kevin Ludwig
 */
@NodeType("Radio/Demodulation/FM")
class FmDemodulation(
    type: String,
    @get:Const("Deviation"  , Int) @get:JsonProperty("deviation"  ) val deviation : Int,
    @get:Const("Sample Rate", Int) @get:JsonProperty("sample_rate") val sampleRate: Int,
    @get:In   (""           , Arr) @get:JsonProperty("in"         ) val `in`      : Int,
    @get:Out  (""           , Arr) @get:JsonProperty("out"        ) val out       : Int
) : Node(type) {
    private val states = mutableMapOf<Scope, State>()

    override fun initialize(scope: Scope) {
        val state = states.getOrPut(scope) { State() }
        val `in` = scope.dataPath(`in`)
        val out = scope.dataPath(out)

        val gain = sampleRate / (2 * PI.toFloat() * deviation)
        out.set {
            val (inRe, inIm) = Deinterleave.deinterleave(`in`.getOfType(), 2)
            val size = inRe.size
            var prevRe = if (state.prevRe.isNaN()) inRe.first().also { state.prevRe = it } else state.prevRe
            var prevIm = if (state.prevIm.isNaN()) inIm.first().also { state.prevIm = it } else state.prevIm
            val outRe = FloatArray(size)
            val outIm = FloatArray(size)
            repeat(size) {
                val _inRe = inRe[it]
                val _inIm = inIm[it]
                val re = _inRe * prevRe + _inIm * prevIm
                val im = _inIm * prevRe - _inRe * prevIm
                outRe[it] = atan2(im, re) * gain
                outIm[it] = im
                prevRe = _inRe
                prevIm = _inIm
            }
            /*interleave(*/outRe/*, outIm)*/
        }
    }

    override fun shutdown(scope: Scope) {
        states.remove(scope)
    }

    private class State {
        var prevRe = Float.NaN
        var prevIm = Float.NaN
    }
}
