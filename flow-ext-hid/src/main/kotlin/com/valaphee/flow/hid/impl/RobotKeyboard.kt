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

package com.valaphee.flow.hid.impl

import com.valaphee.flow.hid.Key
import com.valaphee.flow.hid.Keyboard
import java.awt.Robot

/**
 * @author Kevin Ludwig
 */
class RobotKeyboard : Keyboard() {
    override fun keyPress(key: Key): Boolean {
        robot.keyPress(key.vkCode)
        return true
    }

    override fun keyRelease(key: Key): Boolean {
        robot.keyRelease(key.vkCode)
        return true
    }

    companion object {
        private val robot = Robot()
    }
}
