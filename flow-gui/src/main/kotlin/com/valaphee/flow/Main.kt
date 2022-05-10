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

import com.valaphee.flow.graph.Graph
import com.valaphee.flow.spec.Spec
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.JMetroStyleClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import tornadofx.App
import tornadofx.View
import tornadofx.bindSelected
import tornadofx.dynamicContent
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.launch
import tornadofx.listview
import tornadofx.pane
import tornadofx.scrollpane
import tornadofx.splitpane
import tornadofx.toObservable
import kotlin.system.exitProcess

/**
 * @author Kevin Ludwig
 */
class Main : View("Flow") {
    private val spec = runBlocking { HttpClient.get("http://localhost:8080/spec").body<Spec>() }

    private val graphsProperty = SimpleListProperty(mutableListOf<Graph>().toObservable())
    private val graphProperty = SimpleObjectProperty<Graph>()

    override val root = hbox {
        JMetro(this, jfxtras.styles.jmetro.Style.DARK)
        styleClass.add(JMetroStyleClass.BACKGROUND)

        // Properties
        setPrefSize(1000.0, 800.0)

        // Children
        splitpane {
            // Parent Properties
            hgrow = Priority.ALWAYS

            listview(graphsProperty) {
                // Value
                bindSelected(graphProperty)

                // Events
                setOnKeyPressed {
                    when (it.code) {
                        KeyCode.DELETE -> {
                            if (!selectionModel.isEmpty) {
                                MainScope.launch { delete(selectionModel.selectedItems) }
                                it.consume()
                            }
                        }
                        KeyCode.F5 -> {
                            MainScope.launch { this@Main.refresh() }
                            it.consume()
                        }
                        else -> Unit
                    }
                }

                // Initialization
                MainScope.launch { this@Main.refresh() }
            }
            scrollpane { pane { dynamicContent(graphProperty) { it?.flow(this, spec) } } }
        }
    }

    private suspend fun refresh() {
        HttpClient.get("http://localhost:8080/graph/").body<List<Graph>>().also { withContext(Dispatchers.Main) { graphsProperty.setAll(it) } }
    }

    private suspend fun delete(graphs: List<Graph>) {
        graphs.map { coroutineScope { launch { HttpClient.delete("http://localhost:8080/graph/${it.id}") } } }.joinAll()
    }
}

/**
 * @author Kevin Ludwig
 */
class MainApp : App(Image(MainApp::class.java.getResourceAsStream("/app.png")), Main::class, Style::class)

fun main(arguments: Array<String>) {
    SvgImageLoaderFactory.install()

    launch<MainApp>(arguments)

    exitProcess(0)
}
