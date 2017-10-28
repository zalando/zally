package de.zalando.zally.rule

import org.springframework.stereotype.Component

@Component
class ZallyRuleSet : RuleSet {
    override val id = javaClass.simpleName
    override val title = "Additional Zally Swagger Rules"
    override val url = "https://github.com/zalando-incubator/zally"
}