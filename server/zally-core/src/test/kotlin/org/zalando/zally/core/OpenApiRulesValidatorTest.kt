package org.zalando.zally.core

import com.fasterxml.jackson.core.JsonPointer
import com.typesafe.config.ConfigFactory
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class OpenApiRulesValidatorTest {
    class RulesValidatorTestRuleSet : AbstractRuleSet()

    @Rule(
        ruleSet = RulesValidatorTestRuleSet::class,
        id = "TestExtensionRule",
        severity = Severity.MUST,
        title = "TestExtensionRule"
    )
    class TestExtensionRule {
        @Check(severity = Severity.MUST)
        fun validate(context: Context): List<Violation> {
            val testExtension = context
                ?.api
                ?.info
                ?.extensions?.get("x-test-extension")
                ?.let { it as? Map<*, *>? }

            return testExtension
                ?.values
                ?.filterNotNull()
                ?.map { value -> context.violation("Invalid value", value) }
                ?: listOf()
        }
    }

    @Test
    fun checkCorrectLineNumbersForExtensionViolation() {
        @Language("yaml")
        val openApiContent = """
            openapi: '3.0.0'
            info:
              x-test-extension:
                nested: 42
                paths: test-string
                and:
                  some:
                    more: 12
              title: Lorem Ipsum
            paths: {}
            """.trimIndent()

        val validator = openApiRulesValidator(listOf(TestExtensionRule()), DefaultContextFactory())
        val results = validator.validate(openApiContent, RulesPolicy(emptyList()))
        assertThat(results.map { it.pointer }).containsExactly(
            JsonPointer.compile("/info/x-test-extension/nested"),
            JsonPointer.compile("/info/x-test-extension/paths"),
            JsonPointer.compile("/info/x-test-extension/and")
        )

        assertThat(results.map { it.lines }).containsExactly(
            IntRange(4, 4),
            IntRange(5, 5),
            IntRange(6, 9)
        )
    }

    private fun openApiRulesValidator(rules: List<Any>, context: DefaultContextFactory): RulesValidator<Context> =
        object : RulesValidator<Context>(RulesManager.fromInstances(ConfigFactory.empty(), rules)) {
            override fun parse(content: String, authorization: String?): ContentParseResult<Context> =
                context.parseOpenApiContext(content, authorization)

            override fun ignore(root: Context, pointer: JsonPointer, ruleId: String): Boolean = false
        }
}
