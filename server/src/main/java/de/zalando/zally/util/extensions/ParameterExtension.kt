package de.zalando.zally.util.extensions

import io.swagger.v3.oas.models.parameters.Parameter

/**
 * Checks whether parameter is query parameter.
 */
fun Parameter.isQuery(): Boolean = `in` == "query"