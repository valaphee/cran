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

package com.valaphee.cran.node.input

import com.valaphee.cran.Virtual
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.NodeVirtual
import com.valaphee.cran.spec.NodeImpl
import jdk.incubator.vector.IntVector
import java.awt.MouseInfo

/**
 * @author Kevin Ludwig
 */
@NodeImpl("virtual")
object MousePositionVirtual : NodeVirtual {
    override fun initialize(node: Node, virtual: Virtual) = if (node is MousePosition) {
        val out = virtual.dataPath(node.out)

        out.set {
            val position = MouseInfo.getPointerInfo().location
            IntVector.fromArray(IntVector.SPECIES_64, intArrayOf(position.x, position.y), 0)
        }

        true
    } else false
}
