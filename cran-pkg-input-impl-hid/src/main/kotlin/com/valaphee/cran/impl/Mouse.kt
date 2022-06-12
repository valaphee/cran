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

package com.valaphee.cran.impl

import com.valaphee.cran.Scope
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.input.MousePosition
import com.valaphee.cran.node.input.MoveMouse
import com.valaphee.cran.node.input.SetMouseButton
import com.valaphee.cran.spec.NodeImpl
import jdk.incubator.vector.IntVector
import org.hid4java.HidDevice
import org.hid4java.HidManager
import java.awt.MouseInfo
import java.util.BitSet
import kotlin.concurrent.thread

/**
 * @author Kevin Ludwig
 */
@NodeImpl
object Mouse : Implementation {
    private const val path = "\\\\?\\hid#variable_6&col02#1"
    private var hidDevice: HidDevice? = null

    private val buttons = BitSet()

    init {
        Runtime.getRuntime().addShutdownHook(thread(false) { hidDevice?.close() })

        hidDevice = HidManager.getHidServices().attachedHidDevices.find { it.path.startsWith(path) }?.also { it.open() }
    }

    override fun initialize(node: Node, scope: Scope) = when (node) {
        /*is OnMouseMove -> {
            true
        }*/
        is MoveMouse -> {
            val `in` = scope.controlPath(node.`in`)
            val inSensitivity = scope.dataPath(node.inSensitivity)
            val inMove = scope.dataPath(node.inMove)
            val out = scope.controlPath(node.out)

            val moves = mutableListOf<Int>().apply {
                var moveIndex = 0
                repeat(Byte.MAX_VALUE.toInt()) {
                    val move = (it * 0.25).toInt()
                    while (moveIndex <= move) {
                        this += move
                        moveIndex++
                    }
                }
            }.toIntArray()

            `in`.define {
                val move = inMove.getOfType<IntVector>()
                val moveAbs = move.abs().min(moves.size - 1)
                val moveX = moves[moveAbs.lane(0)]
                val moveY = moves[moveAbs.lane(1)]
                write((if (move.lane(0) >= 0) moveX else -moveX).toByte(), (if (move.lane(1) >= 0) moveY else -moveY).toByte())
                out()
            }

            true
        }
        /*is OnMouseButton -> {
            true
        }*/
        is SetMouseButton -> {
            val `in` = scope.controlPath(node.`in`)
            val inButton = scope.dataPath(node.inButton)
            val inState = scope.dataPath(node.inState)
            val out = scope.controlPath(node.out)

            `in`.define {
                val button = inButton.getOfType<Int>()
                if (inState.getOfType()) {
                    buttons.set(button)
                    write()
                } else {
                    buttons.clear(button)
                    write()
                }
                out()
            }

            true
        }
        is MousePosition -> {
            val out = scope.dataPath(node.out)

            out.set {
                val position = MouseInfo.getPointerInfo().location
                IntVector.fromArray(IntVector.SPECIES_64, intArrayOf(position.x, position.y), 0)
            }

            true
        }
        else -> false
    }

    private fun write(moveX: Byte = 0, moveY: Byte = 0) {
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
