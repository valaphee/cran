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

package com.valaphee.cran.audio.source

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.cran.Scope
import com.valaphee.cran.node.Arr
import com.valaphee.cran.node.Int
import com.valaphee.cran.node.State
import com.valaphee.cran.node.Str
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeType
import com.valaphee.cran.spec.Out
import org.jcodec.codecs.wav.WavInput
import java.io.File
import java.nio.FloatBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * @author Kevin Ludwig
 */
@NodeType("Audio/Source/Wav")
class WavSource(
    type: String,
    @get:In ("Begin"              ) @get:JsonProperty("in_begin"          ) override val inBegin         : Int,
    @get:In ("Abort"              ) @get:JsonProperty("in_abort"          ) override val inAbort         : Int,
    @get:In ("File"          , Str) @get:JsonProperty("in_file"           )          val inFile          : Int,
    @get:In ("Buffer Size"   , Int) @get:JsonProperty("in_buffer_size"    )          val inBufferSize    : Int,
    @get:Out("Subgraph"           ) @get:JsonProperty("out_subgraph"      ) override val outSubgraph     : Int,
    @get:Out("Ready Subgraph"     ) @get:JsonProperty("out_ready_subgraph")          val outReadySubgraph: Int,
    @get:Out(""              , Arr) @get:JsonProperty("out"               )          val out             : Int
) : State(type) {
    override suspend fun onBegin(scope: Scope) {
        val inFile = scope.dataPath(inFile).getOfType<File>()
        val inBufferSize = scope.dataPath(inBufferSize).getOfType<Int>()
        val outReadySubgraph = scope.controlPath(outReadySubgraph)
        val out = scope.dataPath(out)

        WavInput.Source(WavInput.WavFile(inFile)).use {
            suspendCoroutine<Unit> { continuation ->
                out.set {
                    val _out = FloatBuffer.allocate(inBufferSize)
                    if (it.readFloat(_out) == -1) continuation.resume(Unit)
                    _out.array()
                }

                scope.invoke(continuation.context, outReadySubgraph)
            }
        }
    }
}
