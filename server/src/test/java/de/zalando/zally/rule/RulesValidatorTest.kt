package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.zalando.InvalidApiSchemaRule
import de.zalando.zally.rule.zalando.ZalandoRuleSet
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class RulesValidatorTest {

    val DUMMY_VIOLATION_1 = Violation(FirstRule(null), "dummy1", "dummy", ViolationType.SHOULD, listOf("x", "y", "z"))
    val DUMMY_VIOLATION_2 = Violation(FirstRule(null), "dummy2", "dummy", ViolationType.MAY, listOf())
    val DUMMY_VIOLATION_3 = Violation(SecondRule(null), "dummy3", "dummy", ViolationType.MUST, listOf("a"))

    val swaggerContent = javaClass.classLoader.getResource("fixtures/api_spp.json").readText(Charsets.UTF_8)

    class FirstRule(val result: Violation?) : AbstractRule(ZalandoRuleSet()) {
        override val title = "First Rule"
        override val violationType = ViolationType.SHOULD
        override val id = "S999"

        @Check
        fun validate(swagger: Swagger): Violation? = result
    }

    class SecondRule(val result: Violation?) : AbstractRule(ZalandoRuleSet()) {
        override val title = "Second Rule"
        override val violationType = ViolationType.MUST
        override val id = "999"

        @Check
        fun validate(swagger: Swagger): List<Violation> = listOfNotNull(result)
    }

    class BadRule() : AbstractRule(ZalandoRuleSet()) {
        override val title = "Third Rule"
        override val violationType = ViolationType.MUST
        override val id = "M666"

        @Check
        fun invalid(swagger: Swagger): String = "Hello World!"

        @Check
        fun invalidParams(swagger: Swagger, json: JsonNode): Violation? = null
    }

    val invalidApiSchemaRule: InvalidApiSchemaRule = mock(InvalidApiSchemaRule::class.java)

    @Test
    fun shouldReturnEmptyViolationsListWithoutRules() {
        val validator = SwaggerRulesValidator(emptyList(), invalidApiSchemaRule)
        assertThat(validator.validate(swaggerContent, RulesPolicy(emptyArray()))).isEmpty()
    }

    @Test
    fun shouldReturnOneViolation() {
        val violations = listOf(DUMMY_VIOLATION_1)
        val validator = SwaggerRulesValidator(getRules(violations), invalidApiSchemaRule)
        assertThat(validator.validate(swaggerContent, RulesPolicy(emptyArray())).map(Result::toViolation)).hasSameElementsAs(violations)
    }

    @Test
    fun shouldCollectViolationsOfAllRules() {
        val violations = listOf(DUMMY_VIOLATION_1, DUMMY_VIOLATION_2)
        val validator = SwaggerRulesValidator(getRules(violations), invalidApiSchemaRule)
        assertThat(validator.validate(swaggerContent, RulesPolicy(emptyArray())).map(Result::toViolation)).hasSameElementsAs(violations)
    }

    @Test
    fun shouldSortViolationsByViolationType() {
        val violations = listOf(DUMMY_VIOLATION_1, DUMMY_VIOLATION_2, DUMMY_VIOLATION_3)
        val validator = SwaggerRulesValidator(getRules(violations), invalidApiSchemaRule)
        assertThat(validator.validate(swaggerContent, RulesPolicy(emptyArray())).map(Result::toViolation))
                .containsExactly(DUMMY_VIOLATION_3, DUMMY_VIOLATION_1, DUMMY_VIOLATION_2)
    }

    @Test
    fun shouldIgnoreSpecifiedRules() {
        val violations = listOf(DUMMY_VIOLATION_1, DUMMY_VIOLATION_2, DUMMY_VIOLATION_3)
        val validator = SwaggerRulesValidator(getRules(violations), invalidApiSchemaRule)
        assertThat(validator.validate(swaggerContent, RulesPolicy(arrayOf("999"))).map(Result::toViolation)).containsExactly(DUMMY_VIOLATION_1, DUMMY_VIOLATION_2)
    }

    @Test
    fun shouldReturnInvalidApiSchemaRuleForBadSwagger() {
        val resultRule = mock(InvalidApiSchemaRule::class.java)
        Mockito.`when`(resultRule.title).thenReturn("InvalidApiSchemaRule Title")
        Mockito.`when`(resultRule.description).thenReturn("desc")
        Mockito.`when`(resultRule.violationType).thenReturn(ViolationType.MUST)

        val validator = SwaggerRulesValidator(emptyList(), resultRule)
        val valResult = validator.validate("Invalid swagger content !@##", RulesPolicy(emptyArray()))
        assertThat(valResult).hasSize(1)
        assertThat(valResult[0].title).isEqualTo(resultRule.title)
    }

    @Test
    fun checkReturnsStringThrowsException() {
        assertThatThrownBy {
            val validator = SwaggerRulesValidator(listOf(BadRule()), invalidApiSchemaRule)
            validator.validate(swaggerContent, RulesPolicy(arrayOf("999")))
        }.hasMessage("Unsupported return type for a @Check check!: class java.lang.String")
    }

    fun getRules(violations: List<Violation>): List<Rule> {
        return violations.map {
            if (it.rule is FirstRule) {
                FirstRule(it)
            } else {
                SecondRule(it)
            }
        }
    }
}
