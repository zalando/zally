package org.zalando.zally.exception

class MissingApiDefinitionException : RuntimeException(MissingApiDefinitionException.MESSAGE) {
    companion object {
        const val MESSAGE = "An api definition is missing in api_definition field"
    }
}
