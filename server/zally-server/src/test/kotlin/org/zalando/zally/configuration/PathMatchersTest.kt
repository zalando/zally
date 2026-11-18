package org.zalando.zally.configuration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher

/**
 * This serves as a reminder/documentation of how the path matchers used by `OAuthConfiguration` behave.
 *
 * Since Spring Security 6 `requestMatchers("/path")` matches by path pattern: a trailing slash is a different path,
 * and a query string is not part of the path. On Spring Boot 2.7 the configuration needed regular expressions like
 * `/metrics/?(\?.*)?` to get the same effect for the slash.
 */
class PathMatchersTest {
    @Test
    fun `pattern without trailing slash matches just requests without trailing slash`() {
        val matcher = PathPatternRequestMatcher.withDefaults().matcher("/metrics")
        assertThat(matcher.matches(request("/metrics"))).isTrue()
        assertThat(matcher.matches(request("/metrics/"))).isFalse()
        assertThat(matcher.matches(request("/metrics/further"))).isFalse()
    }

    @Test
    fun `pattern ignores the query string`() {
        val matcher = PathPatternRequestMatcher.withDefaults().matcher("/metrics")
        assertThat(matcher.matches(request("/metrics", "parameter=value"))).isTrue()
        assertThat(matcher.matches(request("/metrics-resource", "parameter=value"))).isFalse()
    }

    @Test
    fun `single wildcard matches exactly one path segment`() {
        val matcher = PathPatternRequestMatcher.withDefaults().matcher("/api-violations/*")
        assertThat(matcher.matches(request("/api-violations/dcb5a97e-3586-44fc-b40c-90e209cf2e73"))).isTrue()
        assertThat(matcher.matches(request("/api-violations"))).isFalse()
        assertThat(matcher.matches(request("/api-violations/dcb5a97e-3586-44fc-b40c-90e209cf2e73/"))).isFalse()
        assertThat(matcher.matches(request("/api-violations/a/b"))).isFalse()
        assertThat(matcher.matches(request("/api-violations-resource/x"))).isFalse()
    }

    private fun request(
        path: String,
        query: String? = null,
    ) = MockHttpServletRequest("GET", path).apply { queryString = query }
}
