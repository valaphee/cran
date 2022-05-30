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

package com.valaphee.flow.audio

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Scope
import com.valaphee.flow.node.Arr
import com.valaphee.flow.node.Int
import com.valaphee.flow.node.State
import com.valaphee.flow.spec.Const
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out
import kotlinx.coroutines.delay
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10

/**
 * @author Kevin Ludwig
 */
@NodeType("Audio/Sink")
class AudioSink(
    type: String,
    @get:Const("Sample Rate", Int) @get:JsonProperty("sample_rate" )          val sampleRate : Int,
    @get:In   ("Begin"           ) @get:JsonProperty("in_begin"    ) override val inBegin    : Int,
    @get:In   ("Abort"           ) @get:JsonProperty("in_abort"    ) override val inAbort    : Int,
    @get:In   (""           , Arr) @get:JsonProperty("in"          )          val `in`       : Int,
    @get:Out  ("Subgraph"        ) @get:JsonProperty("out_subgraph") override val outSubgraph: Int
) : State(type) {
    private val states = mutableMapOf<Scope, State>()

    override suspend fun onBegin(scope: Scope) {
        val `in` = scope.dataPath(`in`)

        var deviceHandle = 0L
        var context = 0L
        var source = 0
        var buffers: IntArray? = null

        try {
            deviceHandle = ALC10.alcOpenDevice(ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER))
            context = ALC10.alcCreateContext(deviceHandle, IntArray(1))
            ALC10.alcMakeContextCurrent(context)
            AL.createCapabilities(ALC.createCapabilities(deviceHandle))
            source = AL10.alGenSources()
            buffers = IntArray(8).also { AL10.alGenBuffers(it) }
            buffers.forEach {
                val data = `in`.getOfTypeOrNull() ?: floatArrayOf()
                AL10.alBufferData(it, AL10.AL_FORMAT_MONO16, ShortArray(data.size) { (Short.MAX_VALUE * data[it]).toInt().toShort() }, sampleRate)
                AL10.alSourceQueueBuffers(source, it)
            }
            AL10.alSourcePlay(source)

            while (true) {
                repeat(AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED)) {
                    val buffer = AL10.alSourceUnqueueBuffers(source)
                    val _in = `in`.getOfTypeOrNull() ?: floatArrayOf()
                    AL10.alBufferData(buffer, AL10.AL_FORMAT_MONO16, ShortArray(_in.size) { (Short.MAX_VALUE * _in[it]).toInt().toShort() }, sampleRate)
                    AL10.alSourceQueueBuffers(source, buffer)
                }
                if (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) AL10.alSourcePlay(source)

                delay(10)
            }
        } finally {
            if (source != 0) AL10.alDeleteSources(source)
            buffers?.let { AL10.alDeleteBuffers(it) }
            if (context != 0L) ALC10.alcDestroyContext(context)
            if (deviceHandle != 0L) ALC10.alcCloseDevice(deviceHandle)
        }
    }
}
