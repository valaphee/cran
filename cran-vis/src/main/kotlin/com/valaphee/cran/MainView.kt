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

package com.valaphee.cran

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Injector
import com.valaphee.cran.graph.GraphImpl
import com.valaphee.cran.graph.SkinFactory
import com.valaphee.cran.meta.Meta
import com.valaphee.cran.settings.Settings
import com.valaphee.cran.settings.SettingsView
import com.valaphee.cran.spec.Spec
import com.valaphee.cran.spec.SpecLookup
import com.valaphee.cran.svc.graph.v1.DeleteGraphRequest
import com.valaphee.cran.svc.graph.v1.GetSpecRequest
import com.valaphee.cran.svc.graph.v1.GraphServiceGrpc
import com.valaphee.cran.svc.graph.v1.ListGraphRequest
import com.valaphee.cran.svc.graph.v1.RunGraphRequest
import com.valaphee.cran.svc.graph.v1.StopGraphRequest
import com.valaphee.cran.svc.graph.v1.UpdateGraphRequest
import com.valaphee.cran.util.PathTree
import com.valaphee.cran.util.PathTree.Companion.get
import com.valaphee.cran.util.asStyleClass
import com.valaphee.cran.util.update
import eu.mihosoft.vrl.workflow.VNode
import eu.mihosoft.vrl.workflow.incubating.LayoutGeneratorSmart
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Parent
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.control.cell.TextFieldTreeCell
import javafx.scene.input.DataFormat
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.TransferMode
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
import tornadofx.chooseFile
import tornadofx.contextmenu
import tornadofx.customitem
import tornadofx.drawer
import tornadofx.dynamicContent
import tornadofx.get
import tornadofx.getValue
import tornadofx.item
import tornadofx.onChange
import tornadofx.rectangle
import tornadofx.selectedValue
import tornadofx.separator
import tornadofx.setContent
import tornadofx.setValue
import tornadofx.textfield
import tornadofx.toProperty
import tornadofx.treeview
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * @author Kevin Ludwig
 */
