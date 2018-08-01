package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.ast.JsonPointers
import de.zalando.zally.util.ast.MethodCallRecorder
import de.zalando.zally.util.ast.ReverseAst
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.converter.SwaggerConverter
import io.swagger.v3.parser.core.models.ParseOptions
import io.swagger.v3.parser.util.ResolverFully
import org.slf4j.LoggerFactory

class DefaultContext(openApi: OpenAPI, swagger: Swagger? = null) : Context {
    private val recorder = MethodCallRecorder(openApi).skipMethods(*extensionNames)
    private val openApiAst = ReverseAst.fromObject(openApi).withExtensionMethodNames(*extensionNames).build()
    private val swaggerAst = swagger?.let { ReverseAst.fromObject(it).withExtensionMethodNames(*extensionNames).build() }

    override val api = recorder.proxy

    /**
     * Convenience method for filtering and iterating over the paths in order to create Violations.
     * @param pathFilter a filter selecting the paths to validate
     * @param action the action to perform on filtered items
     * @return a list of Violations and/or nulls where no violations are necessary
     */
    override fun validatePaths(
        pathFilter: (Map.Entry<String, PathItem>) -> Boolean,
        action: (Map.Entry<String, PathItem>) -> List<Violation?>
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
        pathFilter: (Map.Entry<String, PathItem>) -> Boolean,
        operationFilter: (Map.Entry<HttpMethod, Operation>) -> Boolean,
        action: (Map.Entry<HttpMethod, Operation>) -> List<Violation?>
    ): List<Violation> = validatePaths(pathFilter) { (_, path) ->
        path.readOperationsMap()
                .orEmpty()
                .filter(operationFilter)
                .flatMap(action)
                .filterNotNull()
    }

    /**
     * Creates a List of one Violation with a pointer to the OpenAPI or Swagger model node specified,
     * defaulting to the last recorded location.
     * @param description the description of the Violation
     * @param value the OpenAPI or Swagger model node
     * @return the new Violation
     */
    override fun violations(description: String, value: Any): List<Violation> =
        listOf(violation(description, value))

    /**
     * Creates a List of one Violation with the specified pointer, defaulting to the last recorded location.
     * @param description the description of the Violation
     * @param pointer an existing pointer or null
     * @return the new Violation
     */
    override fun violations(description: String, pointer: JsonPointer?): List<Violation> =
        listOf(violation(description, pointer))

    /**
     * Creates a Violation with a pointer to the OpenAPI or Swagger model node specified,
     * defaulting to the last recorded location.
     * @param description the description of the Violation
     * @param value the OpenAPI or Swagger model node
     * @return the new Violation
     */
    override fun violation(description: String, value: Any): Violation =
        violation(description, pointerForValue(value))

    /**
     * Creates a Violation with the specified pointer, defaulting to the last recorded location.
     * @param description the description of the Violation
     * @param pointer an existing pointer or null
     * @return the new Violation
     */
    override fun violation(description: String, pointer: JsonPointer?): Violation =
        Violation(description, pointer ?: recorder.pointer)

    /**
     * Check whether a location should be ignored by a specific rule.
     * @param pointer the location to check
     * @param ruleId the rule id to check
     * @return true if the location should be ignored for this rule
     */
    override fun isIgnored(pointer: JsonPointer, ruleId: String): Boolean =
        swaggerAst?.isIgnored(pointer, ruleId) ?: openApiAst.isIgnored(pointer, ruleId)

    private fun pointerForValue(value: Any): JsonPointer? = if (swaggerAst != null) {
        val swaggerPointer = swaggerAst.getPointer(value)
        if (swaggerPointer != null)
            swaggerPointer
        else {
            // Attempt to convert an OpenAPI pointer to a Swagger pointer.
            val openApiPointer = openApiAst.getPointer(value)
            JsonPointers.convertPointer(openApiPointer) ?: openApiPointer
        }
    } else {
        openApiAst.getPointer(value)
    }

    companion object {
        private val log = LoggerFactory.getLogger(DefaultContext::class.java)
        val extensionNames = arrayOf("getVendorExtensions", "getExtensions")

        fun createOpenApiContext(content: String, failOnParseErrors: Boolean = false): Context? =
            try {
                val parseOptions = ParseOptions()
                parseOptions.isResolve = true
                // parseOptions.isResolveFully = true // https://github.com/swagger-api/swagger-parser/issues/682

                val parseResult = OpenAPIV3Parser().readContents(content, null, parseOptions)
                if (failOnParseErrors && parseResult.messages.orEmpty().isNotEmpty()) {
                    val sep = "\n  - "
                    val messageBulletList = parseResult.messages.joinToString(sep)
                    throw RuntimeException("Swagger parsing failed with those errors:$sep$messageBulletList")
                }
                parseResult?.openAPI?.let {
                    ResolverFully(true).resolveFully(it) // workaround for NPE bug in swagger-parser
                    DefaultContext(it)
                }
            } catch (t: Throwable) {
                null
            }

        fun createSwaggerContext(content: String, failOnParseErrors: Boolean = false): Context? =
            try {
                SwaggerParser().readWithInfo(content, true)?.let { parseResult ->
                    if (failOnParseErrors && parseResult.messages.orEmpty().isNotEmpty()) {
                        val sep = "\n  - "
                        val messageBulletList = parseResult.messages.joinToString(sep)
                        throw RuntimeException("Swagger parsing failed with those errors:$sep$messageBulletList")
                    }
                    val swagger = parseResult.swagger ?: return null
                    val convertResult = SwaggerConverter().convert(parseResult)
                    if (failOnParseErrors && convertResult.messages.orEmpty().isNotEmpty()) {
                        val sep = "\n  - "
                        val messageBulletList = parseResult.messages.joinToString(sep)
                        throw RuntimeException("Swagger conversion to OpenAPI 3 failed with those errors:$sep$messageBulletList")
                    }
                    convertResult?.openAPI?.let {
                        try {
                            ResolverFully(true).resolveFully(it)
                        } catch (e: NullPointerException) {
                            log.warn("Failed to fully resolve Swagger schema.", e)
                            if (failOnParseErrors) throw e
                        }
                        DefaultContext(it, swagger)
                    }
                }
            } catch (t: Throwable) {
                null
            }
    }
}
