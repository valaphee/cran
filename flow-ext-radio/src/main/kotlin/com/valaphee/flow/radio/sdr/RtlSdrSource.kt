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

package com.valaphee.flow.radio.sdr

import com.fasterxml.jackson.annotation.JsonProperty
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import com.valaphee.flow.Scope
import com.valaphee.flow.node.Arr
import com.valaphee.flow.node.Int
import com.valaphee.flow.node.Node
import com.valaphee.flow.spec.Const
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out
import java.nio.ByteBuffer

/**
 * @author Kevin Ludwig
 */
@NodeType("Radio/SDR/RTL-SDR Source")
class RtlSdrSource(
    type: String,
    @get:Const("Sample Rate", Int) @get:JsonProperty("sample_rate") val sampleRate: Int,
    @get:Const("Frequency"  , Int) @get:JsonProperty("frequency"  ) val frequency : Int,
    @get:In   ("Begin"           ) @get:JsonProperty("in_begin"   ) val inBegin   : Int,
    @get:In   ("Abort"           ) @get:JsonProperty("in_abort"   ) val inAbort   : Int,
    @get:Out  (""           , Arr) @get:JsonProperty("out"        ) val out       : Int
) : Node(type) {
    private val states = mutableMapOf<Scope, State>()

    override fun initialize(scope: Scope) {
        val state = states.getOrPut(scope) { State() }
        val inBegin = scope.controlPath(inBegin)
        val inAbort = scope.controlPath(inAbort)
        val out = scope.dataPath(out)

        inBegin.declare {
            if (state.device == null) {
                val deviceByReference = PointerByReference()
                var result = LibRtlSdr.Instance.rtlsdr_open(deviceByReference, 0)
                if (result == 0) {
                    state.device = deviceByReference.value

                    result = LibRtlSdr.Instance.rtlsdr_set_sample_rate(state.device!!, sampleRate)
                    if (result != 0) println("rtlsdr_set_sample_rate returned non-zero. ($result)")
                    result = LibRtlSdr.Instance.rtlsdr_set_center_freq(state.device!!, frequency)
                    if (result != 0) println("rtlsdr_set_center_freq returned non-zero. ($result)")
                    result = LibRtlSdr.Instance.rtlsdr_reset_buffer(state.device!!)
                    if (result != 0) println("rtlsdr_reset_buffer returned non-zero. ($result)")
                } else println("rtlsdr_open returned non-zero. ($result)")
            }
        }
        inAbort.declare {
            state.device?.let {
                state.device = null

                val result = LibRtlSdr.Instance.rtlsdr_close(it)
                if (result != 0) println("rtlsdr_close returned non-zero. ($result)")
            }
        }

        val buffer = ByteBuffer.allocate(16 * 16384)
        val read = IntByReference()
        out.set {
            state.device?.let {
                val result = LibRtlSdr.Instance.rtlsdr_read_sync(it, buffer, buffer.capacity(), read)
                if (result == 0) FloatArray(read.value) { values[buffer[it].toInt() and 0xFF] } else {
                    println("rtlsdr_read_sync returned non-zero. ($result)")
                    noData
                }
            } ?: noData
        }
    }

    override fun shutdown(scope: Scope) {
        states.remove(scope)?.let { state ->
            state.device?.let {
                state.device = null

                val result = LibRtlSdr.Instance.rtlsdr_close(it)
                if (result != 0) println("rtlsdr_close returned non-zero. ($result)")
            }
        }
    }

    private class State {
        var device: Pointer? = null
    }

    companion object {
        private val values = FloatArray(256) { (it - 127.4f) / 128.0f }
        private val noData = floatArrayOf()
    }
}
