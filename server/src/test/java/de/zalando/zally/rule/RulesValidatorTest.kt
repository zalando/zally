package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.zalando.InvalidApiSchemaRule
import de.zalando.zally.rule.zalando.ZalandoRuleSet
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class RulesValidatorTest {

    val swaggerContent = javaClass.classLoader.getResource("fixtures/api_spp.json").readText(Charsets.UTF_8)

    class FirstRule : AbstractRule(ZalandoRuleSet()) {
        override val title = "First Rule"
        override val violationType = ViolationType.SHOULD
        override val id = "S999"

        @Check
        fun validate(swagger: Swagger): List<Violation> = listOf(
                Violation("dummy1", ViolationType.SHOULD, listOf("x", "y", "z")),
                Violation("dummy2", ViolationType.MAY, listOf()))
    }

    class SecondRule : AbstractRule(ZalandoRuleSet()) {
        override val title = "Second Rule"
        override val violationType = ViolationType.MUST
        override val id = "999"

        @Check
        fun validate(swagger: Swagger): Violation? =
                Violation("dummy3", ViolationType.MUST, listOf("a"))
    }

    class BadRule : AbstractRule(ZalandoRuleSet()) {
        override val title = "Third Rule"
        override val violationType = ViolationType.MUST
        override val id = "M666"

        @Check
        fun invalid(swagger: Swagger): String = "Hello World!"

        @Check
        fun invalidParams(swagger: Swagger, json: JsonNode): Violation? = null
    }

    val invalidApiSchemaRule: InvalidApiSchemaRule = mock(InvalidApiSchemaRule::class.java)

    @Test
    fun shouldReturnEmptyViolationsListWithoutRules() {
        val rules = emptyList<Rule>()
        val validator = SwaggerRulesValidator(rules, invalidApiSchemaRule)
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results)
                .isEmpty()
    }

    @Test
    fun shouldReturnOneViolation() {
        val rules = listOf(SecondRule())
        val validator = SwaggerRulesValidator(rules, invalidApiSchemaRule)
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results.map(Result::toViolation).map(Violation::description))
                .containsExactly("dummy3")
    }

    @Test
    fun shouldCollectViolationsOfAllRules() {
        val rules = listOf(FirstRule())
        val validator = SwaggerRulesValidator(rules, invalidApiSchemaRule)
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results.map(Result::toViolation).map(Violation::description))
                .containsExactly("dummy1", "dummy2")
    }

    @Test
    fun shouldSortViolationsByViolationType() {
        val rules = listOf(FirstRule(), SecondRule())
        val validator = SwaggerRulesValidator(rules, invalidApiSchemaRule)
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results.map(Result::toViolation).map(Violation::description))
                .containsExactly("dummy3", "dummy1", "dummy2")
    }

    @Test
    fun shouldIgnoreSpecifiedRules() {
        val rules = listOf(FirstRule(), SecondRule())
        val validator = SwaggerRulesValidator(rules, invalidApiSchemaRule)
        val results = validator.validate(swaggerContent, RulesPolicy(arrayOf("999")))
        assertThat(results.map(Result::toViolation).map(Violation::description))
                .containsExactly("dummy1", "dummy2")
    }

    @Test
    fun shouldReturnInvalidApiSchemaRuleForBadSwagger() {
        val resultRule = mock(InvalidApiSchemaRule::class.java)
        Mockito.`when`(resultRule.title).thenReturn("InvalidApiSchemaRule Title")
        Mockito.`when`(resultRule.description).thenReturn("desc")
        Mockito.`when`(resultRule.violationType).thenReturn(ViolationType.MUST)

        val rules = emptyList<Rule>()
        val validator = SwaggerRulesValidator(rules, resultRule)
        val valResult = validator.validate("Invalid swagger content !@##", RulesPolicy(emptyArray()))
        assertThat(valResult).hasSize(1)
        assertThat(valResult[0].title).isEqualTo(resultRule.title)
    }

    @Test
    fun checkReturnsStringThrowsException() {
        val rules = listOf(BadRule())
        assertThatThrownBy {
            val validator = SwaggerRulesValidator(rules, invalidApiSchemaRule)
            validator.validate(swaggerContent, RulesPolicy(arrayOf("999")))
        }.hasMessage("Unsupported return type for a @Check check!: class java.lang.String")
    }
}
