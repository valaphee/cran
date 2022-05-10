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
import com.valaphee.flow.graph.SkinFactory
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.geometry.Side
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListCell
import javafx.scene.control.SplitPane
import javafx.scene.control.TabPane
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tornadofx.App
import tornadofx.View
import tornadofx.action
import tornadofx.bindSelected
import tornadofx.contextmenu
import tornadofx.customitem
import tornadofx.dynamicContent
import tornadofx.importStylesheet
import tornadofx.item
import tornadofx.launch
import tornadofx.listview
import tornadofx.menu
import tornadofx.menubar
import tornadofx.onChange
import tornadofx.pane
import tornadofx.scrollpane
import tornadofx.splitpane
import tornadofx.style
import tornadofx.tab
import tornadofx.tabpane
import tornadofx.textarea
import tornadofx.textfield
import tornadofx.toObservable
import tornadofx.toProperty
import tornadofx.vbox
import tornadofx.vgrow
import kotlin.system.exitProcess

/**
 * @author Kevin Ludwig
 */
class Main : View("Flow") {
    private val jsonProperty = "".toProperty()

    private val graphsProperty = SimpleListProperty(mutableListOf<Graph>().toObservable())
    private val graphProperty = SimpleObjectProperty<Graph>().apply {
        onChange {
            title = "http://localhost:8080/${(it?.meta?.name ?: it?.id)?.let { " - $it" } ?: ""}"
            jsonProperty.set(it?.let { ObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it) } ?: "")
        }
    }

    // setSkinFactories(SkinFactory(parent))
    override val root = vbox {
        // Properties
        setPrefSize(1000.0, 800.0)

        // Children
        menubar {
            menu("File") { item("Exit") { action { close() } } }
            menu("Help") { item("About") { action { find<About>().openModal(resizable = false) } } }
        }
        splitpane {
            // Parent Properties
            vgrow = Priority.ALWAYS
            setDividerPositions(0.25)

            // Children
            listview(graphsProperty) {
                // Parent Properties
                SplitPane.setResizableWithParent(this, false)

                // Value
                bindSelected(graphProperty)

                // Properties
                setCellFactory {
                    object : ListCell<Graph>() {
                        init {
                            setOnMouseClicked {
                                if (isEmpty) selectionModel.clearSelection()

                                it.consume()
                            }
                        }

                        override fun updateItem(item: Graph?, empty: Boolean) {
                            super.updateItem(item, empty)

                            text = if (empty || item == null) "" else item.meta?.name ?: item.id.toString()
                        }
                    }
                }

                fun contextMenu(selectedComponents: ObservableList<out Graph>) = ContextMenu().apply {
                    if (selectedComponents.isEmpty()) item("New") { action { graphsProperty.value += Graph() } }
                    else item("Remove") { action { MainScope.launch { delete(selectedComponents) } } }
                }

                contextMenu = contextMenu(selectionModel.selectedItems)
                selectionModel.selectedItems.onChange { contextMenu = contextMenu(it.list) }

                // Events
                setOnKeyPressed {
                    when (it.code) {
                        KeyCode.DELETE -> if (!selectionModel.isEmpty) {
                            MainScope.launch { delete(selectionModel.selectedItems) }
                            it.consume()
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
            tabpane {
                vgrow = Priority.ALWAYS

                // Properties
                side = Side.BOTTOM
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

                // Children
                tab("Graph") {
                    scrollpane {
                        pane { dynamicContent(graphProperty) { it?.flow?.setSkinFactories(SkinFactory(this)) } }
                        contextMenu = contextmenu {
                            val searchProperty = "".toProperty()
                            lateinit var search: TextField
                            customitem(hideOnClick = false) { search = textfield { textProperty().onChange { searchProperty.set(it) } } }
                            setOnKeyPressed { search.requestFocus() }

                            val items = items.toMutableList()
                            Spec.nodes.forEach {
                                item(it.name) {
                                    action {
                                        val local = sceneToLocal(x, y)
                                        graphProperty.get().newNode(it, Meta.Node(local.x, local.y))
                                    }
                                }
                            }
                            searchProperty.onChange { search ->
                                this.items.setAll(items)
                                (if (search!!.isEmpty()) Spec.nodes else Spec.nodes.filter { it.name.contains(search, true) }).forEach {
                                    item(it.name) {
                                        action {
                                            val local = sceneToLocal(x, y)
                                            graphProperty.get().newNode(it, Meta.Node(local.x, local.y))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                tab("JSON") {
                    // Children
                    textarea(jsonProperty) { style { font = Font.font("monospaced", 10.0) } }

                    // Events
                    setOnSelectionChanged { jsonProperty.set(ObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(graphProperty.get())) }
                }
            }
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
class MainApp : App(Image(MainApp::class.java.getResourceAsStream("/app.png")), Main::class, Style::class) {
    override fun init() {
        importStylesheet("/style.css")
    }
}

fun main(arguments: Array<String>) {
    SvgImageLoaderFactory.install()

    launch<MainApp>(arguments)

    exitProcess(0)
}
