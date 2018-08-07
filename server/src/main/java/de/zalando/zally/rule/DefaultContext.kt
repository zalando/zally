package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.ast.JsonPointers
import de.zalando.zally.util.ast.MethodCallRecorder
import de.zalando.zally.util.ast.ReverseAst
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import io.swagger.parser.util.SwaggerDeserializationResult
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.converter.SwaggerConverter
import io.swagger.v3.parser.core.models.ParseOptions
import io.swagger.v3.parser.core.models.SwaggerParseResult
import io.swagger.v3.parser.util.ResolverFully
import org.slf4j.LoggerFactory

class DefaultContext(override val source: String, openApi: OpenAPI, swagger: Swagger? = null) : Context {
    private val recorder = MethodCallRecorder(openApi).skipMethods(*extensionNames)
    private val openApiAst = ReverseAst.fromObject(openApi).withExtensionMethodNames(*extensionNames).build()
    private val swaggerAst = swagger?.let { ReverseAst.fromObject(it).withExtensionMethodNames(*extensionNames).build() }

    override val api = recorder.proxy
    override fun isOpenAPI3(): Boolean = this.swaggerAst == null

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
        private val extensionNames = arrayOf("getVendorExtensions", "getExtensions")

        fun createOpenApiContext(content: String): ContentParseResult<Context> {
            val parseOptions = ParseOptions()
            parseOptions.isResolve = true
            // parseOptions.isResolveFully = true // https://github.com/swagger-api/swagger-parser/issues/682
            val parseResult = OpenAPIV3Parser().readContents(content, null, parseOptions)
            if (parseResult.messages.isNotEmpty()) {
                return if (parseResult.messages.contains("attribute openapi is missing")) {
                    ContentParseResult.NotApplicable()
                } else {
                    ContentParseResult.ParsedWithErrors(parseResult.messages)
                }
            }

            val context = parseResult.openAPI.let {
                ResolverFully(true).resolveFully(it) // workaround for NPE bug in swagger-parser
                DefaultContext(content, it)
            }
            return ContentParseResult.Success(context)
        }

        fun createSwaggerContext(content: String): ContentParseResult<Context> {
            val parseResult = SwaggerParser().readWithInfo(content, true)
            if (parseResult.messages.isNotEmpty()) {
                return if (parseResult.messages.contains("attribute swagger is missing")) {
                    ContentParseResult.NotApplicable()
                } else {
                    ContentParseResult.ParsedWithErrors(parseResult.messages)
                }
            }

            val convertResult = convertWithPreChecks(parseResult)
            if (convertResult.openAPI === null) {
                return ContentParseResult.ParsedWithErrors(convertResult.messages)
            }

            ResolverFully(true).resolveFully(convertResult.openAPI)
            val context = DefaultContext(content, convertResult.openAPI, parseResult.swagger)
            return ContentParseResult.Success(context)
        }

        /**
         * @throws PreCheckViolationsException when one of the check fails, including the appropriate list of [Violation].
         */
        private fun convertWithPreChecks(swaggerDeserializationResult: SwaggerDeserializationResult): SwaggerParseResult {
            val swagger = swaggerDeserializationResult.swagger
            val violations = mutableListOf<Violation>()

//            if (swagger.info === null) {
//                violations += Violation("""An "info" block must be specified.""", rootJsonPointer)
//            }
            // todo #773

            if (violations.isNotEmpty()) {
                throw PreCheckViolationsException(violations)
            }

            return try {
                SwaggerConverter().convert(swaggerDeserializationResult)
            } catch (t: Throwable) {
                log.warn("Unable to convert specification from 'Swagger 2' to 'OpenAPI 3'. Error not covered by pre-checks.", t)
                val violation = Violation("Unable to parse specification", rootJsonPointer)
                throw PreCheckViolationsException(listOf(violation))
            }
        }

        private val rootJsonPointer = JsonPointer.compile("/")
    }
}
