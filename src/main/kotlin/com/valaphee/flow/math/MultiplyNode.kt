package com.valaphee.flow.math

import com.valaphee.flow.Binding

/**
 * @author Kevin Ludwig
 */
class MultiplyNode(
    override val inA: Binding,
    override val inB: Binding,
    override val out: Binding
) : MathNode() {
    override suspend fun evaluate() {
        out.set {
            val a = inA()
            val b = inB()
            if (a is Number && b is Number) when (typeFor(a::class, b::class)) {
                Byte::class -> a.toByte() * b.toByte()
                Short::class -> a.toShort() * b.toShort()
                Int::class -> a.toInt() * b.toInt()
                Long::class -> a.toLong() * b.toLong()
                Float::class -> a.toFloat() * b.toFloat()
                Double::class -> a.toDouble() * b.toDouble()
                else -> TODO("$a * $b")
            } else TODO("$a * $b")
        }
    }
}
