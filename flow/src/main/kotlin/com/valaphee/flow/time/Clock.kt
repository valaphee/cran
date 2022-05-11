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

package com.valaphee.flow.time

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.ControlPath
import com.valaphee.flow.DataPath
import com.valaphee.flow.EagerNode
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Kevin Ludwig
 */
@Node("Time/Clock")
class Clock(
    @get:In            @get:JsonProperty("in"       ) override val `in`    : ControlPath,
    @get:In ("Cancel") @get:JsonProperty("in_cancel")          val inCancel: ControlPath,
    @get:In ("Delay" ) @get:JsonProperty("in_delay" )          val inDelay : DataPath   ,
    @get:In ("Period") @get:JsonProperty("in_period")          val inPeriod: DataPath   ,
    @get:Out           @get:JsonProperty("out"      )          val out     : ControlPath
) : EagerNode() {
    @JsonIgnore private var running = AtomicBoolean()

    override fun initialize(scope: CoroutineScope) {
        `in`.collect(scope) {
            val inDelay = inDelay.get()
            val inPeriod = inPeriod.get()
            if (inDelay is Number && inPeriod is Number) {
                delay(inDelay.toLong())
                running.set(true)
                while (running.get()) {
                    out.emit()
                    delay(inPeriod.toLong())
                }
                running.set(false)
            } else error("$inDelay")
        }
        inCancel.collect(scope) { running.set(false) }
    }
}
