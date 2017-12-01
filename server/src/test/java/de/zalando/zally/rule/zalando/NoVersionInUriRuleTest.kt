package de.zalando.zally.rule.zalando

import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoVersionInUriRuleTest {

    private val rule = NoVersionInUriRule(ZalandoRuleSet())

    @Test
    fun returnsViolationsWhenVersionIsInTheBeginingOfBasePath() {
        val swagger = Swagger().apply { basePath = "/v1/tests" }
        assertThat(rule.validate(swagger))
                .hasFieldOrPropertyWithValue("description", "basePath attribute contains version number")
                .hasFieldOrPropertyWithValue("paths", listOf<String>())
    }

    @Test
    fun returnsViolationsWhenVersionIsInTheMiddleOfBasePath() {
        val swagger = Swagger().apply { basePath = "/api/v1/tests" }
        assertThat(rule.validate(swagger))
                .hasFieldOrPropertyWithValue("description", "basePath attribute contains version number")
                .hasFieldOrPropertyWithValue("paths", listOf<String>())
    }

    @Test
    fun returnsViolationsWhenVersionIsInTheEndOfBasePath() {
        val swagger = Swagger().apply { basePath = "/api/v1" }
        assertThat(rule.validate(swagger))
                .hasFieldOrPropertyWithValue("description", "basePath attribute contains version number")
                .hasFieldOrPropertyWithValue("paths", listOf<String>())
    }

    @Test
    fun returnsViolationsWhenVersionIsBig() {
        val swagger = Swagger().apply { basePath = "/v1024/tests" }
        assertThat(rule.validate(swagger))
                .hasFieldOrPropertyWithValue("description", "basePath attribute contains version number")
                .hasFieldOrPropertyWithValue("paths", listOf<String>())
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
