package de.zalando.zally.rule

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesPolicyTest {

    /** TestRule used for testing RulesPolicy */
    @Rule(
        ruleSet = TestRuleSet::class,
        id = "TestRule",
        severity = Severity.MUST,
        title = "Test Rule"
    )
    class TestRule(val result: Violation?) {

        @Suppress("UNUSED_PARAMETER")
        @Check(severity = Severity.MUST)
        fun validate(swagger: Swagger): Violation? = result
    }

    @Test
    fun shouldAcceptRuleIfNotFiltered() {
        val policy = RulesPolicy(arrayOf("TestCheckApiNameIsPresentJsonRule", "136"))
        assertTrue(policy.accepts(rule()))
    }

    @Test
    fun shouldNotAcceptRuleIfFiltered() {
        val policy = RulesPolicy(arrayOf("TestCheckApiNameIsPresentJsonRule", "TestRule"))
        assertFalse(policy.accepts(rule()))
    }

    @Test
    fun withMoreIgnoresAllowsExtension() {
        val original = RulesPolicy(emptyArray())
        assertTrue(original.accepts(rule()))

        val extended = original.withMoreIgnores(listOf("TestCheckApiNameIsPresentJsonRule", "TestRule"))
        assertFalse(extended.accepts(rule()))

        // original is unmodified
        assertTrue(original.accepts(rule()))
    }

    private fun rule() = TestRule(null).javaClass.getAnnotation(Rule::class.java)
}
