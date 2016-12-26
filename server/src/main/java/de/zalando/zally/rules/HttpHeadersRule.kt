package de.zalando.zally.rules

import de.zalando.zally.Violation
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import java.util.*

abstract class HttpHeadersRule : Rule {
    val PARAMETER_NAMES_WHITELIST = setOf("ETag", "TSV", "TE", "Content-MD5", "DNT", "X-ATT-DeviceId", "X-UIDH",
            "X-Request-ID", "X-Correlation-ID", "WWW-Authenticate", "X-XSS-Protection", "X-Flow-ID", "X-UID",
            "X-Tenant-ID", "X-Device-OS")

    abstract fun createViolation(header: String, path: Optional<String>): Violation

    abstract fun isViolation(header: String): Boolean

    override fun validate(swagger: Swagger): List<Violation> {
        val res = ArrayList<Violation>()
        if (swagger.parameters != null) {
            res.addAll(validateParameters(swagger.parameters.values, Optional.empty<String>()))
        }
        if (swagger.paths != null) {
            for ((key, value) in swagger.paths) {
                val pathName = Optional.of(key)
                res.addAll(validateParameters(value.parameters, pathName))
                for (operation in value.operations) {
                    res.addAll(validateParameters(operation.parameters, pathName))
                    res.addAll(validateHeaders(getResponseHeaders(operation.responses), pathName))
                }
            }
        }
        res.addAll(validateHeaders(getResponseHeaders(swagger.responses), Optional.empty<String>()))
        return res
    }

    private fun validateParameters(parameters: Collection<Parameter>?, path: Optional<String>): List<Violation> {
        if (parameters == null) {
            return emptyList()
        }
        return validateHeaders(parameters.filter { it.`in` == "header" }.map { it.getName() }, path)
    }

    private fun validateHeaders(headers: Collection<String>?, path: Optional<String>): List<Violation> {
        return headers
                ?.filter { p -> !PARAMETER_NAMES_WHITELIST.contains(p) && isViolation(p) }
                ?.map { p -> createViolation(p, path) }
                ?: emptyList()
    }

    private fun getResponseHeaders(responses: Map<String, Response>?): Set<String> =
         responses?.values?.flatMap { it.headers?.keys ?: mutableSetOf() }?.toSet() ?: emptySet()

}
