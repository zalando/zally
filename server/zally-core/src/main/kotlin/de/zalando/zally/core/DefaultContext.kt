package de.zalando.zally.core

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.core.ast.ReverseAst
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.responses.ApiResponse

class DefaultContext(
    override val source: String,
    openApi: OpenAPI,
    swagger: Swagger? = null
) : Context {

    private val extensionNames = arrayOf("getVendorExtensions", "getExtensions")

    private val openApiAst = ReverseAst.fromObject(openApi).withExtensionMethodNames(*extensionNames).build()
    private val swaggerAst =
        swagger?.let { ReverseAst.fromObject(it).withExtensionMethodNames(*extensionNames).build() }

    override val api = openApi
    override val swagger = swagger
    override fun isOpenAPI3(): Boolean = this.swaggerAst == null

    /**
     * Convenience method for filtering and iterating over the paths in order to create Violations.
     * @param pathFilter a filter selecting the paths to validate
     * @param action the action to perform on filtered items
     * @return a list of Violations and/or nulls where no violations are necessary
     */
    override fun validatePaths(
        pathFilter: (Map.Entry<String, PathItem?>) -> Boolean,
        action: (Map.Entry<String, PathItem?>) -> List<Violation?>
    ): List<Violation> = api.paths
        .orEmpty()
        .filter(pathFilter)
        .flatMap(action)
        .filterNotNull()

    /**
     * Convenience method for filtering and iterating over the operations in order to create Violations.
     * @param pathFilter a filter selecting the paths to validate
     * @param operationFilter a filter selecting the operations to validate
     * @param action the action to perform on filtered items
     * @return a list of Violations and/or nulls where no violations are necessary
     */
    override fun validateOperations(
        pathFilter: (Map.Entry<String, PathItem?>) -> Boolean,
        operationFilter: (Map.Entry<HttpMethod, Operation?>) -> Boolean,
        action: (Map.Entry<HttpMethod, Operation?>) -> List<Violation?>
    ): List<Violation> = validatePaths(pathFilter) { (_, path) ->
        path?.readOperationsMap()
            .orEmpty()
            .filter(operationFilter)
            .flatMap(action)
            .filterNotNull()
    }

    /**
     * Convenience method for filtering and iterating over the responses in order to create Violations.
     * @param pathFilter a filter selecting the paths to validate
     * @param operationFilter a filter selecting the operations to validate
     * @param responseFilter a filter selecting the responses to validate
     * @param action the action to perform on filtered items
     * @return a list of Violations and/or nulls where no violations are necessary
     */
    override fun validateResponses(
        pathFilter: (Map.Entry<String, PathItem?>) -> Boolean,
        operationFilter: (Map.Entry<HttpMethod, Operation?>) -> Boolean,
        responseFilter: (Map.Entry<String, ApiResponse?>) -> Boolean,
        action: (Map.Entry<String, ApiResponse?>) -> List<Violation?>
    ): List<Violation> = validateOperations(pathFilter, operationFilter) { (_, operation) ->
        operation?.responses
            .orEmpty()
            .filter(responseFilter)
            .flatMap(action)
            .filterNotNull()
    }

    /**
     * Creates a List of one Violation with a pointer to the OpenAPI or Swagger model node specified,
     * defaulting to the last recorded location.
     * @param description the description of the Violation
     * @param value the OpenAPI or Swagger model node
     * @return the new Violation
     * @throws IllegalStateException if value is not an OpenAPI or Swagger model element.
     */
    override fun violations(description: String, value: Any): List<Violation> =
        listOf(violation(description, value))

    /**
     * Creates a List of one Violation with the specified pointer, defaulting to the last recorded location.
     * @param description the description of the Violation
     * @param pointer an existing pointer or null
     * @return the new Violation
     */
    override fun violations(description: String, pointer: JsonPointer): List<Violation> =
        listOf(violation(description, pointer))

    /**
     * Creates a Violation with a pointer to the OpenAPI or Swagger model node specified,
     * defaulting to the last recorded location.
     * @param description the description of the Violation
     * @param value the OpenAPI or Swagger model node
     * @return the new Violation
     * @throws IllegalStateException if value is not an OpenAPI or Swagger model element.
     */
    override fun violation(description: String, value: Any): Violation =
        violation(description, getJsonPointer(value))

    /**
     * Creates a Violation with the specified pointer, defaulting to the last recorded location.
     * @param description the description of the Violation
     * @param pointer an existing pointer or null
     * @return the new Violation
     */
    override fun violation(description: String, pointer: JsonPointer): Violation =
        Violation(description, pointer)

    /**
     * Check whether a location should be ignored by a specific rule.
     * @param pointer the location to check
     * @param ruleId the rule id to check
     * @return true if the location should be ignored for this rule
     */
    override fun isIgnored(pointer: JsonPointer, ruleId: String): Boolean =
        swaggerAst?.isIgnored(pointer, ruleId) ?: openApiAst.isIgnored(pointer, ruleId)

    /**
     * Look up the JsonPointer for an OpenAPI or Swagger model element.
     * @return a pointer representing the value.
     * @throws IllegalStateException if value is not an OpenAPI or Swagger model element.
     */
    override fun getJsonPointer(value: Any): JsonPointer = when (swaggerAst) {
        null -> openApiAst
            .getPointer(value)
            ?: error("Expected OpenAPI model element, not: $value")
        else -> when (val swaggerPointer = swaggerAst.getPointer(value)) {
            null -> openApiAst
                .getPointer(value)
                ?.let { it.toSwaggerJsonPointer() ?: it }
                ?: error("Expected OpenAPI or Swagger model element, not: $value")
            else -> swaggerPointer
        }
    }
}
