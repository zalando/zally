package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.AbstractRuleSet
import java.net.URI

class ZalandoRuleSet : AbstractRuleSet() {

    override val url: URI = URI.create("https://zalando.github.io/restful-api-guidelines/")
}
