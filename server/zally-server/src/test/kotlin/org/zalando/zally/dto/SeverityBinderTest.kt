package org.zalando.zally.dto

import org.zalando.zally.rule.api.Severity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SeverityBinderTest {
    @Test()
    fun shouldRaiseIllegalArgumentExceptionWhenTypeIsNotFound() {
        val typeBinder = SeverityBinder()
        assertThrows(IllegalArgumentException::class.java, { typeBinder.asText = "SOLUTION" })
    }

    @Test
    fun shouldSetValueWhenTypeIsFound() {
        val typeBinder = SeverityBinder()
        val allowedTypes = arrayOf("Must", "MUST", "must", "SHOULD", "MAY", "HINT")

        for (allowedType in allowedTypes) {
            val expectedType = Severity.valueOf(allowedType.toUpperCase())
            typeBinder.asText = allowedType

            assertThat(typeBinder.value).isEqualTo(expectedType)
        }
    }
}
