package org.zalando.zally.rule

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.zalando.zally.apireview.RestApiTestConfiguration.Companion.assertRuleManagerUsingAllAnnotatedRules
import org.zalando.zally.core.RulesManager

@SpringBootTest
@ActiveProfiles("test", "all-annotated-rules")
class RuleUniquenessTest {

    @Autowired
    private lateinit var rules: RulesManager

    @Test
    fun `correct rules are under test`() = assertRuleManagerUsingAllAnnotatedRules(rules)

    @Test
    fun ruleIdsShouldBeUnique() {
        val duplicatedCodes = rules.rules
            .filterNot { it.rule.title == "TestUseOpenApiRule" }
            .groupBy { it.rule.id }
            .filterValues { it.size > 1 }

        assertThat(duplicatedCodes)
            .hasToString("{}")
    }

    @Test
    fun ruleTitlesShouldBeUnique() {
        val duplicatedCodes = rules.rules
            .filterNot { it.rule.title == "TestUseOpenApiRule" }
            .groupBy { it.rule.title }
            .filterValues { it.size > 1 }

        assertThat(duplicatedCodes)
            .hasToString("{}")
    }
}
