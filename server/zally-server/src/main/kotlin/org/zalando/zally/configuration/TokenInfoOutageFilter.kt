package org.zalando.zally.configuration

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Answers 503 when the tokeninfo endpoint could not be asked.
 *
 * `OpaqueTokenAuthenticationProvider` wraps an introspection failure (5xx, timeout, wrong URL, rate limit) in an
 * [AuthenticationServiceException], and `BearerTokenAuthenticationFilter` rethrows that instead of handing it to the
 * entry point, which would leave the caller with a bare 500. Registered in front of that filter.
 */
class TokenInfoOutageFilter(
    private val problems: ProblemSecurityHandlers,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (e: AuthenticationServiceException) {
            logger.warn("Token validation failed because the tokeninfo endpoint could not be used", e)
            problems.serviceUnavailable(response)
        }
    }
}
