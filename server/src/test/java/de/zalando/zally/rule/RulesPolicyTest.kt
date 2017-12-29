package de.zalando.zally.rule

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.zalando.ZalandoRuleSet
import io.swagger.models.Swagger
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.springframework.stereotype.Component

class RulesPolicyTest {

    /** TestRule used for testing RulesPolicy */
    @Component
    class TestRule(val result: Violation?) : AbstractRule(ZalandoRuleSet()) {
        override val title = "Test Rule"
        override val id = javaClass.simpleName
        override val severity = Severity.MUST

        @Check(severity = Severity.MUST)
        fun validate(swagger: Swagger): Violation? = result
    }

    @Test
    fun shouldAcceptRuleIfNotFiltered() {
        val policy = RulesPolicy(arrayOf("TestCheckApiNameIsPresentJsonRule", "136"))
        assertTrue(policy.accepts(TestRule(null)))
    }

    @Test
    fun shouldNotAcceptRuleIfFiltered() {
        val policy = RulesPolicy(arrayOf("TestCheckApiNameIsPresentJsonRule", "TestRule"))
        assertFalse(policy.accepts(TestRule(null)))
    }

    @Test
    fun withMoreIgnoresAllowsExtension() {
        val original = RulesPolicy(emptyArray())
        assertTrue(original.accepts(TestRule(null)))

        val extended = original.withMoreIgnores(listOf("TestCheckApiNameIsPresentJsonRule", "TestRule"))
        assertFalse(extended.accepts(TestRule(null)))

        // original is unmodified
        assertTrue(original.accepts(TestRule(null)))
    }
}
