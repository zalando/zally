package de.zalando.zally.rule

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.api.Check
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

        @Check
        fun validate(swagger: Swagger): Violation? = result
    }

    @Test
    fun shouldAcceptRuleIfNotFiltered() {
        val policy = RulesPolicy(arrayOf("M001", "M002"))
        assertTrue(policy.accepts(TestRule(null)))
    }

    @Test
    fun shouldNotAcceptRuleIfFiltered() {
        val policy = RulesPolicy(arrayOf("M001", "M999"))
        assertFalse(policy.accepts(TestRule(null)))
    }

    @Test
    fun withMoreIgnoresAllowsExtension() {

        val original = RulesPolicy(emptyArray())
        assertTrue(original.accepts(TestRule(null)))

        val extended = original.withMoreIgnores(listOf("M001", "M999"))
        assertFalse(extended.accepts(TestRule(null)))

        // original is unmodified
        assertTrue(original.accepts(TestRule(null)))
    }
}
