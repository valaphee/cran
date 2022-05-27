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

package com.valaphee.flow.input

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Scope
import com.valaphee.flow.node.Node
import com.valaphee.flow.node.math.vector2.Vec2
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out
import com.valaphee.foundry.math.Int2
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
            Int2(position.x, position.y)
        }
    }
}
