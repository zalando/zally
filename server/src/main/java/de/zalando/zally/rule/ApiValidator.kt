package de.zalando.zally.rule

interface ApiValidator {
    fun validate(content: String, requestPolicy: RulesPolicy): List<Result>
}
