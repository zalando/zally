package de.zalando.zally.rule

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.zalando.ZalandoRuleSet
import io.swagger.models.Swagger
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesPolicyTest {
    class TestRule(val result: Violation?) : AbstractRule(ZalandoRuleSet()) {
        override val title = "Test Rule"
        override val violationType = ViolationType.MUST
        override val id = "999"

        @Check
        fun validate(swagger: Swagger): Violation? = result
    }

    @Test
    fun shouldAcceptRuleIfNotFiltered() {
        val policy = RulesPolicy(arrayOf("166", "136"))
        assertTrue(policy.accepts(TestRule(null)))
    }

    @Test
    fun shouldNotAcceptRuleIfFiltered() {
        val policy = RulesPolicy(arrayOf("166", "999"))
        assertFalse(policy.accepts(TestRule(null)))
    }

    @Test
    fun withMoreIgnoresAllowsExtension() {

        val original = RulesPolicy(emptyArray())
        assertTrue(original.accepts(TestRule(null)))

        val extended = original.withMoreIgnores(listOf("166", "999"))
        assertFalse(extended.accepts(TestRule(null)))

        // original is unmodified
        assertTrue(original.accepts(TestRule(null)))
    }
}
