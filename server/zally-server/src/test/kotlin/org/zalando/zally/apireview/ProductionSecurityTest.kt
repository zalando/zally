package org.zalando.zally.apireview

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.zalando.zally.util.StubTokenInfoServer.NOT_FOUND_TOKEN
import org.zalando.zally.util.StubTokenInfoServer.NO_SCOPE_TOKEN
import org.zalando.zally.util.StubTokenInfoServer.OTHER_SCOPE_TOKEN
import org.zalando.zally.util.StubTokenInfoServer.RATE_LIMITED_TOKEN
import org.zalando.zally.util.StubTokenInfoServer.SERVER_ERROR_TOKEN
import org.zalando.zally.util.StubTokenInfoServer.SLOW_TOKEN
import org.zalando.zally.util.StubTokenInfoServer.UID_TOKEN

/**
 * Pins the access rules of `OAuthConfiguration`.
 *
 * These tests were written against Spring Boot 2.7 and carried over to the Spring Security 6 rewrite. Deliberate
 * differences, all announced in the pull request: authentication failures are 401 and refused requests 403 (2.7
 * answered both with 500), trailing slashes are not matched (decision 3), and tokeninfo rejections are `problem+json`.
 */
class ProductionSecurityTest : ProductionProfileTest() {
    private val unknownReview = "/api-violations/6f1f1b1e-9a38-4c7e-8d0e-111111111111"

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/supported-rules", "/supported-rules?type=MUST",
            "/review-statistics", "/review-statistics?from=2020-01-01",
        ],
    )
    fun `token with uid scope may read the protected resources`(path: String) {
        assertThat(get(path, UID_TOKEN).status).isEqualTo(200)
    }

    @Test
    fun `token with uid scope may post and read api violations`() {
        val created = send("POST", "/api-violations", UID_TOKEN, body = """{"api_definition_string":"{}"}""")
        assertThat(created.status).isEqualTo(200)

        assertThat(get(unknownReview, UID_TOKEN).status).isEqualTo(404)
        // a trailing slash is no longer matched (Spring 6), so it is not a known resource
        assertThat(get("/api-violations/", UID_TOKEN).status).isEqualTo(404)
    }

    @Test
    fun `request without token is unauthorized`() {
        // Spring Boot 2.7 answered 500 here; an authentication failure is a 401.
        val response = get("/supported-rules")

        assertThat(response.status).isEqualTo(401)
        assertThat(response.contentType).isEqualTo("application/problem+json")
        // Spring Security 7 adds resource_metadata="..." to the challenge
        assertThat(response.header("www-authenticate")).startsWith("Bearer")
        assertThat(response.body).isEqualTo(
            """{"title":"Unauthorized","status":401,"detail":"Full authentication is required to access this resource"}""",
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["/supported-rules/", "/review-statistics/", "/api-violations/6f1f1b1e-9a38-4c7e-8d0e-111111111111/"])
    fun `trailing slashes are not matched and so denied`(path: String) {
        // Boot 2.7 matched them (regex `/?`) and served 200. Spring 6 does not match trailing slashes any more (decision 3).
        val response = get(path, UID_TOKEN)

        assertThat(response.status).isEqualTo(403)
        assertThat(response.body).isEqualTo("""{"title":"Forbidden","status":403,"detail":"Access Denied"}""")
    }

    @ParameterizedTest
    @ValueSource(strings = [OTHER_SCOPE_TOKEN, NO_SCOPE_TOKEN])
    fun `token without uid scope is rejected as unauthorized`(token: String) {
        val response = get("/supported-rules", token)

        assertThat(response.status).isEqualTo(401)
        assertThat(response.header("www-authenticate")).contains("invalid_token", "No 'uid'-scope found in access-token!")
        assertThat(response.contentType).isEqualTo("application/problem+json")
    }

    @Test
    fun `token rejected by the tokeninfo endpoint is unauthorized`() {
        val response = get("/supported-rules", "revoked-token")

        assertThat(response.status).isEqualTo(401)
        assertThat(response.header("www-authenticate")).startsWith("""Bearer error="invalid_token"""")
        assertThat(response.contentType).isEqualTo("application/problem+json")
        assertThat(response.body).startsWith("""{"title":"Unauthorized","status":401,"detail":"""")
    }

    @Test
    fun `rejection by the tokeninfo endpoint does not leak its wording`() {
        val response = get("/supported-rules", "revoked-token")

        assertThat(response.status).isEqualTo(401)
        assertThat(response.body).isEqualTo(
            """{"title":"Unauthorized","status":401,"detail":"Full authentication is required to access this resource"}""",
        )
    }

    @ParameterizedTest
    @ValueSource(strings = [SERVER_ERROR_TOKEN, NOT_FOUND_TOKEN, RATE_LIMITED_TOKEN])
    fun `a failing tokeninfo endpoint is service unavailable, not an invalid token`(token: String) {
        // 5xx, a wrong URL (404) or rate limiting (429) say nothing about the token, so the caller must not get a 401.
        val response = get("/supported-rules", token)

        assertThat(response.status).isEqualTo(503)
        assertThat(response.contentType).isEqualTo("application/problem+json")
        assertThat(response.header("www-authenticate")).isNull()
        assertThat(response.body).isEqualTo(
            """{"title":"Service Unavailable","status":503,"detail":"Token validation is temporarily unavailable"}""",
        )
    }

    @Test
    fun `a tokeninfo endpoint that does not answer in time is service unavailable`() {
        val response = get("/supported-rules", SLOW_TOKEN)

        assertThat(response.status).isEqualTo(503)
    }

    @ParameterizedTest
    @ValueSource(strings = ["/api", "/.well-known/schema-discovery", "/unknown", "/supported-rules.yaml"])
    fun `everything else is denied even with a valid token`(path: String) {
        val response = get(path, UID_TOKEN)

        // Boot 2.7 answered 500 "Access is denied"; a refused request is a 403.
        assertThat(response.status).isEqualTo(403)
        assertThat(response.contentType).isEqualTo("application/problem+json")
        assertThat(response.body).isEqualTo("""{"title":"Forbidden","status":403,"detail":"Access Denied"}""")
    }

    @ParameterizedTest
    @ValueSource(strings = ["/health", "/metrics"])
    fun `actuator paths are not served on the main port`(path: String) {
        // Security lets both through (/health is public, /metrics needs the uid scope), then MVC has no handler for them.
        val response = get(path, UID_TOKEN)

        assertThat(response.status).isEqualTo(404)
        assertThat(response.contentType).isEqualTo("application/json")
    }

    @Test
    fun `options requests are public`() {
        assertThat(send("OPTIONS", "/api-violations").status).isEqualTo(200)
        assertThat(send("OPTIONS", "/api").status).isEqualTo(200)
        // allowed by security, then not found by MVC
        assertThat(send("OPTIONS", "/unknown").status).isEqualTo(404)
    }

    @ParameterizedTest
    @ValueSource(strings = ["/health"])
    fun `health on the management port is public`(path: String) {
        val response = managementGet(path)

        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).isEqualTo("""{"status":"UP"}""")
    }

    @Test
    fun `health with a trailing slash is not public`() {
        // Boot 2.7 allowed `/health/`; the docker-compose health check uses `/health`.
        assertThat(managementGet("/health/").status).isEqualTo(401)
    }

    @ParameterizedTest
    @ValueSource(strings = ["/metrics"])
    fun `metrics on the management port require the uid scope`(path: String) {
        assertThat(managementGet(path).status).isEqualTo(401)
        assertThat(managementGet(path, OTHER_SCOPE_TOKEN).status).isEqualTo(401)

        val response = managementGet(path, UID_TOKEN)
        assertThat(response.status).isEqualTo(200)
        assertThat(response.body).contains("\"names\"")
    }

    @ParameterizedTest
    @ValueSource(strings = ["/metrics/jvm.memory.used", "/info", "/env", "/prometheus"])
    fun `other actuator paths are denied`(path: String) {
        assertThat(managementGet(path, UID_TOKEN).status).isEqualTo(403)
    }
}
