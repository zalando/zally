package de.zalando.zally.test

import de.zalando.zally.rule.api.Violation

@Suppress("UndocumentedPublicClass")
object ZallyAssertions {

    fun assertThat(actual: Violation?): ViolationAssert = ViolationAssert(actual)

    fun assertThat(actual: List<Violation>?): ViolationsAssert = ViolationsAssert(actual)
}
