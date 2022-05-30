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

package com.valaphee.cran.input

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.cran.Scope
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.math.vector.Vec2
import com.valaphee.cran.spec.NodeType
import com.valaphee.cran.spec.Out
import jdk.incubator.vector.IntVector
import java.awt.MouseInfo

/**
 * @author Kevin Ludwig
 */
@NodeType("Input/Mouse Position")
class MousePosition(
    type: String,
    @get:Out("", Vec2) @get:JsonProperty("out_position") val out: Int
) : Node(type) {
    override fun initialize(scope: Scope) {
        val out = scope.dataPath(out)

        out.set {
            val position = MouseInfo.getPointerInfo().location
            IntVector.fromArray(IntVector.SPECIES_64, intArrayOf(position.x, position.y), 0)
        }
    }
}
