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
import com.valaphee.cran.spec.NodeImpl

/**
 * @author Kevin Ludwig
 */
@NodeImpl("virtual")
object SetKeyboardKeyVirtual : KeyboardNodeVirtual() {
    override fun initialize(node: Node, virtual: Virtual) = if (node is SetKeyboardKey) {
        val `in` = virtual.controlPath(node.`in`)
        val inKey = virtual.dataPath(node.inKey)
        val inState = virtual.dataPath(node.inState)
        val out = virtual.controlPath(node.out)

        `in`.define {
            val key = inKey.getOfType<Key>()
            if (inState.getOfType()) {
                if (!keys.contains(key) && keys.size <= 6) {
                    keys += key
                    write()
                }
            } else if (keys.contains(key)) {
                keys -= key
                write()
            }
            out.invoke()
        }

        true
    } else false
}
