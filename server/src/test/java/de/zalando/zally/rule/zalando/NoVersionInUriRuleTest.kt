package de.zalando.zally.rule.zalando

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoVersionInUriRuleTest {

    private val rule = NoVersionInUriRule(ZalandoRuleSet())

    val expectedViolation = Violation(
            rule,
            "Do Not Use URI Versioning",
            "basePath attribute contains version number",
            ViolationType.MUST,
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
