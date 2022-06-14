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

import com.valaphee.cran.spec.Spec
import eu.mihosoft.vrl.workflow.Connector
import eu.mihosoft.vrl.workflow.DefaultConnectorValueObject
import javafx.beans.property.ObjectProperty

/**
 * @author Kevin Ludwig
 */
class ConnectorValueObject(
    connector: Connector,
    val spec: Spec.Node.Port,
    val multiKeyProperty: ObjectProperty<Any>? = null
) : DefaultConnectorValueObject(connector)
