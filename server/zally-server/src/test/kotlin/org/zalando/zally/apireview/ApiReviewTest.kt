package org.zalando.zally.apireview

import com.fasterxml.jackson.core.JsonPointer
import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.dto.ApiDefinitionRequest
import org.zalando.zally.core.Result
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.util.resourceToString
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.IOException
import java.net.URI
import java.util.Arrays.asList

class ApiReviewTest {

    @Test
    fun shouldAggregateRuleTypeCount() {
        val mustViolation1 = result(Severity.MUST, "/pointer1".toJsonPointer())
        val mustViolation2 = result(Severity.MUST, "/pointer2".toJsonPointer())
        val shouldViolation = result(Severity.SHOULD, "/pointer3".toJsonPointer())

        val apiReview =
            ApiReview(ApiDefinitionRequest(), "", "", listOf(mustViolation1, mustViolation2, shouldViolation))

        assertThat(apiReview.mustViolations).isEqualTo(2)
        assertThat(apiReview.shouldViolations).isEqualTo(1)
        assertThat(apiReview.mayViolations).isEqualTo(0)
        assertThat(apiReview.hintViolations).isEqualTo(0)
    }

    @Test
    @Throws(IOException::class)
    fun shouldCalculateNumberOfEndpoints() {
        val violation1 = result(Severity.MUST, "/pointer1".toJsonPointer())
        val violation2 = result(Severity.MUST, "/pointer2".toJsonPointer())

        val apiDefinition = resourceToString("fixtures/limitNumberOfResourcesValid.json")

        val apiReview = ApiReview(ApiDefinitionRequest(), "", apiDefinition, asList(violation1, violation2))

        assertThat(apiReview.numberOfEndpoints).isEqualTo(2)
    }

    @Test
    @Throws(IOException::class)
    fun shouldParseApiNameFromApiDefinition() {
        val apiDefinition = resourceToString("fixtures/limitNumberOfResourcesValid.json")
        val apiReview = ApiReview(ApiDefinitionRequest(), "", apiDefinition)
        assertThat(apiReview.name).isEqualTo("Test Service")
    }

    private fun result(severity: Severity, pointer: JsonPointer): Result =
        Result(
            id = "TestRuleId",
            url = URI.create("http://rules.example.com/test"),
            title = "Test Rule Title",
            description = "Description of test rule",
            violationType = severity,
            pointer = pointer
        )
}
