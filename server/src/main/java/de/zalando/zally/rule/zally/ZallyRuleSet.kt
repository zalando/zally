package de.zalando.zally.rule.zally

import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.RuleSet
import org.springframework.stereotype.Component
import java.net.URI

@Component
class ZallyRuleSet : RuleSet {
    override val id = javaClass.simpleName
    override val title = "Additional Zally Swagger Rules"
    override val url = URI.create("https://github.com/zalando-incubator/zally/blob/master/server/rules.md")
    override fun url(rule: Rule): URI {
        val ref = "${rule.id}: ${rule.title}"
                .toLowerCase()
                .replace(Regex("[^a-z0-9]+"), "-")
        return url.resolve("#$ref")
    }
}