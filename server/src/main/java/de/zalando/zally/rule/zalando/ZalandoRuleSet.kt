package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.AbstractRuleSet
import de.zalando.zally.rule.api.Rule
import org.springframework.stereotype.Component
import java.net.URI

@Component
class ZalandoRuleSet : AbstractRuleSet() {
    override val id: String = javaClass.simpleName
    override val title: String = "Zalando RESTful API and Event Scheme Guidelines"
    override val url: URI = URI.create("https://zalando.github.io/restful-api-guidelines/")
    override fun url(rule: Rule): URI {
        return url.resolve("#${rule.id}")
    }
}
