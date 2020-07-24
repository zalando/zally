package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.AbstractRuleSet
import java.net.URI

class ZalandoRuleSet : AbstractRuleSet() {

    override val url: URI = URI.create("https://zalando.github.io/restful-api-guidelines/")
}
