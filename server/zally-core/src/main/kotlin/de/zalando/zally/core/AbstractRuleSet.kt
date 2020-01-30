package de.zalando.zally.core

import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.RuleSet
import java.net.URI

abstract class AbstractRuleSet : RuleSet {

    override val id: String
        get() = javaClass.simpleName

    override val url: URI = URI.create("https://zally.example.com/$id")

    override fun url(rule: Rule): URI = url.resolve("#${rule.id}")

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean = other != null &&
        this.javaClass == other.javaClass &&
        this.id == (other as RuleSet).id

    override fun toString(): String = id
}
