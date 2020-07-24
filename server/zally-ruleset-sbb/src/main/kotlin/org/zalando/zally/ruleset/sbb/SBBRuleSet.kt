package org.zalando.zally.ruleset.sbb

import org.zalando.zally.core.AbstractRuleSet
import org.zalando.zally.rule.api.Rule
import java.net.URI

class SBBRuleSet : AbstractRuleSet() {

    override val url: URI = URI.create("https://schweizerischebundesbahnen.github.io/api-principles/")

    override fun url(rule: Rule): URI = url.resolve("${rule.id}")
}
