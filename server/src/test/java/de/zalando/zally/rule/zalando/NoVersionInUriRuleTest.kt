package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoVersionInUriRuleTest {

    private val rule = NoVersionInUriRule()

    val expectedViolation = Violation(
            "basePath attribute contains version number",
            emptyList())

    @Test
    fun returnsViolationsWhenVersionIsInTheBeginingOfBasePath() {
        val swagger = Swagger().apply { basePath = "/v1/tests" }
        assertThat(rule.validate(swagger)).isEqualTo(expectedViolation)
    }

    @Test
    fun returnsViolationsWhenVersionIsInTheMiddleOfBasePath() {
        val swagger = Swagger().apply { basePath = "/api/v1/tests" }
        assertThat(rule.validate(swagger)).isEqualTo(expectedViolation)
    }

    @Test
    fun returnsViolationsWhenVersionIsInTheEndOfBasePath() {
        val swagger = Swagger().apply { basePath = "/api/v1" }
        assertThat(rule.validate(swagger)).isEqualTo(expectedViolation)
    }

    @Test
    fun returnsViolationsWhenVersionIsBig() {
        val swagger = Swagger().apply { basePath = "/v1024/tests" }
        assertThat(rule.validate(swagger)).isEqualTo(expectedViolation)
    }

    @Test
    fun returnsEmptyViolationListWhenNoVersionFoundInURL() {
        val swagger = Swagger().apply { basePath = "/violations/" }
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun returnsEmptyViolationListWhenBasePathIsNull() {
        val swagger = Swagger()
        assertThat(rule.validate(swagger)).isNull()
    }
}
