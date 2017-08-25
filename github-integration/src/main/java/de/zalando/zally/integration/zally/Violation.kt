package de.zalando.zally.integration.zally

data class Violation(

        var title: String? = null,
        var description: String? = null,
        var violationType: ViolationType? = null,
        var ruleLink: String? = null,
        var paths: List<String>? = null

)
