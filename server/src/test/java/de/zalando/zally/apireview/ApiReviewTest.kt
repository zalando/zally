package de.zalando.zally.apireview

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.dto.ApiDefinitionRequest
import de.zalando.zally.rule.Result
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.util.resourceToString
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.IOException
import java.net.URI
import java.util.Arrays.asList

class ApiReviewTest {

    @Test
    fun shouldAggregateRuleTypeCount() {
        val mustViolation1 = result(Severity.MUST, JsonPointer.compile("/pointer1"))
        val mustViolation2 = result(Severity.MUST, JsonPointer.compile("/pointer2"))
        val shouldViolation = result(Severity.SHOULD, JsonPointer.compile("/pointer3"))

        val apiReview =
            ApiReview(ApiDefinitionRequest(), "", "", asList(mustViolation1, mustViolation2, shouldViolation))

        assertThat(apiReview.mustViolations).isEqualTo(2)
        assertThat(apiReview.shouldViolations).isEqualTo(1)
        assertThat(apiReview.mayViolations).isEqualTo(0)
        assertThat(apiReview.hintViolations).isEqualTo(0)
    }

    @Test
    @Throws(IOException::class)
    fun shouldCalculateNumberOfEndpoints() {
        val violation1 = result(Severity.MUST, JsonPointer.compile("/pointer1"))
        val violation2 = result(Severity.MUST, JsonPointer.compile("/pointer2"))

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
