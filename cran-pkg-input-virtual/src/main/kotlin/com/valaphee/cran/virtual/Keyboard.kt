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

package com.valaphee.cran.virtual

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import com.valaphee.cran.Virtual
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.input.Key
import com.valaphee.cran.node.input.SetKeyboardKey
import com.valaphee.cran.spec.NodeImpl
import kotlinx.coroutines.CoroutineScope
import org.hid4java.HidDevice
import org.hid4java.HidManager
import java.util.BitSet
import kotlin.concurrent.thread

/**
 * @author Kevin Ludwig
 */
@NodeImpl("virtual")
object Keyboard : Implementation {
    private const val path = "\\\\?\\hid#variable_6&col04#1"
    private var hidDevice: HidDevice? = null
    private val keys = mutableSetOf<Key>()

    @Volatile private var hookThreadId = 0
    /*private val onKey = MutableSharedFlow<Pair<Key, Boolean>>()*/

    init {
        Runtime.getRuntime().addShutdownHook(thread(false) {
            if (hookThreadId != 0) User32.INSTANCE.PostThreadMessage(hookThreadId, WinUser.WM_QUIT, WinDef.WPARAM(), WinDef.LPARAM())
            if (keys.isNotEmpty()) {
                keys.clear()
                write()
            }
            hidDevice?.close()
        })

        hidDevice = HidManager.getHidServices().attachedHidDevices.find { it.path.startsWith(path) }?.also { it.open() }

        /*thread {
            hookThreadId = Kernel32.INSTANCE.GetCurrentThreadId()
            val hMod = Kernel32.INSTANCE.GetModuleHandle(null)
            val hhkKeyboardLL = User32.INSTANCE.SetWindowsHookEx(User32.WH_KEYBOARD_LL, object : WinUser.LowLevelKeyboardProc {
                override fun callback(nCode: Int, wParam: WinDef.WPARAM, lParam: WinUser.KBDLLHOOKSTRUCT): WinDef.LRESULT {
                    if (nCode == 0) runCatching { Key.byVkCode(lParam.vkCode)?.let { onKey.tryEmit(it to (wParam.toInt() == User32.WM_KEYDOWN)) } }
                    return User32.INSTANCE.CallNextHookEx(null, nCode, wParam, WinDef.LPARAM(Pointer.nativeValue(lParam.pointer)))
                }
            }, hMod, 0)
            User32.INSTANCE.GetMessage(WinUser.MSG(), WinDef.HWND(Pointer.NULL), 0, 0)
            User32.INSTANCE.UnhookWindowsHookEx(hhkKeyboardLL)
            hookThreadId = 0
        }*/
    }

    override fun initialize(coroutineScope: CoroutineScope, node: Node, virtual: Virtual) = when(node) {
        /*is OnKeyboardKey -> {
            val outKey = virtual.dataPath(node.outKey)
            val outState = virtual.dataPath(node.outState)
            val out = virtual.controlPath(node.out)

            coroutineScope.launch {
                onKey.collectLatest { (key, state) ->
                    outKey.set(key)
                    outState.set(state)
                    out.invoke()
                }
            }

            true
        }*/
        is SetKeyboardKey -> {
            val `in` = virtual.controlPath(node.`in`)
            val inKey = virtual.dataPath(node.inKey)
            val inState = virtual.dataPath(node.inState)
            val out = virtual.controlPath(node.out)

            `in`.define {
                val key = inKey.getOfType<Key>()
                if (inState.getOfType() && !keys.contains(key) && keys.size <= 6) {
                    keys += key
                    write()
                } else if (keys.contains(key)) {
                    keys -= key
                    write()
                }
                out.invoke()
            }

            true
        }
        else -> false
    }

    private fun write() {
        hidDevice?.let {
            if (it.isOpen) {
                val message = ByteArray(2 + keys.size)
                val modifiers = BitSet().apply {
                    if (keys.contains(Key.LeftControl)) set(0)
                    if (keys.contains(Key.LeftShift)) set(1)
                    if (keys.contains(Key.LeftAlt)) set(2)
                    if (keys.contains(Key.LeftMeta)) set(3)
                    if (keys.contains(Key.RightControl)) set(4)
                    if (keys.contains(Key.RightShift)) set(5)
                    if (keys.contains(Key.RightAlt)) set(6)
                    if (keys.contains(Key.RightMeta)) set(7)
                }
                message[0] = if (modifiers.isEmpty) 0 else modifiers.toByteArray()[0]
                message[1] = 0x00
                keys.forEachIndexed { i, key -> message[2 + i] = key.scanCode.toByte() }
                it.write(message, message.size, 0x04)
            }
        }
    }
}
