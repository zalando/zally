package de.zalando.zally.rule

import org.springframework.stereotype.Component

@Component
class ZalandoRuleSet : RuleSet {
    override val id = javaClass.simpleName
    override val title = "Zalando RESTful API and Event Scheme Guidelines"
    override val url = "https://zalando.github.io/restful-api-guidelines/"
}
