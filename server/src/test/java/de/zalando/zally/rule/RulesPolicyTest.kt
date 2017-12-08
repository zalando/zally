package de.zalando.zally.rule

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.api.Check
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
        val policy = RulesPolicy(arrayOf("166", "136"), arrayOf(""))
        assertTrue(policy.accepts(TestRule(null)))
    }

    @Test
    fun shouldNotAcceptRuleIfFiltered() {
        val policy = RulesPolicy(arrayOf("166", "999"), arrayOf(""))
        assertFalse(policy.accepts(TestRule(null)))
    }

    @Test
    fun shouldNotAcceptRuleIfIgnoredPackage() {
        val policy = RulesPolicy(arrayOf(), arrayOf("de.zalando.zally.rule"))
        val violation = Violation(TestRule(null), "dummy1", "dummy", ViolationType.MUST, listOf("x"))
        assertFalse(policy.accepts(TestRule(violation)))
    }

    @Test
    fun withMoreIgnoresAllowsExtension() {

        val original = RulesPolicy(emptyArray(), emptyArray())
        assertTrue(original.accepts(TestRule(null)))

        val extended = original.withMoreIgnores(listOf("166", "999"))
        assertFalse(extended.accepts(TestRule(null)))

        // original is unmodified
        assertTrue(original.accepts(TestRule(null)))
    }
}
