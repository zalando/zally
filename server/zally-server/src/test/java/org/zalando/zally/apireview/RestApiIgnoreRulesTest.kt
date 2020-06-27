package org.zalando.zally.apireview

import org.zalando.zally.util.readApiDefinition
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.test.context.TestPropertySource
import java.io.IOException

@TestPropertySource(properties = ["zally.ignoreRules=TestCheckAlwaysReport3MustViolations"])
class RestApiIgnoreRulesTest : RestApiBaseTest() {
    @Test
    @Throws(IOException::class)
    fun testShouldNotReportViolationsIfIgnoreIsPresent() {
        val response = sendApiDefinition(readApiDefinition("fixtures/openapi3_petstore_expanded.json"))
        assertThat(response.violations).isEmpty()
    }
}
