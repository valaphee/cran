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
import com.valaphee.flow.node.Node
import com.valaphee.flow.spec.Const
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    @get:Const("Sample Rate", Int) @get:JsonProperty("sample_rate") val sampleRate: Int,
    @get:In   ("Begin"           ) @get:JsonProperty("in_begin"   ) val inBegin   : Int,
    @get:In   ("Abort"           ) @get:JsonProperty("in_abort"   ) val inAbort   : Int,
    @get:In   (""           , Arr) @get:JsonProperty("in"         ) val `in`      : Int
) : Node(type) {
    private val states = mutableMapOf<Scope, State>()

    override fun initialize(scope: Scope) {
        val state = states.getOrPut(scope) { State() }
        val inBegin = scope.controlPath(inBegin)
        val inAbort = scope.controlPath(inAbort)
        val `in` = scope.dataPath(`in`)

        inBegin.declare {
            if (state.deviceHandle == 0L) state.deviceHandle = ALC10.alcOpenDevice(ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER))
            if (state.context == 0L) state.context = ALC10.alcCreateContext(state.deviceHandle, IntArray(1))
            ALC10.alcMakeContextCurrent(state.context)
            AL.createCapabilities(ALC.createCapabilities(state.deviceHandle))
            if (state.source == 0) state.source = AL10.alGenSources()
            if (state.buffers == null) state.buffers = IntArray(8).also { AL10.alGenBuffers(it) }
            state.buffers!!.forEach {
                val data = `in`.getOfType<FloatArray>()
                AL10.alBufferData(it, AL10.AL_FORMAT_MONO16, ShortArray(data.size) { (Short.MAX_VALUE * data[it]).toInt().toShort() }, sampleRate)
                AL10.alSourceQueueBuffers(state.source, it)
            }
            AL10.alSourcePlay(state.source)

            coroutineScope {
                launch {
                    if (!state.running) {
                        state.running = true
                        while (state.running) {
                            repeat(AL10.alGetSourcei(state.source, AL10.AL_BUFFERS_PROCESSED)) {
                                val buffer = AL10.alSourceUnqueueBuffers(state.source)
                                val data = `in`.getOfType<FloatArray>()
                                AL10.alBufferData(buffer, AL10.AL_FORMAT_MONO16, ShortArray(data.size) { (Short.MAX_VALUE * data[it]).toInt().toShort() }, sampleRate)
                                AL10.alSourceQueueBuffers(state.source, buffer)
                            }
                            if (AL10.alGetSourcei(state.source, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING) AL10.alSourcePlay(state.source)
                            delay(10)
                        }
                        AL10.alDeleteSources(state.source)
                        AL10.alDeleteBuffers(state.buffers!!)
                        ALC10.alcDestroyContext(state.context)
                        ALC10.alcCloseDevice(state.deviceHandle)
                    }
                }
            }
        }
        inAbort.declare { state.running = false }
    }

    override fun shutdown(scope: Scope) {
        states.remove(scope)?.let { it.running = false }
    }

    private class State {
        var deviceHandle = 0L
        var context = 0L
        var source = 0
        var buffers: IntArray? = null
        var running = false
    }
}
