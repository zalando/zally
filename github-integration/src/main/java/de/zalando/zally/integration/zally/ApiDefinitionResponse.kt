package de.zalando.zally.integration.zally

data class ApiDefinitionResponse(

        var message: String? = null,
        var violations: List<Violation>? = null,
        var violationsCount: Map<String, Int>? = null

)
