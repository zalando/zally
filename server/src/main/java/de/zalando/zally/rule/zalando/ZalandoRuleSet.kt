package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.RuleSet
import org.springframework.stereotype.Component
import java.net.URI

@Component
class ZalandoRuleSet : RuleSet {
    override val id = javaClass.simpleName
    override val title = "Zalando RESTful API and Event Scheme Guidelines"
    override val url = URI.create("https://zalando.github.io/restful-api-guidelines/")
    override fun url(rule: Rule): URI {
        return url.resolve("#" + rule.id)
    }
}
