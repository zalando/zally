package com.corefiling.pdds.zally.extensions

import com.corefiling.pdds.zally.rule.collections.ifNotEmptyLet
import de.zalando.zally.rule.api.Violation
import io.swagger.models.HttpMethod
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter

fun List<String>.toViolation(description: String) =
        this.ifNotEmptyLet { Violation(description, it.map { it.trim() }) }

private fun validate(description: String, toMessages: () -> List<String>) =
        toMessages().toViolation(description)

private fun validatePath(description: String, swagger: Swagger, toMessages: (pattern: String, path: Path) -> List<String>) =
        swagger.paths.orEmpty()
                .flatMap { (pattern, path) ->
                    toMessages(pattern, path).map { "$pattern $it" }
                }.toViolation(description)

private fun validateOperation(description: String, swagger: Swagger, toMessages: (pattern: String, path: Path, method: HttpMethod, operation: Operation) -> List<String>) =
        validatePath(description, swagger) { pattern, path ->
            path.operationMap.orEmpty()
                    .flatMap { (method, operation) ->
                        toMessages(pattern, path, method, operation).map { "$method $it " }
                    }
        }

private fun validateParameter(description: String, swagger: Swagger, toMessages: (pattern: String, path: Path, method: HttpMethod, operation: Operation, parameter: Parameter) -> List<String>) =
        validateOperation(description, swagger) { pattern, path, method, operation ->
            operation.parameters
                    .flatMap { parameter ->
                        toMessages(pattern, path, method, operation, parameter).map { "${parameter.`in`} parameter ${parameter.name} $it" }
                    }
        }

private fun String?.normalizeLocations() = this?.let { listOf(it) }.orEmpty()

fun Swagger.validatePath(description: String, getViolationMessage: (pattern: String, path: Path) -> String?) =
        validatePath(description, this) { pattern, path ->
            getViolationMessage(pattern, path).normalizeLocations()
        }

fun Swagger.validateOperation(description: String, getViolationMessage: (pattern: String, path: Path, method: HttpMethod, operation: Operation) -> String?) =
        validateOperation(description, this) { pattern, path, method, operation ->
            getViolationMessage(pattern, path, method, operation).normalizeLocations()
        }

fun Swagger.validateParameter(description: String, getViolationMessage: (pattern: String, path: Path, method: HttpMethod, operation: Operation, parameter: Parameter) -> String?) =
        validateParameter(description, this) { pattern, path, method, operation, parameter ->
            getViolationMessage(pattern, path, method, operation, parameter).normalizeLocations()
        }
