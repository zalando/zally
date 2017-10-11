package de.zalando.zally.dto

import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import de.zalando.zally.util.ApiDefinitionDeserializer

data class ApiDefinitionRequest(

        @JsonRawValue
        @JsonDeserialize(using = ApiDefinitionDeserializer::class)
        var apiDefinition: ApiDefinitionWrapper? = null,

        var apiDefinitionUrl: String? = null,

        var ignoreRules: List<String>? = emptyList()
) {
    /** for java invocations: it doesn't have overloaded constructors */
    companion object Factory {

        fun fromJson(json: String) = ApiDefinitionRequest(ApiDefinitionWrapper(json))

        fun fromUrl(url: String) = ApiDefinitionRequest(apiDefinitionUrl = url)
    }
}
