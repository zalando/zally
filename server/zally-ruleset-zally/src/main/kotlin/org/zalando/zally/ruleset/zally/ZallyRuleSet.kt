package org.zalando.zally.ruleset.zally

import org.zalando.zally.core.AbstractRuleSet
import org.zalando.zally.rule.api.Rule
import java.net.URI

class ZallyRuleSet : AbstractRuleSet() {
    override val url: URI = URI.create("https://github.com/zalando/zally/blob/master/server/rules.md")

    override fun url(rule: Rule): URI {
        val heading = "${rule.id}: ${rule.title}"
        val ref = heading
            .toLowerCase()
            .replace(Regex("[^a-z0-9]+"), "-")
        return url.resolve("#$ref")
    }
}
