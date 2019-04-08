package de.zalando.zally.rule.api

import com.fasterxml.jackson.core.JsonPointer
import io.swagger.models.Swagger
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.responses.ApiResponse

interface Context {

    /**
     * Parsed OpenAPI 3 object represents either the OpenAPI 3 specification or converted OpenAPI 2 (Swagger) spec.
     */
    val api: OpenAPI

    /**
     * Parsed Swagger object represents the original OpenAPI 2 (Swagger) spec, or null if original is OpenAPI 3.
     */
    val swagger: Swagger?

    /**
     * @return `true` if the source is a OpenAPI 3 specification.
     */
    fun isOpenAPI3(): Boolean

    /**
     * Original API specification content as `String`.
     */
    val source: String

    /**
     * Convenience method for filtering and iterating over the paths in order to create Violations.
     * @param pathFilter a filter selecting the paths to validate
     * @param action the action to perform on filtered items
     * @return a list of Violations and/or nulls where no violations are necessary
     */
    fun validatePaths(
        pathFilter: (Map.Entry<String, PathItem?>) -> Boolean = { true },
        action: (Map.Entry<String, PathItem?>) -> List<Violation?>
    ): List<Violation>

    /**
     * Convenience method for filtering and iterating over the operations in order to create Violations.
     * @param pathFilter a filter selecting the paths to validate
     * @param operationFilter a filter selecting the operations to validate
     * @param action the action to perform on filtered items
     * @return a list of Violations and/or nulls where no violations are necessary
     */
    fun validateOperations(
        pathFilter: (Map.Entry<String, PathItem?>) -> Boolean = { true },
        operationFilter: (Map.Entry<PathItem.HttpMethod, Operation?>) -> Boolean = { true },
        action: (Map.Entry<PathItem.HttpMethod, Operation?>) -> List<Violation?>
    ): List<Violation>

    /**
     * Convenience method for filtering and iterating over the responses in order to create Violations.
     * @param pathFilter a filter selecting the paths to validate
     * @param operationFilter a filter selecting the operations to validate
     * @param responseFilter a filter selecting the responses to validate
     * @param action the action to perform on filtered items
     * @return a list of Violations and/or nulls where no violations are necessary
     */
    fun validateResponses(
        pathFilter: (Map.Entry<String, PathItem?>) -> Boolean = { true },
        operationFilter: (Map.Entry<PathItem.HttpMethod, Operation?>) -> Boolean = { true },
        responseFilter: (Map.Entry<String, ApiResponse?>) -> Boolean = { true },
        action: (Map.Entry<String, ApiResponse?>) -> List<Violation?>
    ): List<Violation>

    /**
     * Creates a List of one Violation with a pointer to the OpenAPI or Swagger model node specified,
     * defaulting to the last recorded location.
     * @param description the description of the Violation
     * @param value the OpenAPI or Swagger model node
     * @return the new Violation
     */
    fun violations(description: String, value: Any?): List<Violation>

    /**
     * Creates a List of one Violation with the specified pointer, defaulting to the last recorded location.
     * @param description the description of the Violation
     * @param pointer an existing pointer or null
     * @return the new Violation
     */
    fun violations(description: String, pointer: JsonPointer?): List<Violation>

    /**
     * Creates a Violation with a pointer to the OpenAPI or Swagger model node specified,
     * defaulting to the last recorded location.
     * @param description the description of the Violation
     * @param value the OpenAPI or Swagger model node
     * @return the new Violation
     */
    fun violation(description: String, value: Any?): Violation

    /**
     * Creates a Violation with the specified pointer, defaulting to the last recorded location.
     * @param description the description of the Violation
     * @param pointer an existing pointer or null
     * @return the new Violation
     */
    fun violation(description: String, pointer: JsonPointer?): Violation

    /**
     * Check whether a location should be ignored by a specific rule.
     * @param pointer the location to check
     * @param ruleId the rule id to check
     * @return true if the location should be ignored for this rule
     */
    fun isIgnored(pointer: JsonPointer, ruleId: String): Boolean
}
