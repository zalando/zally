package de.zalando.zally.core

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.config.ConfigFactory
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

@Suppress("UndocumentedPublicClass", "StringLiteralDuplication")
class RulesValidatorTest {

    private val swaggerContent =
        resourceToString("fixtures/api_spp.json")

    class RulesValidatorTestRuleSet : AbstractRuleSet()

    @Rule(
        ruleSet = RulesValidatorTestRuleSet::class,
        id = "TestFirstRule",
        severity = Severity.SHOULD,
        title = "First Rule"
    )
    class TestFirstRule {

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.SHOULD)
        fun validate(swagger: Swagger): List<Violation> = listOf("dummy1", "dummy2").map { Violation(it, EMPTY_JSON_POINTER) }
    }

    @Rule(
        ruleSet = RulesValidatorTestRuleSet::class,
        id = "TestSecondRule",
        severity = Severity.MUST,
        title = "Second Rule"
    )
    class TestSecondRule {

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.MUST)
        fun validate(swagger: Swagger): Violation? = Violation("dummy3", EMPTY_JSON_POINTER)
    }

    @Rule(
        ruleSet = RulesValidatorTestRuleSet::class,
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
        val validator = rulesValidator(emptyList())
        val results = validator.validate(swaggerContent, RulesPolicy(emptyList()))
        assertThat(results)
            .isEmpty()
    }

    @Test
    fun shouldReturnOneViolation() {
        val validator = rulesValidator(listOf(TestSecondRule()))
        val results = validator.validate(swaggerContent, RulesPolicy(emptyList()))
        assertThat(results.map(Result::description))
            .containsExactly("dummy3")
    }

    @Test
    fun shouldCollectViolationsOfAllRules() {
        val validator = rulesValidator(listOf(TestFirstRule()))
        val results = validator.validate(swaggerContent, RulesPolicy(emptyList()))
        assertThat(results.map(Result::description))
            .containsExactly("dummy1", "dummy2")
    }

    @Test
    fun shouldSortViolationsByViolationType() {
        val validator = rulesValidator(listOf(TestFirstRule(), TestSecondRule()))
        val results = validator.validate(swaggerContent, RulesPolicy(emptyList()))
        assertThat(results.map(Result::description))
            .containsExactly("dummy3", "dummy1", "dummy2")
    }

    @Test
    fun shouldIgnoreSpecifiedRules() {
        val validator = rulesValidator(listOf(TestFirstRule(), TestSecondRule()))
        val results = validator.validate(swaggerContent, RulesPolicy(listOf("TestSecondRule")))
        assertThat(results.map(Result::description))
            .containsExactly("dummy1", "dummy2")
    }

    @Test
    fun checkReturnsStringThrowsException() {
        assertThatThrownBy {
            val validator = rulesValidator(listOf(TestBadRule()))
            validator.validate(swaggerContent, RulesPolicy(listOf("TestCheckApiNameIsPresentRule")))
        }.hasMessage("Unsupported return type for a @Check method!: class java.lang.String")
    }

    private fun rulesValidator(rules: List<Any>): RulesValidator<Swagger> =
        object : RulesValidator<Swagger>(RulesManager.fromInstances(ConfigFactory.empty(), rules)) {
            override fun parse(content: String, authorization: String?): ContentParseResult<Swagger> =
                ContentParseResult.ParsedSuccessfully(Swagger())

            override fun ignore(root: Swagger, pointer: JsonPointer, ruleId: String): Boolean = false
        }

    private fun resourceToString(resourceName: String): String =
        IOUtils.toString(ClassLoader.getSystemResourceAsStream(resourceName))
}
