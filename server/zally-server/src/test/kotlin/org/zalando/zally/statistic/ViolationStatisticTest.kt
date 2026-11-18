package org.zalando.zally.statistic

import com.fasterxml.jackson.core.JsonPointer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.zalando.zally.apireview.ApiReview
import org.zalando.zally.core.Result
import org.zalando.zally.dto.ApiDefinitionRequest
import org.zalando.zally.rule.api.Severity
import java.net.URI

class ViolationStatisticTest {
    @Test
    fun `is empty by default`() {
        val statistic = ViolationStatistic()

        assertThat(statistic.name).isNull()
        assertThat(statistic.type).isNull()
        assertThat(statistic.occurrence).isEqualTo(0)
    }

    @Test
    fun `takes name and type from the violation`() {
        val result =
            Result(
                id = "R1",
                url = URI.create("https://rules.example.com/R1"),
                title = "Title",
                description = "Description",
                violationType = Severity.SHOULD,
                pointer = JsonPointer.empty(),
            )
        val violation = ApiReview(ApiDefinitionRequest(), apiDefinition = "", violations = listOf(result)).ruleViolations.single()

        val statistic = ViolationStatistic(violation, 3)

        assertThat(statistic.name).isEqualTo("Title (R1)")
        assertThat(statistic.type).isEqualTo(Severity.SHOULD)
        assertThat(statistic.occurrence).isEqualTo(3)
    }
}
