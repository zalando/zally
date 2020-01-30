package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.AbstractRuleSet
import java.net.URI

class ZalandoRuleSet : AbstractRuleSet() {
    override val title: String = "Zalando RESTful API and Event Scheme Guidelines"

    override val url: URI = URI.create("https://zalando.github.io/restful-api-guidelines/")
}
