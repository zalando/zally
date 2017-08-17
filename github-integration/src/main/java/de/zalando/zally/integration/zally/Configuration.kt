package de.zalando.zally.integration.zally

import com.fasterxml.jackson.annotation.JsonProperty

class Configuration {
    @JsonProperty("swagger_path")
    lateinit var swaggerPath: String

    @JsonProperty("ignored_rules")
    lateinit var ignoredRules: List<String>
}
