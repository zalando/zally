package de.zalando.zally.dto

class ApiDefinitionWrapper(
        val apiDefinition: String,
        val lineResolver: LocationResolver
) {
    constructor(apiDefinition: String) : this(apiDefinition, LocationResolver.Empty)

    override fun toString(): String {
        return apiDefinition
    }
}