package de.zalando.zally.core

interface ApiValidator {
    fun validate(content: String, policy: RulesPolicy, authorization: String? = null): List<Result>
}
