package org.zalando.zally.configuration

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

/** Twintip schema discovery document. The snake_case names are part of the protocol. */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class SchemaDiscovery(
    @get:JsonProperty("schema_url") val schemaUrl: String,
    @get:JsonProperty("schema_type") val schemaType: String,
    @get:JsonProperty("ui_url") val uiUrl: String? = null,
)
