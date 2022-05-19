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

import javafx.scene.Scene
import javafx.scene.image.Image
import tornadofx.App
import tornadofx.UIComponent
import tornadofx.importStylesheet

/**
 * @author Kevin Ludwig
 */
class FlowApp : App(Image(FlowApp::class.java.getResourceAsStream("/app.png")), FlowView::class, FlowStyle::class) {
    override fun init() {
        importStylesheet("/style.css")
    }

    override fun createPrimaryScene(view: UIComponent) = Scene(view.root, 1000.0, 800.0)
}
