package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.ast.JsonPointers
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import kotlin.reflect.full.createInstance

@Suppress("UndocumentedPublicClass", "StringLiteralDuplication")
class RulesValidatorTest {

    private val swaggerContent = javaClass.classLoader.getResource("fixtures/api_spp.json").readText(Charsets.UTF_8)

    @Rule(
        ruleSet = TestRuleSet::class,
        id = "TestFirstRule",
        severity = Severity.SHOULD,
        title = "First Rule"
    )
    class TestFirstRule {

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.SHOULD)
        fun validate(swagger: Swagger): List<Violation> = listOf("dummy1", "dummy2").map { Violation(it, JsonPointers.EMPTY) }
    }

    @Rule(
        ruleSet = TestRuleSet::class,
        id = "TestSecondRule",
        severity = Severity.MUST,
        title = "Second Rule"
    )
    class TestSecondRule {

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.MUST)
        fun validate(swagger: Swagger): Violation? = Violation("dummy3", JsonPointers.EMPTY)
    }

    @Rule(
        ruleSet = TestRuleSet::class,
        id = "TestBadRule",
        severity = Severity.MUST,
        title = "Third Rule"
    )
    class TestBadRule {

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.MUST)
        fun invalid(swagger: Swagger): String = "Hello World!"

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.MUST)
        fun invalidParams(swagger: Swagger, json: JsonNode, text: String): Violation? = null
    }

    @Test
    fun shouldReturnEmptyViolationsListWithoutRules() {
        val rules = emptyList<Any>()
        val validator = SwaggerRulesValidator(rulesManager(rules))
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results)
            .isEmpty()
    }

    @Test
    fun shouldReturnOneViolation() {
        val rules = listOf(TestSecondRule())
        val validator = SwaggerRulesValidator(rulesManager(rules))
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results.map(Result::description))
            .containsExactly("dummy3")
    }

    @Test
    fun shouldCollectViolationsOfAllRules() {
        val rules = listOf(TestFirstRule())
        val validator = SwaggerRulesValidator(rulesManager(rules))
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results.map(Result::description))
            .containsExactly("dummy1", "dummy2")
    }

    @Test
    fun shouldSortViolationsByViolationType() {
        val rules = listOf(TestFirstRule(), TestSecondRule())
        val validator = SwaggerRulesValidator(rulesManager(rules))
        val results = validator.validate(swaggerContent, RulesPolicy(emptyArray()))
        assertThat(results.map(Result::description))
            .containsExactly("dummy3", "dummy1", "dummy2")
    }

    @Test
    fun shouldIgnoreSpecifiedRules() {
        val rules = listOf(TestFirstRule(), TestSecondRule())
        val validator = SwaggerRulesValidator(rulesManager(rules))
        val results = validator.validate(swaggerContent, RulesPolicy(arrayOf("TestSecondRule")))
        assertThat(results.map(Result::description))
            .containsExactly("dummy1", "dummy2")
    }

    @Test
    fun checkReturnsStringThrowsException() {
        val rules = listOf(TestBadRule())
        assertThatThrownBy {
            val validator = SwaggerRulesValidator(rulesManager(rules))
            validator.validate(swaggerContent, RulesPolicy(arrayOf("TestCheckApiNameIsPresentRule")))
        }.hasMessage("Unsupported return type for a @Check method!: class java.lang.String")
    }

    private fun rulesManager(rules: List<Any>): RulesManager {
        return RulesManager(
            rules.mapNotNull { instance ->
                val rule = instance::class.java.getAnnotation(Rule::class.java)
                val ruleSet = rule?.ruleSet?.createInstance()
                ruleSet?.let { RuleDetails(ruleSet, rule, instance) }
            }
        )
    }
}
