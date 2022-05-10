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

import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.multi
import tornadofx.px

/**
 * @author Kevin Ludwig
 */
class Style : Stylesheet() {
    init {
        nodeWindow {
            backgroundColor = multi(LinearGradient(0.0, 0.0, 0.0, 1.0, true, CycleMethod.NO_CYCLE, Stop(0.0, Color.rgb(13, 13, 13, 0.95)), Stop(1.0, Color.rgb(27, 27, 27, 0.95))))
            borderColor = multi(box(Color.rgb(12, 12, 12)))
            borderWidth = multi(box(2.0.px, 2.0.px, 2.0.px, 2.0.px))
        }
        nodeWindowTitle {
            backgroundColor = multi(RadialGradient(0.0, 1.0, 0.0, 0.0, 1.0, true, CycleMethod.NO_CYCLE, Stop(0.0, Color.rgb(50, 50, 50, 1.0)), Stop(1.0, Color.rgb(50, 50, 50, 0.1))))
        }
        nodePath {
            stroke = Color.rgb(255, 255, 255)
            strokeWidth = 3.0.px
        }
        nodeNewPath {
            stroke = Color.rgb(255, 255, 255)
            strokeWidth = 3.0.px
        }
    }

    companion object {
        val nodeWindow by cssclass("window")
        val nodeWindowTitle by cssclass("window-titlebar")
        val nodePath by cssclass("vnode-connection")
        val nodeNewPath by cssclass("vnode-new-connection")
    }
}
