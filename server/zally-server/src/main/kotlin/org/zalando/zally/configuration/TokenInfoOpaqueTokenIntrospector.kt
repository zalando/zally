package org.zalando.zally.configuration

import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.time.Duration

/** The tokeninfo endpoint could not tell whether the token is valid. Answered with 503, not with 401. */
class TokenInfoUnavailableException(
    cause: Throwable,
) : OAuth2IntrospectionException("Unable to get authorisation from tokeninfo", cause)

/**
 * Validates a bearer token with a Zalando style tokeninfo endpoint: `GET <uri>` with the token in the `Authorization`
 * header answers a JSON object with `scope` (list), `uid`, `access_token`, `expires_in` and others.
 *
 * Replaces `stups-spring-oauth2-server`, which is built on the removed Spring Security OAuth 2 and cannot run on
 * Spring Security 6. Like that library, a token that carries a `uid` also gets the `uid` scope.
 */
class TokenInfoOpaqueTokenIntrospector(
    private val tokenInfoUri: String,
    builder: RestClient.Builder,
) : OpaqueTokenIntrospector {
    private val restClient: RestClient =
        builder
            .requestFactory(
                SimpleClientHttpRequestFactory().apply {
                    setConnectTimeout(Duration.ofSeconds(2))
                    setReadTimeout(Duration.ofSeconds(5))
                },
            ).build()

    override fun introspect(token: String): OAuth2AuthenticatedPrincipal {
        val attributes = fetch(token)

        val uid = (attributes["uid"] as? String)?.takeIf { it.isNotBlank() }
        val scopes = linkedSetOf<String>()
        (attributes["scope"] as? Collection<*>)?.mapNotNullTo(scopes) { it as? String }
        uid?.let { scopes += UID_SCOPE }
        if (UID_SCOPE !in scopes) {
            throw BadOpaqueTokenException("No 'uid'-scope found in access-token!")
        }

        return DefaultOAuth2AuthenticatedPrincipal(
            uid ?: "N/A",
            attributes,
            scopes.map { SimpleGrantedAuthority("SCOPE_$it") },
        )
    }

    private fun fetch(token: String): Map<String, Any> =
        try {
            restClient
                .get()
                .uri(tokenInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(object : ParameterizedTypeReference<Map<String, Any>>() {})
                ?.takeIf { it.isNotEmpty() }
                ?: throw BadOpaqueTokenException("Empty tokeninfo response")
        } catch (e: HttpClientErrorException) {
            if (e.statusCode.value() in REJECTED_TOKEN_STATUSES) {
                // the endpoint rejected the token itself
                throw BadOpaqueTokenException("invalid_token", e)
            }
            // 404, 429 and the like: the endpoint is misconfigured or limiting us, the token may well be fine
            throw TokenInfoUnavailableException(e)
        } catch (e: RestClientException) {
            // 5xx, timeouts, connection errors
            throw TokenInfoUnavailableException(e)
        }

    companion object {
        private const val UID_SCOPE = "uid"
        private val REJECTED_TOKEN_STATUSES = setOf(400, 401, 403)
    }
}
