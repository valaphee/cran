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

package com.valaphee.cran.radio.source

import com.fasterxml.jackson.annotation.JsonProperty
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import com.valaphee.cran.Scope
import com.valaphee.cran.node.Arr
import com.valaphee.cran.node.Int
import com.valaphee.cran.node.Task
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeType
import com.valaphee.cran.spec.Out
import kotlinx.coroutines.awaitCancellation
import java.nio.ByteBuffer

/**
 * @author Kevin Ludwig
 */
@NodeType("Radio/Source/RTL-SDR Source")
class RtlSdrSource(
    type: String,
    @get:In ("Begin"           ) @get:JsonProperty("in_begin"      ) override val inBegin     : Int,
    @get:In ("Abort"           ) @get:JsonProperty("in_abort"      ) override val inAbort     : Int,
    @get:In ("Buffer Size", Int) @get:JsonProperty("in_buffer_size")          val inBufferSize: Int,
    @get:In ("Sample Rate", Int) @get:JsonProperty("in_sample_rate")          val inSampleRate: Int,
    @get:In ("Frequency"  , Int) @get:JsonProperty("in_frequency"  )          val inFrequency : Int,
    @get:Out("Subgraph"        ) @get:JsonProperty("out_subgraph"  ) override val outSubgraph : Int,
    @get:Out(""           , Arr) @get:JsonProperty("out"           )          val out         : Int
) : Task(type) {
    override suspend fun onBegin(scope: Scope) {
        val inBufferSize = scope.dataPath(inBufferSize).getOfType<Int>()
        val inSampleRate = scope.dataPath(inSampleRate).getOfType<Int>()
        val inFrequency = scope.dataPath(inFrequency).getOfType<Int>()
        val out = scope.dataPath(out)

        var device: Pointer? = null

        try {
            val deviceByReference = PointerByReference()
            var result = LibRtlSdr.Instance.rtlsdr_open(deviceByReference, 0)
            if (result == 0) {
                device = deviceByReference.value

                result = LibRtlSdr.Instance.rtlsdr_set_sample_rate(device, inSampleRate)
                if (result != 0) println("rtlsdr_set_sample_rate returned non-zero. ($result)")
                result = LibRtlSdr.Instance.rtlsdr_set_center_freq(device, inFrequency)
                if (result != 0) println("rtlsdr_set_center_freq returned non-zero. ($result)")
                result = LibRtlSdr.Instance.rtlsdr_reset_buffer(device)
                if (result != 0) println("rtlsdr_reset_buffer returned non-zero. ($result)")
            } else println("rtlsdr_open returned non-zero. ($result)")

            val buffer = ByteBuffer.allocate(inBufferSize)
            val read = IntByReference()
            out.set {
                result = LibRtlSdr.Instance.rtlsdr_read_sync(device!!, buffer, buffer.capacity(), read)
                if (result == 0) FloatArray(read.value) { values[buffer[it].toInt() and 0xFF] } else {
                    println("rtlsdr_read_sync returned non-zero. ($result)")

                    null
                }
            }

            awaitCancellation()
        } finally {
            device?.let {
                val result = LibRtlSdr.Instance.rtlsdr_close(it)
                if (result != 0) println("rtlsdr_close returned non-zero. ($result)")
            }
        }
    }

    companion object {
        private val values = FloatArray(256) { (it - 127.4f) / 128.0f }
    }
}
