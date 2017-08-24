package de.zalando.zally.integration.zally

import com.fasterxml.jackson.annotation.JsonProperty

class Configuration {
    @JsonProperty("api_definitions")
    lateinit var apiDefinitions: List<String>
}
