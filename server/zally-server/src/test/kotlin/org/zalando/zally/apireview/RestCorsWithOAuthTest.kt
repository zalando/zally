package org.zalando.zally.apireview

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * CORS preflight requests must pass the OAuth filter chain without a token, so this runs with the `production` profile.
 * (It used to run with `test`, where security is `permitAll` and the OAuth rules were never exercised.)
 */
class RestCorsWithOAuthTest : ProductionProfileTest() {
    @Test
    fun shouldSupportCorsWhenOAuthIsEnabledOnAllResources() {
        assertPreflightAllowed("/api-violations", "POST")
        assertPreflightAllowed("/supported-rules", "GET")
        assertPreflightAllowed("/review-statistics", "GET")
    }

    @Test
    fun `preflight for a method the resource does not allow is refused`() {
        // Boot 3.5 answered 403; Spring 7 answers 200 without any CORS header, which the browser refuses just the same.
        val response = preflight("/supported-rules", "POST")
        assertThat(response.header("access-control-allow-origin")).isNull()
        assertThat(response.header("access-control-allow-methods")).isNull()
    }

    private fun assertPreflightAllowed(
        path: String,
        method: String,
    ) {
        val response = preflight(path, method)

        assertThat(response.status).isEqualTo(200)
        assertThat(response.header("access-control-allow-origin")).isEqualTo("*")
        assertThat(response.header("access-control-allow-methods")).isEqualTo(method)
    }

    private fun preflight(
        path: String,
        method: String,
    ) = send(
        "OPTIONS",
        path,
        headers = mapOf("Origin" to "https://example.org", "Access-Control-Request-Method" to method),
    )
}
