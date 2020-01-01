package de.zalando.zally.test

import de.zalando.zally.rule.api.Violation
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.StringAssert

@Suppress("UndocumentedPublicClass")
class ViolationAssert(actual: Violation?) :
    AbstractAssert<ViolationAssert, Violation?>(actual, ViolationAssert::class.java) {

    fun descriptionEqualTo(description: String): ViolationAssert {
        description().isEqualTo(description)
        return this
    }

    fun descriptionMatches(description: String): ViolationAssert {
        description().matches(description)
        return this
    }

    private fun description() = StringAssert(actual?.description).`as`("description")

    fun pointerEqualTo(pointer: String): ViolationAssert {
        pointer().isEqualTo(pointer)
        return this
    }

    fun pointerMatches(pointer: String): ViolationAssert {
        pointer().matches(pointer)
        return this
    }

    private fun pointer() = StringAssert(actual?.pointer?.toString()).`as`("pointer")
}
