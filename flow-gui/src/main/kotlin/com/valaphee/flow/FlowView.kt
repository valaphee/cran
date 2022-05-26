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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Injector
import com.google.protobuf.ByteString
import com.valaphee.flow.graph.ConnectorValue
import com.valaphee.flow.graph.Graph
import com.valaphee.flow.graph.SkinFactory
import com.valaphee.flow.graph.asNodeStyleClass
import com.valaphee.flow.meta.Meta
import com.valaphee.flow.settings.SettingsView
import com.valaphee.flow.spec.Spec
import com.valaphee.svc.graph.v1.DeleteGraphRequest
import com.valaphee.svc.graph.v1.GetSpecRequest
import com.valaphee.svc.graph.v1.GraphServiceGrpc
import com.valaphee.svc.graph.v1.GraphServiceGrpc.GraphServiceBlockingStub
import com.valaphee.svc.graph.v1.ListGraphRequest
import com.valaphee.svc.graph.v1.UpdateGraphRequest
import eu.mihosoft.vrl.workflow.VNode
import eu.mihosoft.vrl.workflow.incubating.LayoutGeneratorSmart
import io.grpc.ManagedChannelBuilder
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.geometry.Side
import javafx.scene.Parent
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.util.StringConverter
import jfxtras.labs.util.event.MouseControlUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tornadofx.FileChooserMode
import tornadofx.View
import tornadofx.action
import tornadofx.asyncItems
import tornadofx.bindSelected
import tornadofx.checkbox
import tornadofx.chooseFile
import tornadofx.contextmenu
import tornadofx.customitem
import tornadofx.drawer
import tornadofx.dynamicContent
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.getValue
import tornadofx.item
import tornadofx.listview
import tornadofx.onChange
import tornadofx.pane
import tornadofx.rectangle
import tornadofx.setValue
import tornadofx.textfield
import tornadofx.toObservable
import tornadofx.toProperty
import tornadofx.wrapIn
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * @author Kevin Ludwig
 */
