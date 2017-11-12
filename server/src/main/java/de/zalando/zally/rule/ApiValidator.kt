package de.zalando.zally.rule

interface ApiValidator {
    fun validate(swaggerContent: String, requestPolicy: RulesPolicy): List<Violation>
}
