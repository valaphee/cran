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

package com.valaphee.flow

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.spec.Const
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import kotlinx.coroutines.CoroutineScope

/**
 * @author Kevin Ludwig
 */
@Node("Value")
class Value(
    @get:Const   @get:JsonProperty("value") val value: Any?    ,
    @get:Out("") @get:JsonProperty("out"  ) val out  : DataPath
) : LazyNode() {
    override fun run(scope: CoroutineScope) {
        out.set { value }
    }
}
