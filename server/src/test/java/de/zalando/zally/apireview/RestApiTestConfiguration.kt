package de.zalando.zally.apireview

import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.config.Config
import de.zalando.zally.core.AbstractRuleSet
import de.zalando.zally.core.EMPTY_JSON_POINTER
import de.zalando.zally.core.RulesManager
import de.zalando.zally.core.toJsonPointer
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class RestApiTestConfiguration {

    companion object {
        fun assertRuleManagerUsingLimitedRules(rulesManager: RulesManager) {
            assertThat(rulesManager.rules.map { it.ruleSet.url.toString() })
                .hasSize(2)
                .containsOnly("https://zally.example.com/TestRuleSet")
        }

        fun assertRuleManagerUsingAllAnnotatedRules(rulesManager: RulesManager) {
            assertThat(rulesManager.rules.map { it.ruleSet.url.toString() })
                .isNotEmpty
                .contains(
                    "https://zally.example.com/TestRuleSet",
                    "https://zalando.github.io/restful-api-guidelines/",
                    "https://github.com/zalando/zally/blob/master/server/rules.md"
                )
        }
    }

    @Autowired
    lateinit var context: ApplicationContext

    @Bean
    @Primary
    @Profile("all-annotated-rules")
    fun rulesManagerWithAllAnnotatedRules(config: Config): RulesManager = RulesManager
        .fromInstances(
            config,
            context.getBeansWithAnnotation(Rule::class.java).values
        )

    @Bean
    @Primary
    @Profile("limited-rules")
    fun rulesManagerWithLimitedRules(config: Config): RulesManager = RulesManager
        .fromInstances(
            config,
            listOf(
                TestCheckIsOpenApi3(),
                TestCheckAlwaysReport3MustViolations()
            )
        )

    class TestRuleSet : AbstractRuleSet()

    /** Rule used for testing  */
    @Rule(
        ruleSet = TestRuleSet::class,
        id = "TestCheckIsOpenApi3",
        severity = Severity.MUST,
        title = "TestCheckIsOpenApi3"
    )
    class TestCheckIsOpenApi3 {

        @Check(severity = Severity.MUST)
        fun validate(json: JsonNode): Violation? {
            return if ("3.0.0" != json.path("openapi").textValue()) {
                Violation("TestCheckIsOpenApi3", "/openapi".toJsonPointer())
            } else null
        }
    }

    /** Rule used for testing  */
    @Rule(
        ruleSet = TestRuleSet::class,
        id = "TestCheckAlwaysReport3MustViolations",
        severity = Severity.MUST,
        title = "TestCheckAlwaysReport3MustViolations"
    )
    class TestCheckAlwaysReport3MustViolations {

        @Check(severity = Severity.MUST)
        fun validate(@Suppress("UNUSED_PARAMETER") json: JsonNode): Iterable<Violation> {
            return listOf(
                Violation("TestCheckAlwaysReport3MustViolations #1", EMPTY_JSON_POINTER),
                Violation("TestCheckAlwaysReport3MustViolations #2", EMPTY_JSON_POINTER),
                Violation("TestCheckAlwaysReport3MustViolations #3", EMPTY_JSON_POINTER)
            )
        }
    }
}
