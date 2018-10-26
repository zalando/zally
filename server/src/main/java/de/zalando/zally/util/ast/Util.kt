package de.zalando.zally.util.ast

internal object Util {
    val PRIMITIVES = setOf(
        String::class.java,
        Int::class.java,
        Float::class.java,
        Double::class.java,
        Boolean::class.java,
        Enum::class.java
    )
}
