package com.valaphee.flow

import com.fasterxml.jackson.annotation.ObjectIdGenerator
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver
import java.util.UUID

/**
 * @author Kevin Ludwig
 */
class BindingIdResolver : SimpleObjectIdResolver() {
    override fun resolveId(id: ObjectIdGenerator.IdKey) = super.resolveId(id) ?: Binding(id.key as UUID).also { bindItem(id, it) }

    override fun newForDeserialization(context: Any?) = BindingIdResolver()
}
