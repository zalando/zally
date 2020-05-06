package de.zalando.zally.rule

import de.zalando.zally.apireview.RestApiTestConfiguration.Companion.assertRuleManagerUsingAllAnnotatedRules
import de.zalando.zally.core.RulesManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
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
