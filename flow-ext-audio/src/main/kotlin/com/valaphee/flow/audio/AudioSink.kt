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
import com.valaphee.flow.node.Task
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
    @get:In ("Begin"            ) @get:JsonProperty("in_begin"      ) override val inBegin      : Int,
    @get:In ("Abort"            ) @get:JsonProperty("in_abort"      ) override val inAbort      : Int,
    @get:In ("Buffer Count", Int) @get:JsonProperty("in_buffer_size")          val inBufferCount: Int,
    @get:In ("Sample Rate" , Int) @get:JsonProperty("in_sample_rate")          val inSampleRate : Int,
    @get:In (""            , Arr) @get:JsonProperty("in"            )          val `in`         : Int,
    @get:Out("Subgraph"         ) @get:JsonProperty("out_subgraph"  ) override val outSubgraph  : Int
) : Task(type) {
    override suspend fun onBegin(scope: Scope) {
        val `in` = scope.dataPath(`in`)
        val inBufferCount = scope.dataPath(inBufferCount).getOfType<Int>()
        val inSampleRate = scope.dataPath(inSampleRate).getOfType<Int>()

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
            buffers = IntArray(inBufferCount).also { AL10.alGenBuffers(it) }
            buffers.forEach { buffer ->
                `in`.getOfTypeOrNull<FloatArray>()?.let { _in ->
                    AL10.alBufferData(buffer, AL10.AL_FORMAT_MONO16, ShortArray(_in.size) { (Short.MAX_VALUE * _in[it]).toInt().toShort() }, inSampleRate)
                    AL10.alSourceQueueBuffers(source, buffer)
                }
            }
            if (AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED) != 0) AL10.alSourcePlay(source)

            while (true) {
                repeat(AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED)) {
                    `in`.getOfTypeOrNull<FloatArray>()?.let { _in ->
                        val buffer = AL10.alSourceUnqueueBuffers(source)
                        AL10.alBufferData(buffer, AL10.AL_FORMAT_MONO16, ShortArray(_in.size) { (Short.MAX_VALUE * _in[it]).toInt().toShort() }, inSampleRate)
                        AL10.alSourceQueueBuffers(source, buffer)
                    }
                }
                if (AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED) != 0 && AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) AL10.alSourcePlay(source)

                /*yield()*/delay(10) // TODO
            }
        } finally {
            if (source != 0) AL10.alDeleteSources(source)
            buffers?.let { AL10.alDeleteBuffers(it) }
            if (context != 0L) ALC10.alcDestroyContext(context)
            if (deviceHandle != 0L) ALC10.alcCloseDevice(deviceHandle)
        }
    }
}
