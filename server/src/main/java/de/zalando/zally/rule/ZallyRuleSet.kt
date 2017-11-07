package de.zalando.zally.rule

import org.springframework.stereotype.Component
import java.net.URI

@Component
class ZallyRuleSet : RuleSet {
    override val id = javaClass.simpleName
    override val title = "Additional Zally Swagger Rules"
    override val url = URI.create("https://github.com/zalando-incubator/zally")
}