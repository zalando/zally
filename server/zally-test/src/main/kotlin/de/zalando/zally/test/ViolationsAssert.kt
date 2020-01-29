package de.zalando.zally.test

import de.zalando.zally.rule.api.Violation
import org.assertj.core.api.AbstractListAssert
import org.assertj.core.api.ListAssert
import org.assertj.core.api.ObjectAssert

@Suppress("UndocumentedPublicClass", "SpreadOperator")
class ViolationsAssert(violations: List<Violation>?) :
    AbstractListAssert<ViolationsAssert, List<Violation>, Violation, ObjectAssert<Violation>>(
        violations,
        ViolationsAssert::class.java
    ) {
    override fun newAbstractIterableAssert(iterable: MutableIterable<Violation>?): ViolationsAssert =
        ViolationsAssert(violations = iterable?.toList())

    override fun toAssert(value: Violation?, description: String?): ObjectAssert<Violation> {
        return ObjectAssert<Violation>(value).`as`(description)
    }

    fun descriptionsAllEqualTo(description: String): ViolationsAssert {
        descriptions().containsOnly(description)
        return this
    }

    fun descriptionsAllMatch(regex: Regex): ViolationsAssert {
        descriptions().allMatch { regex.matches(it) }
        return this
    }

    fun descriptionsEqualTo(vararg descriptions: String): ViolationsAssert {
        descriptions().containsExactly(*descriptions)
        return this
    }

    private fun descriptions(): ListAssert<String> {
        isNotNull
        return ListAssert(actual.map { it.description }).`as`("descriptions")
    }

    fun pointersAllEqualTo(pointer: String): ViolationsAssert {
        pointers().containsOnly(pointer)
        return this
    }

    fun pointersEqualTo(vararg pointers: String): ViolationsAssert {
        pointers().containsExactly(*pointers)
        return this
    }

    private fun pointers(): ListAssert<String> {
        isNotNull
        return ListAssert(actual.map { it.pointer.toString() }).`as`("pointers")
    }
}
