package de.zalando.zally.rule

import de.zalando.zally.rule.api.Violation

class ZallyAssertions {
    companion object {
        fun assertThat(actual: Violation?): ViolationAssert = ViolationAssert(actual)
        fun assertThat(actual: List<Violation>?): ViolationsAssert = ViolationsAssert(actual)
    }
}
