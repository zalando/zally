package de.zalando.zally

import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.RuleSet
import org.springframework.stereotype.Component
import java.net.URI

@Component
class TestRuleSet : RuleSet {
    override val id = javaClass.simpleName
    override val title = "Zally Test Rules"
    override val url = URI.create("https://test.example.com")
    override fun url(rule: Rule): URI {
        val ref = "${rule.id}: ${rule.title}"
                .toLowerCase()
                .replace(Regex("[^a-z0-9]+"), "-")
        return url.resolve("#$ref")
    }
}