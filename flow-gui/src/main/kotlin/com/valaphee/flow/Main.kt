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

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import javafx.beans.property.SimpleListProperty
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import jfxtras.styles.jmetro.Style
import tornadofx.App
import tornadofx.View
import tornadofx.action
import tornadofx.hbox
import tornadofx.item
import tornadofx.launch
import tornadofx.listview
import tornadofx.menu
import tornadofx.menubar
import tornadofx.tab
import tornadofx.tabpane
import tornadofx.toObservable
import tornadofx.vbox
import tornadofx.vgrow
import kotlin.system.exitProcess

/**
 * @author Kevin Ludwig
 */
class Main : View("Flow") {
    private val graphsProperty = SimpleListProperty(mutableListOf<String>().toObservable())

    override val root = vbox {
        JMetro(this, Style.DARK)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        // Properties
        setPrefSize(1000.0, 800.0)

        // Children
        menubar {
            menu("File") { item("Exit") { action { close() } } }
            menu("Help") { item("About") { action { find<About>().openModal(resizable = false) } } }
        }
        hbox {
            // Properties
            vgrow = Priority.ALWAYS

            // Children
            listview(graphsProperty) {

            }
            tabpane {
                tab("Graph") {}
                tab("JSON") {}
            }
        }
    }
}

/**
 * @author Kevin Ludwig
 */
class MainApp : App(Image(MainApp::class.java.getResourceAsStream("/app.png")), Main::class)

fun main(arguments: Array<String>) {
    SvgImageLoaderFactory.install()

    launch<MainApp>(arguments)

    exitProcess(0)
}
