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

package com.valaphee.cran.graph

import eu.mihosoft.vrl.workflow.ConnectionResult
import eu.mihosoft.vrl.workflow.fx.ConnectionListener
import javafx.scene.Node

/**
 * @author Kevin Ludwig
 */
class ConnectionListener : ConnectionListener {
    override fun onConnectionCompatible(node: Node) = Unit

    override fun onConnectionCompatibleReleased(node: Node) = Unit

    override fun onConnectionIncompatible() = Unit

    override fun onConnectionIncompatibleReleased(node: Node) = Unit

    override fun onCreateNewConnectionReleased(connectionResult: ConnectionResult) = Unit

    override fun onCreateNewConnectionReverseReleased(connectionResult: ConnectionResult) = Unit

    override fun onNoConnection(node: Node) = Unit

    override fun onRemoveConnectionReleased() = Unit
}
