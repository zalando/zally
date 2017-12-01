package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.zalando.InvalidApiSchemaRule
import de.zalando.zally.rule.zalando.ZalandoRuleSet
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.groups.Tuple
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock

class RulesValidatorTest {

    private val swaggerContent = javaClass.classLoader.getResource("fixtures/api_spp.json").readText(Charsets.UTF_8)

    class MultiViolationRule : AbstractRule(ZalandoRuleSet()) {
        override val title = "First Rule"
        override val violationType = ViolationType.SHOULD
        override val id = "S999"

        @Check
        fun validate(swagger: Swagger): List<Violation> = listOfNotNull(
                Violation(this, this.title, "dummy2", ViolationType.MAY, listOf()),
                Violation(this, this.title, "dummy1", ViolationType.SHOULD, listOf("x", "y", "z"))
        )
    }

    class SingleViolationRule : AbstractRule(ZalandoRuleSet()) {
        override val title = "Second Rule"
        override val violationType = ViolationType.MUST
        override val id = "999"

        @Check
        fun validate(swagger: Swagger): Violation? =
                Violation(this, this.title, "dummy3", ViolationType.MUST, listOf("a"))
    }

    class BadRule : AbstractRule(ZalandoRuleSet()) {
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
        val rules = listOf(SingleViolationRule())
        val validator = SwaggerRulesValidator(rules, invalidApiSchemaRule)
        assertThat(validator.validate(swaggerContent, RulesPolicy(emptyArray())))
                .extracting("description", "paths")
                .containsExactly(
                        Tuple("dummy3", listOf("a"))
                )
    }

    @Test
    fun shouldCollectViolationsOfAllRules() {
        val rules = listOf(MultiViolationRule(), SingleViolationRule())
        val validator = SwaggerRulesValidator(rules, invalidApiSchemaRule)
        assertThat(validator.validate(swaggerContent, RulesPolicy(emptyArray())))
                .extracting("description", "paths")
                .containsExactly(
                        Tuple("dummy3", listOf("a")),
                        Tuple("dummy1", listOf("x", "y", "z")),
                        Tuple("dummy2", listOf<String>())
                )
    }

    @Test
    fun shouldIgnoreSpecifiedRules() {
        val rules = listOf(MultiViolationRule(), SingleViolationRule())
        val validator = SwaggerRulesValidator(rules, invalidApiSchemaRule)
        assertThat(validator.validate(swaggerContent, RulesPolicy(arrayOf("999"))))
                .extracting("description", "paths")
                .containsExactly(
                        Tuple("dummy1", listOf("x", "y", "z")),
                        Tuple("dummy2", listOf<String>())
                )
    }

    @Test
    fun shouldReturnInvalidApiSchemaRuleForBadSwagger() {
        val resultRule = mock(InvalidApiSchemaRule::class.java)
        Mockito.`when`(resultRule.title).thenReturn("InvalidApiSchemaRule Title")
        Mockito.`when`(resultRule.description).thenReturn("desc")
        Mockito.`when`(resultRule.violationType).thenReturn(ViolationType.MUST)

        val validator = SwaggerRulesValidator(emptyList(), resultRule)
        val valResult = validator.validate("Invalid swagger content !@##", RulesPolicy(emptyArray()))
        assertThat(valResult)
                .extracting("description", "paths")
                .containsExactly(
                        Tuple("desc", listOf<String>())
                )
    }

    @Test
    fun checkReturnsStringThrowsException() {
        assertThatThrownBy {
            val rules = listOf(BadRule())
            val validator = SwaggerRulesValidator(rules, invalidApiSchemaRule)
            validator.validate(swaggerContent, RulesPolicy(arrayOf("999")))
        }.hasMessage("Unsupported return type for a @Check check!: class java.lang.String")
    }
}
