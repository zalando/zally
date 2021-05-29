package org.zalando.zally.ruleset.zalando.model

enum class ApiAudience(val code: String) {
    EXTERNAL_PUBLIC("external-public"),
    EXTERNAL_PARTNER("external-partner"),
    COMPANY_INTERNAL("company-internal"),
    BUSINESS_UNIT_INTERNAL("business-unit-internal"),
    COMPONENT_INTERNAL("component-internal");

    companion object {
        fun parse(code: String): ApiAudience =
            values().find { it.code == code }
                ?: throw UnsupportedAudienceException(code)
    }
}

class UnsupportedAudienceException(val code: String) : Exception("API audience $code is not supported.")
