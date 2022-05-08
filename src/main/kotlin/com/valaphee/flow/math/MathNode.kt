package com.valaphee.flow.math

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Binding
import com.valaphee.flow.Node
import kotlin.math.max
import kotlin.reflect.KClass

/**
 * @author Kevin Ludwig
 */
abstract class MathNode : Node() {
    @get:JsonProperty("in_a") abstract val inA: Binding
    @get:JsonProperty("in_b") abstract val inB: Binding
    @get:JsonProperty("out") abstract val out: Binding

    companion object {
        private val order = listOf(
            Byte::class,
            Short::class,
            Int::class,
            Long::class,
            Float::class,
            Double::class
        )

        @JvmStatic
        protected fun typeFor(a: KClass<*>, b: KClass<*>) = order[max(order.indexOf(a), order.indexOf(b))]
    }
}
