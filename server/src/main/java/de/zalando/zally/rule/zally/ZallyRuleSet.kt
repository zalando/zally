package de.zalando.zally.rule.zally

import de.zalando.zally.rule.api.RuleSet
import org.springframework.stereotype.Component
import java.net.URI

@Component
class ZallyRuleSet : de.zalando.zally.rule.api.RuleSet {
    override val id = javaClass.simpleName
    override val title = "Additional Zally Swagger Rules"
    override val url = URI.create("https://github.com/zalando-incubator/zally")
}