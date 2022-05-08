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

package com.valaphee.flow.loop

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.ControlPath
import com.valaphee.flow.DataPath
import com.valaphee.flow.EagerNode
import kotlinx.coroutines.CoroutineScope

/**
 * @author Kevin Ludwig
 */
class ForEach(
    override val `in`: ControlPath,
    @get:JsonProperty("in_value") val inValue: DataPath,
    @get:JsonProperty("out_body") val outBody: ControlPath,
    @get:JsonProperty("out") val out: ControlPath,
) : EagerNode() {
    override fun run(scope: CoroutineScope) {
        `in`.collect(scope) {
            (inValue.get() as List<*>).forEach { _ -> outBody.emit(/*it*/) }
            out.emit()
        }
    }
}
