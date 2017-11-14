package de.zalando.zally.rule

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.zalando.ZalandoRuleSet
import io.swagger.models.Swagger
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RulesPolicyTest {
    class TestRule(val result: Violation?) : SwaggerRule(ZalandoRuleSet()) {
        override val title = "Test Rule"
        override val url = null
        override val violationType = ViolationType.MUST
        override val code = "M999"
        override val guidelinesCode = "000"
        override fun validate(swagger: Swagger): Violation? = result
    }

    @Test
    fun shouldAcceptRuleIfNotFiltered() {
        val policy = RulesPolicy(arrayOf("M001", "M002"), arrayOf(""))
        val violation = Violation(TestRule(null), "dummy1", "dummy", ViolationType.MUST, "dummy", listOf("x"))
        assertTrue(policy.accepts(TestRule(violation)))
    }

    @Test
    fun shouldNotAcceptRuleIfFiltered() {
        val policy = RulesPolicy(arrayOf("M001", "M999"), arrayOf(""))
        val violation = Violation(TestRule(null), "dummy1", "dummy", ViolationType.MUST, "dummy", listOf("x"))
        assertFalse(policy.accepts(TestRule(violation)))
    }

    @Test
    fun shouldNotAcceptRuleIfIgnoredPackage() {
        val policy = RulesPolicy(arrayOf(), arrayOf("de.zalando.zally.rule"))
        val violation = Violation(TestRule(null), "dummy1", "dummy", ViolationType.MUST, "dummy", listOf("x"))
        assertFalse(policy.accepts(TestRule(violation)))
    }
}
