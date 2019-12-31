package de.zalando.zally.rule

import de.zalando.zally.core.Result
import de.zalando.zally.core.RulesPolicy

interface ApiValidator {
    fun validate(content: String, policy: RulesPolicy, authorization: String? = null): List<Result>
}