class FlowView(
    private val graphService: GraphServiceBlockingStub
) : View("Flow"), CoroutineScope {
    override val coroutineContext = Dispatchers.IO

    private val objectMapper by di<ObjectMapper>()
    private val spec = objectMapper.readValue<Spec>(graphService.getSpec(GetSpecRequest.getDefaultInstance()).spec.toByteArray())
    private val injector by di<Injector>()
    private var graphProvider = injector.getProvider(Graph::class.java)

    private val graphProperty = SimpleObjectProperty<Graph>()
    private var graph by graphProperty
    private val nodesProperty = SimpleListProperty(mutableListOf<VNode>().toObservable()).apply {
        graphProperty.onChange {
            it?.let {
                it.flow.nodes.forEach { node -> node.selectedProperty().onChange { if (it) this += node else this -= node } }
                it.flow.nodes.onChange {
                    while (it.next()) {
                        this -= it.removed.toSet()
                        it.addedSubList.map { node -> node.selectedProperty().onChange { if (it) this += node else this -= node } }
                    }
                }
            }
        }
    }

    override val root by fxml<Parent>("/flow.fxml")
    private val rootHbox by fxid<HBox>()
    private lateinit var graphsListView: ListView<Graph>
    private val graphHbox by fxid<HBox>()
    private val graphScrollPane by fxid<ScrollPane>()
    private val graphPane by fxid<Pane>()
    private val jsonTextArea by fxid<TextArea>()

    init {
        rootHbox.children.add(0, drawer(Side.LEFT) {
            item("Graphs", null, true) {
                minWidth = 200.0
                maxWidth = 200.0

                graphsListView = listview()
            }
        })
        with(graphsListView) {
            bindSelected(graphProperty)

            setCellFactory {
                TextFieldListCell<Graph>().apply {
                    converter = object : StringConverter<Graph>() {
                        override fun toString(`object`: Graph) = `object`.meta.name

                        override fun fromString(string: String): Graph {
                            item.meta.name = string
                            return item
                        }
                    }

                    setOnMouseClicked {
                        if (isEmpty) selectionModel.clearSelection()

                        it.consume()
                    }
                }
            }

            fun contextMenu(selectedComponents: ObservableList<out Graph>) = ContextMenu().apply {
                if (selectedComponents.isEmpty()) item("New Graph") { action { this@with.items += graphProvider.get() } }
                else {
                    item("Delete") {
                        action {
                            launch {
                                delete(selectedComponents)
                                this@FlowView.refresh()
                            }
                        }
                    }
                }
            }

            contextMenu = contextMenu(selectionModel.selectedItems)
            selectionModel.selectedItems.onChange { contextMenu = contextMenu(it.list) }
        }

        // Graph
        graphHbox.children += (drawer(Side.RIGHT, false, false) {
            item("Node", null, true) {
                minWidth = 200.0
                maxWidth = 200.0

                form {
                    styleClass += "background"

                    dynamicContent(nodesProperty) {
                        it?.singleOrNull()?.let {
                            fieldset {
                                it.connectors.forEach { connector ->
                                    (connector.valueObject.value as ConnectorValue?)?.let { (spec, value) ->
                                        if (connector.isInput && connector.type == "const") {
                                            field(spec.name) {
                                                when (spec.data) {
                                                    bitData -> checkbox(null, (value as? Boolean ?: false).toProperty().apply { onChange { (connector.valueObject.value as ConnectorValue).value = it } })
                                                    else -> {
                                                        val objectMapper = jacksonObjectMapper()
                                                        textfield(value?.let { objectMapper.writeValueAsString(it) } ?: "") {
                                                            minWidth = connector.node.width / 2.0

                                                            focusedProperty().onChange {
                                                                if (!it) (connector.valueObject.value as ConnectorValue).value = try {
                                                                    text.takeIf { it.isNotBlank() }?.let { objectMapper.readValue(it) }
                                                                } catch (_: Throwable) {
                                                                    text
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
        with(graphScrollPane) {
            contextMenu = contextmenu {
                val searchProperty = "".toProperty()

                customitem(hideOnClick = false) {
                    textfield { textProperty().onChange { searchProperty.value = it } }.also {
                        setOnKeyPressed { _ ->
                            it.requestFocus()
                            it.positionCaret(it.length)
                        }
                    }
                }

                val treeItems = mutableListOf<MenuItem>()
                val nodeItems = mutableMapOf<String, MenuItem>()
                spec.nodes.forEach { node ->
                    val name = node.name.split('/')
                    val path = StringBuilder()
                    var item: MenuItem? = null
                    name.forEachIndexed { i, element ->
                        path.append("${element}/")
                        val _styleClass = "menu-${path.toString().asNodeStyleClass()}"
                        item = when (val _item = item) {
                            is Menu -> _item.items.find { it.text == element } ?: if (i == name.lastIndex) MenuItem(element).apply {
                                _item.items += this
                                nodeItems[node.name] = this

                                styleClass += _styleClass

                                action {
                                    val local = sceneToLocal(x - currentWindow!!.x, y - currentWindow!!.y)
                                    graph?.newNode(node, Meta.Node(if (local.x.isNaN()) 0.0 else local.x, if (local.y.isNaN()) 0.0 else local.y))
                                }
                            } else Menu(element).apply {
                                _item.items += this

                                styleClass += _styleClass
                            }
                            null -> treeItems.find { it.text == element } ?: if (i == name.lastIndex) MenuItem(element).apply {
                                treeItems += this
                                nodeItems[node.name] = this

                                styleClass += _styleClass

                                action {
                                    val local = sceneToLocal(x - currentWindow!!.x, y - currentWindow!!.y)
                                    graph?.newNode(node, Meta.Node(if (local.x.isNaN()) 0.0 else local.x, if (local.y.isNaN()) 0.0 else local.y))
                                }
                            } else Menu(element).apply {
                                treeItems += this

                                styleClass += _styleClass
                            }
                            else -> error("$_item")
                        }
                    }
                }

                val items = items.toMutableList()
                this.items.addAll(treeItems)
                searchProperty.onChange { _search ->
                    this.items.setAll(items)
                    this.items.addAll(if (_search!!.isEmpty()) treeItems else nodeItems.filterKeys { it.contains(_search, true) }.values)
                }
            }
        }
        with(graphPane) {
            dynamicContent(graphProperty) { it?.flow?.setSkinFactories(SkinFactory(this)) }
            MouseControlUtil.addSelectionRectangleGesture(this, rectangle {
                stroke = Color.rgb(255, 255, 255, 1.0)
                fill = Color.rgb(0, 0, 0, 0.5)
            })
            minWidthProperty().bind(graphScrollPane.widthProperty())
            minHeightProperty().bind(graphScrollPane.heightProperty())

        }

        // Json
        with(jsonTextArea) { graphProperty.onChange { text = it?.let { objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it) } ?: "" } }

        // Initialization
        launch { refresh() }
    }

    constructor() : this(GraphServiceGrpc.newBlockingStub(ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build()))

    fun keyPressed(event: KeyEvent) {
        if (event.isControlDown) when (event.code) {
            /*KeyCode.C -> {
                graph?.let { graph -> graph.flow.nodes.filter(VNode::isSelected) }
                event.consume()
            }*/
            KeyCode.S -> {
                graph?.let { graph -> launch { graphService.updateGraph(UpdateGraphRequest.newBuilder().setGraph(ByteString.copyFrom(objectMapper.writeValueAsBytes(graph))).build()) } }
                event.consume()
            }
            /*KeyCode.V -> Unit*/
            else -> Unit
        } else when (event.code) {
            KeyCode.DELETE -> {
                graph?.let { graph -> graph.flow.nodes.filter(VNode::isSelected).forEach(graph.flow::remove) }
                event.consume()
            }
            KeyCode.F5 -> {
                launch { this@FlowView.refresh() }
                event.consume()
            }
            else -> Unit
        }
    }

    fun fileSettingsMenuItemAction() {
        find<SettingsView>().openModal()
    }

    fun fileImportMenuItemAction() {
        chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("Flow", "*.flw"), FileChooser.ExtensionFilter("All Files", "*.*"))).singleOrNull()?.let {
            val graph = it.inputStream().use { objectMapper.readValue<Graph>(GZIPInputStream(it)).also { it.spec = spec } }
            graphsListView.items += graph
            graphsListView.selectionModel.select(graph)
        }
    }

    fun fileExportAsMenuItemAction() {
        chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("Flow", "*.flw"), FileChooser.ExtensionFilter("All Files", "*.*")), mode = FileChooserMode.Save).singleOrNull()?.let { it.outputStream().use { objectMapper.writeValue(GZIPOutputStream(it), graph) } }
    }

    fun fileExitMenuItemAction() {
        close()
    }

    fun editLayoutIsomMenuItemAction() {
        LayoutGeneratorSmart().apply {
            layoutSelector = 0
            workflow = graph.flow.model
        }.generateLayout()
    }

    fun editLayoutFrMenuItemAction() {
        LayoutGeneratorSmart().apply {
            layoutSelector = 1
            workflow = graph.flow.model
        }.generateLayout()
    }

    fun editLayoutKkMenuItemAction() {
        LayoutGeneratorSmart().apply {
            layoutSelector = 2
            workflow = graph.flow.model
        }.generateLayout()
    }

    fun editLayoutDacMenuItemAction() {
        LayoutGeneratorSmart().apply {
            layoutSelector = 3
            workflow = graph.flow.model
        }.generateLayout()
    }

    fun helpAboutMenuItemAction() {
        find<AboutView>().openModal(resizable = false)
    }

    fun jsonTabSelectionChanged() {
        jsonTextArea.text = graph?.let { objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it) } ?: ""
    }

    private suspend fun refresh() {
        val graphs = objectMapper.readValue<List<Graph>>(graphService.listGraph(ListGraphRequest.getDefaultInstance()).graphs.toByteArray()).onEach { it.spec = spec }
        withContext(Dispatchers.Main) {
            val graphId = graph?.id
            graphsListView.items.setAll(graphs)
            graphsListView.selectionModel.select(graphs.singleOrNull { it.id == graphId })
        }
    }

    private fun delete(graphs: List<Graph>) {
        graphs.forEach { graphService.deleteGraph(DeleteGraphRequest.newBuilder().setGraphId(it.id.toString()).build()) }
    }

    companion object {
        private val bitData = jacksonObjectMapper().readTree("""{"type":"boolean"}""")
    }
}
