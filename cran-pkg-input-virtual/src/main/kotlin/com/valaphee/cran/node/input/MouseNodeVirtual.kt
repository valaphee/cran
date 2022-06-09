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

import com.valaphee.cran.node.NodeVirtual
import org.hid4java.HidDevice
import org.hid4java.HidManager
import java.util.BitSet
import kotlin.concurrent.thread

/**
 * @author Kevin Ludwig
 */
abstract class MouseNodeVirtual : NodeVirtual {
    protected companion object {
        private var hidDevice: HidDevice? = null
        private const val path = "\\\\?\\hid#variable_6&col02#1"

        val buttons = BitSet()

        init {
            Runtime.getRuntime().addShutdownHook(thread(false) { hidDevice?.close() })

            hidDevice = HidManager.getHidServices().attachedHidDevices.find { it.path.startsWith(path) }?.also { it.open() }
        }

        fun write(moveX: Byte = 0, moveY: Byte = 0) {
            hidDevice?.let {
                if (it.isOpen) {
                    val message = ByteArray(3)
                    message[0] = if (buttons.isEmpty) 0 else buttons.toByteArray()[0]
                    message[1] = moveX
                    message[2] = moveY
                    it.write(message, message.size, 0x02)
                }
            }
        }
    }
}
