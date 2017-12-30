package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.zalando.InvalidApiSchemaRule
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.stereotype.Component

class RulesValidatorTest {

    val swaggerContent = javaClass.classLoader.getResource("fixtures/api_spp.json").readText(Charsets.UTF_8)

    @Component
    class TestFirstRule : AbstractRule(TestRuleSet()) {
        override val title = "First Rule"
        override val id = javaClass.simpleName
        override val severity = Severity.SHOULD

        @Check(severity = Severity.SHOULD)
        fun validate(swagger: Swagger): List<Violation> = listOf(
                Violation("dummy1", listOf("x", "y", "z")),
                Violation("dummy2", listOf()))
    }

    @Component
    class TestSecondRule : AbstractRule(TestRuleSet()) {
        override val title = "Second Rule"
        override val id = javaClass.simpleName
        override val severity = Severity.MUST

        @Check(severity = Severity.MUST)
        fun validate(swagger: Swagger): Violation? =
                Violation("dummy3", listOf("a"))
    }

    @Component
    class TestBadRule : AbstractRule(TestRuleSet()) {
        override val title = "Third Rule"
        override val id = javaClass.simpleName
        override val severity = Severity.MUST

        @Check(severity = Severity.MUST)
        fun invalid(swagger: Swagger): String = "Hello World!"

        @Check(severity = Severity.MUST)
        fun invalidParams(swagger: Swagger, json: JsonNode): Violation? = null
    }

    val invalidApiSchemaRule: InvalidApiSchemaRule = mock(InvalidApiSchemaRule::class.java)

    @Test
    fun shouldReturnEmptyViolationsListWithoutRules() {
        val rules = emptyList<Rule>()
        val validator = SwaggerRulesValidator(rulesManager(rules), invalidApiSchemaRule)
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results)
                .isEmpty()
    }

    @Test
    fun shouldReturnOneViolation() {
        val rules = listOf(TestSecondRule())
        val validator = SwaggerRulesValidator(rulesManager(rules), invalidApiSchemaRule)
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results.map(Result::toViolation).map(Violation::description))
                .containsExactly("dummy3")
    }

    @Test
    fun shouldCollectViolationsOfAllRules() {
        val rules = listOf(TestFirstRule())
        val validator = SwaggerRulesValidator(rulesManager(rules), invalidApiSchemaRule)
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results.map(Result::toViolation).map(Violation::description))
                .containsExactly("dummy1", "dummy2")
    }

    @Test
    fun shouldSortViolationsByViolationType() {
        val rules = listOf(TestFirstRule(), TestSecondRule())
        val validator = SwaggerRulesValidator(rulesManager(rules), invalidApiSchemaRule)
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results.map(Result::toViolation).map(Violation::description))
                .containsExactly("dummy3", "dummy1", "dummy2")
    }

    @Test
    fun shouldIgnoreSpecifiedRules() {
        val rules = listOf(TestFirstRule(), TestSecondRule())
        val validator = SwaggerRulesValidator(rulesManager(rules), invalidApiSchemaRule)
        val results = validator.validate(swaggerContent, RulesPolicy(arrayOf("TestSecondRule")))
        assertThat(results.map(Result::toViolation).map(Violation::description))
                .containsExactly("dummy1", "dummy2")
    }

    @Test
    fun shouldReturnInvalidApiSchemaRuleForBadSwagger() {
        val resultRule = mock(InvalidApiSchemaRule::class.java)
        Mockito.`when`(resultRule.title).thenReturn("InvalidApiSchemaRule Title")
        Mockito.`when`(resultRule.description).thenReturn("desc")

        val rules = emptyList<Rule>()
        val validator = SwaggerRulesValidator(rulesManager(rules), resultRule)
        val valResult = validator.validate("Invalid swagger content !@##", RulesPolicy(emptyArray()))
        assertThat(valResult).hasSize(1)
        assertThat(valResult[0].title).isEqualTo(resultRule.title)
    }

    @Test
    fun checkReturnsStringThrowsException() {
        val rules = listOf(TestBadRule())
        assertThatThrownBy {
            val validator = SwaggerRulesValidator(rulesManager(rules), invalidApiSchemaRule)
            validator.validate(swaggerContent, RulesPolicy(arrayOf("TestCheckApiNameIsPresentRule")))
        }.hasMessage("Unsupported return type for a @Check method!: class java.lang.String")
    }

    private fun rulesManager(rules: List<Rule>): RulesManager {
        return RulesManager(rules.map { instance -> RuleDetails(instance.ruleSet, instance) })
    }
}