class MainView(
    environment: Settings.Environment
) : View("%main"), CoroutineScope, SpecLookup {
    override val coroutineContext = Dispatchers.IO

    private val channel = environment.toChannel()
    internal val graphService = GraphServiceGrpc.newBlockingStub(channel)

    private val objectMapper by di<ObjectMapper>()
    private val spec = objectMapper.readValue<Spec>(graphService.getSpec(GetSpecRequest.getDefaultInstance()).spec.toByteArray())
    private val injector by di<Injector>()
    private var graphProvider = injector.getProvider(GraphImpl::class.java)

    private val graphProperty = SimpleObjectProperty<GraphImpl>().apply {
        update {
            title = "${environment.target}${it?.let { " - ${it.name}" } ?: ""}"
        }
    }
    private var graph by graphProperty

    override val root by fxml<Parent>("/main.fxml")
    private val rootHbox by fxid<HBox>()
    private lateinit var graphsTreeView: TreeView<PathTree<GraphImpl>>
    private val graphScrollPane by fxid<ScrollPane>()
    private val graphPane by fxid<Pane>()
    private val jsonFormat by fxid<HBox>()
    private val jsonTextArea by fxid<TextArea>()

    init {
        rootHbox.children.add(0, drawer {
            item(messages["main.graphs"], null, true) {
                minWidth = 200.0
                maxWidth = 200.0

                graphsTreeView = treeview()
            }
        })

        with(graphsTreeView) {
            root = TreeItem(PathTree())
            isShowRoot = false

            setOnKeyPressed {
                if (it.isControlDown) when (it.code) {
                    KeyCode.ENTER -> {
                        selectedValue?.value?.let { graph = it }
                        it.consume()
                    }
                    KeyCode.DELETE -> {
                        selectedValue?.value?.let {
                            launch {
                                delete(it)
                                _refresh()
                            }
                        }
                        it.consume()
                    }
                    else -> Unit
                }
            }

            contextmenu {
                item(messages["main.graphs.new"]) { action { graphsTreeMerge(listOf(graphProvider.get())) } }
                separator()
                item(messages["main.graphs.delete"]) {
                    action {
                        selectedValue?.value?.let {
                            launch {
                                delete(it)
                                _refresh()
                            }
                        }
                    }
                }
                item(messages["main.graphs.rename"])
            }

            setCellFactory {
                TextFieldTreeCell<PathTree<GraphImpl>>().apply {
                    setOnMouseClicked {
                        if (it.clickCount == 2) {
                            item?.value?.let { graph = it }
                            it.consume()
                        }
                    }

                    setOnDragDetected {
                        item?.value?.let {
                            startDragAndDrop(TransferMode.COPY).apply {
                                dragView = snapshot(null, null)
                                setContent { this[nodeSpecDataFormat] = objectMapper.writeValueAsString(it.toSpec()) }
                            }
                        }
                        it.consume()
                    }

                    converter = object : StringConverter<PathTree<GraphImpl>>() {
                        override fun toString(`object`: PathTree<GraphImpl>) = `object`.name

                        override fun fromString(string: String) = PathTree(item.path, string, item.value?.also { it.name = string })
                    }
                }
            }
        }

        // Graph
        with(graphScrollPane) {
            contextmenu {
                val searchProperty = "".toProperty()

                customitem(hideOnClick = false) {
                    textfield { textProperty().onChange { searchProperty.value = it } }.also {
                        setOnKeyPressed { _ ->
                            it.requestFocus()
                            it.positionCaret(it.length)
                        }
                    }
                }

                val nodeItems = mutableMapOf<String, MenuItem>()
                val treeItems = (PathTree(spec.nodes) { it.name }.convert<MenuItem> { parent, tree ->
                    val _styleClass = tree.path.asStyleClass()
                    tree.value?.let {
                        MenuItem(tree.name).apply {
                            (parent as Menu?)?.let { it.items += this }
                            nodeItems[tree.path] = this

                            styleClass += _styleClass

                            action {
                                val local = sceneToLocal(x - currentWindow!!.x, y - currentWindow!!.y)
                                graph?.newNode(it, Meta.Node(if (local.x.isNaN()) 0.0 else local.x, if (local.y.isNaN()) 0.0 else local.y))
                            }
                        }
                    } ?: Menu(tree.name).apply {
                        (parent as Menu?)?.let { it.items += this }

                        styleClass += _styleClass
                    }
                } as Menu).items

                val items = items.toMutableList()
                this.items.addAll(treeItems)
                searchProperty.onChange { _search ->
                    this.items.setAll(items)
                    this.items.addAll(if (_search!!.isEmpty()) treeItems else nodeItems.filterKeys { it.contains(_search, true) }.values)
                }
            }
        }
        with(graphPane) {
            minWidthProperty().bind(graphScrollPane.widthProperty())
            minHeightProperty().bind(graphScrollPane.heightProperty())

            dynamicContent(graphProperty) { it?.flow?.setSkinFactories(SkinFactory(this, null, this@MainView)) }

            MouseControlUtil.addSelectionRectangleGesture(this, rectangle {
                stroke = Color.rgb(255, 255, 255, 1.0)
                fill = Color.rgb(0, 0, 0, 0.5)
            })

            setOnDragOver {
                if (it.dragboard.hasContent(nodeSpecDataFormat)) {
                    it.acceptTransferModes(TransferMode.COPY)
                    it.consume()
                }
            }

            setOnDragDropped {
                if (it.dragboard.hasContent(nodeSpecDataFormat)) {
                    graph?.newNode(objectMapper.readValue(it.dragboard.getContent(nodeSpecDataFormat) as String), Meta.Node(it.x, it.y))
                    it.consume()
                }
            }
        }

        // Json
        with(jsonTextArea) { graphProperty.onChange { text = it?.let { objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it) } ?: "" } }

        // Initialization
        launch { _refresh() }
    }

    override fun getNodeSpec(name: String) = spec.nodes.find { it.name == name } ?: graphsTreeView.root?.get(name)?.toSpec()

    fun keyPressed(event: KeyEvent) {
        if (event.isControlDown) when (event.code) {
            KeyCode.A -> {
                graph?.let { it.flow.nodes.forEach { it.requestSelection(true) } }
                event.consume()
            }
            KeyCode.S -> {
                graph?.let { graph -> launch { graphService.updateGraph(UpdateGraphRequest.newBuilder().setGraph(objectMapper.writeValueAsString(graph)).build()) } }
                event.consume()
            }
            else -> Unit
        } else when (event.code) {
            KeyCode.DELETE -> {
                graph?.let { it.flow.nodes.filter(VNode::isSelected).forEach(it.flow::remove) }
                event.consume()
            }
            KeyCode.F5 -> {
                launch { this@MainView._refresh() }
                event.consume()
            }
            KeyCode.F11 -> {
                primaryStage.isFullScreen = !primaryStage.isFullScreen
                event.consume()
            }
            else -> Unit
        }
    }

    fun fileSettingsMenuItemAction() {
        openInternalWindow<SettingsView>()
    }

    fun fileImportMenuItemAction() {
        chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("Graph", "*.gph"), FileChooser.ExtensionFilter("All Files", "*.*"))).singleOrNull()?.let { graphsTreeMerge(listOf(it.inputStream().use { objectMapper.readValue<GraphImpl>(GZIPInputStream(it)).also { it.specLookup = this } })) }
    }

    fun fileExportAsMenuItemAction() {
        chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("Graph", "*.gph"), FileChooser.ExtensionFilter("All Files", "*.*")), mode = FileChooserMode.Save).singleOrNull()?.let { it.outputStream().use { objectMapper.writeValue(GZIPOutputStream(it), graph) } }
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

    fun editLayoutDagMenuItemAction() {
        LayoutGeneratorSmart().apply {
            layoutSelector = 3
            workflow = graph.flow.model
        }.generateLayout()
    }

    fun helpAboutMenuItemAction() {
        openInternalWindow<AboutView>()
    }

    fun runButtonAction() {
        graphService.runGraph(RunGraphRequest.newBuilder().setGraphName(graph.name).build())
    }

    fun stopButtonAction() {
        graphService.stopGraph(StopGraphRequest.newBuilder().setScopeId("").build())
    }

    fun jsonTabSelectionChanged() {
        jsonTextArea.text = graph?.let { objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it) } ?: ""
    }

    private fun graphsTreeMerge(graphs: List<GraphImpl>, replace: Boolean = false) {
         PathTree(graphs) { it.name }.convert<TreeItem<PathTree<GraphImpl>>>(graphsTreeView.root, { parent, tree -> parent?.children?.find { it.value.path == tree.path }?.also { it.value = tree } ?: TreeItem(tree) }) { parent, children -> if (replace) parent?.children?.setAll(children) else parent?.children?.addAll(children) }
    }

    private suspend fun _refresh() {
        val graphs = graphService.listGraph(ListGraphRequest.getDefaultInstance()).graphsList.map { objectMapper.readValue<GraphImpl>(it).apply { specLookup = this@MainView } }
        withContext(Dispatchers.Main) { graphsTreeMerge(graphs, true) }
    }

    private fun delete(graph: GraphImpl) {
        graphService.deleteGraph(DeleteGraphRequest.newBuilder().setGraphName(graph.name).build())
    }

    companion object {
        private val nodeSpecDataFormat = DataFormat(Spec.Node.MediaType)
    }
}
