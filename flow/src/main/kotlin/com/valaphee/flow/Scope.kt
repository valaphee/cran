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

import com.fasterxml.jackson.databind.ObjectMapper
import com.valaphee.flow.path.ControlPath
import com.valaphee.flow.path.DataPath

/**
 * @author Kevin Ludwig
 */
class Scope(
    val objectMapper: ObjectMapper,
    val graphManager: GraphManager
) {
    private val controlPaths = mutableSetOf<ControlPath>()
    private val dataPaths = mutableSetOf<DataPath>()

    fun subScope() = Scope(objectMapper, graphManager)

    fun controlPath(controlPathId: Int) = controlPaths.find { it.id == controlPathId } ?: ControlPath(controlPathId).also { controlPaths += it }

    fun dataPath(dataPathId: Int) = dataPaths.find { it.id == dataPathId } ?: DataPath(dataPathId, objectMapper).also { dataPaths += it }
}
