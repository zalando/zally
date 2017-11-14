package com.corefiling.zally.rule

import de.zalando.zally.rule.api.RuleSet
import org.springframework.stereotype.Component
import java.net.URI

@Component
class CoreFilingRuleSet : RuleSet {
    override val id = javaClass.simpleName
    override val title = "CoreFiling API Guidelines"
    override val url = URI.create("https://wiki.int.corefiling.com/platform/APIGuidelines")
}