package de.zalando.zally.rule.zally

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.*
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.Parameter
import io.swagger.models.properties.*

@Rule(
        ruleSet = ZallyRuleSet::class,
        id = "S005",
        severity = Severity.SHOULD,
        title = "Do not leave unused definitions"
)
class NoUnusedDefinitionsRule {

    @Check(severity = Severity.SHOULD)
    fun validate(adapter: ApiAdapter): Violation? {
        if (adapter.isV2()) {
            return validateV2(adapter.swagger!!)
        }
        return null
    }

    private fun validateV2(swagger: Swagger): Violation? {
        val paramsInPaths = swagger.paths.orEmpty().values.flatMap { path ->
            path.operations.orEmpty().flatMap { operation ->
                operation.parameters.orEmpty()
            }
        }.toSet()

        val refsInPaths = swagger.paths.orEmpty().values.flatMap { path ->
            path.operations.orEmpty().flatMap { operation ->
                val inParams = operation.parameters.orEmpty().flatMap(this::findAllRefs)
                val inResponse = operation.responses.orEmpty().values.flatMap(this::findAllRefs)
                inParams + inResponse
            }
        }
        val refsInDefs = swagger.definitions.orEmpty().values.flatMap(this::findAllRefs)
        val allRefs = (refsInPaths + refsInDefs).toSet()

        val unusedParams = swagger.parameters.orEmpty().filterValues { it !in paramsInPaths }.keys.map { "#/parameters/$it" }
        val unusedDefs = swagger.definitions.orEmpty().keys.filter { it !in allRefs }.map { "#/definitions/$it" }

        val paths = unusedParams + unusedDefs

        return if (paths.isNotEmpty()) {
            Violation("Found ${paths.size} unused definitions", paths)
        } else null
    }

    fun findAllRefs(param: Parameter?): List<String> =
        if (param is BodyParameter) findAllRefs(param.schema) else emptyList()

    fun findAllRefs(response: Response?): List<String> =
        if (response?.schema != null) findAllRefs(response.schema) else emptyList()

    fun findAllRefs(model: Model?): List<String> =
        when (model) {
            is RefModel -> listOf(model.simpleRef)
            is ArrayModel -> findAllRefs(model.items)
            is ModelImpl ->
                model.properties.orEmpty().values.flatMap(this::findAllRefs) +
                    findAllRefs(model.additionalProperties)
            is ComposedModel ->
                model.allOf.orEmpty().flatMap(this::findAllRefs) +
                    model.interfaces.orEmpty().flatMap(this::findAllRefs) +
                    findAllRefs(model.parent) +
                    findAllRefs(model.child)
            else -> emptyList()
        }

    fun findAllRefs(prop: Property?): List<String> =
        when (prop) {
            is RefProperty -> listOf(prop.simpleRef)
            is ArrayProperty -> findAllRefs(prop.items)
            is MapProperty -> findAllRefs(prop.additionalProperties)
            is ObjectProperty -> prop.properties.orEmpty().values.flatMap(this::findAllRefs)
            else -> emptyList()
        }
}
