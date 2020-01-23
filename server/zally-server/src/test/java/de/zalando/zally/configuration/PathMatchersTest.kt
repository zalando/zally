package de.zalando.zally.configuration

import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.RegexRequestMatcher
import javax.servlet.http.HttpServletRequest

/**
 * This serves as a reminder/documentation of how the matchers do work.
 */
class PathMatchersTest {

    @Test
    fun `antMatcher without trailing slash matches just requests without trailing slash`() {
        val matcher = AntPathRequestMatcher("/metrics")
        assertThat(matcher.matches(request("/metrics"))).isTrue()
        assertThat(matcher.matches(request("/metrics/"))).isFalse()
        assertThat(matcher.matches(request("/metrics/further"))).isFalse()
    }

    @Test
    fun `antMatcher with trailing slash matches just requests with trailing slash`() {
        val matcher = AntPathRequestMatcher("/metrics/")
        assertThat(matcher.matches(request("/metrics"))).isFalse()
        assertThat(matcher.matches(request("/metrics/"))).isTrue()
        assertThat(matcher.matches(request("/metrics/further"))).isFalse()
    }

    @Test
    fun `regex matcher matches requests with and without trailing slash`() {
        val matcher = RegexRequestMatcher("/metrics/?", null)
        assertThat(matcher.matches(request("/metrics"))).isTrue()
        assertThat(matcher.matches(request("/metrics/"))).isTrue()
        assertThat(matcher.matches(request("/metrics/further"))).isFalse()
    }

    @Test
    fun `regex matcher matches allows query parameters`() {
        val matcher = RegexRequestMatcher("/metrics/?(\\?.*)?", null)
        assertThat(matcher.matches(request("/metrics"))).isTrue()
        assertThat(matcher.matches(request("/metrics?"))).isTrue()
        assertThat(matcher.matches(request("/metrics/"))).isTrue()
        assertThat(matcher.matches(request("/metrics/?"))).isTrue()
        assertThat(matcher.matches(request("/metrics/?"))).isTrue()
        assertThat(matcher.matches(request("/metrics/ab"))).isFalse()
        assertThat(matcher.matches(request("/metrics?parameter=value"))).isTrue()
        assertThat(matcher.matches(request("/metrics/?parameter=value"))).isTrue()
        assertThat(matcher.matches(request("/metrics-resource/?parameter=value"))).isFalse()
    }

    @Test
    fun `regex matcher matches allows sub-paths`() {
        val matcher = RegexRequestMatcher("/api-violations/?([a-z0-9-]+/?)?(\\?)?", null)
        assertThat(matcher.matches(request("/api-violations"))).isTrue()
        assertThat(matcher.matches(request("/api-violations?"))).isTrue()
        assertThat(matcher.matches(request("/api-violations/"))).isTrue()
        assertThat(matcher.matches(request("/api-violations/?"))).isTrue()
        assertThat(matcher.matches(request("/api-violations/?"))).isTrue()
        assertThat(matcher.matches(request("/api-violations/dcb5a97e-3586-44fc-b40c-90e209cf2e73"))).isTrue()
        assertThat(matcher.matches(request("/api-violations/dcb5a97e-3586-44fc-b40c-90e209cf2e73/"))).isTrue()
        assertThat(matcher.matches(request("/api-violations?parameter=value"))).isFalse()
        assertThat(matcher.matches(request("/api-violations/?parameter=value"))).isFalse()
        assertThat(matcher.matches(request("/api-violations-resource/?parameter=value"))).isFalse()
    }

    @Test
    @Ignore("The outcome of this test does not match the behaviour explained in the documentation. Probably cause: unable to correctly mock the `HttpServletRequest`.")
    fun `mvcMatcher matches requests with and without trailing slash but also unwanted deeper paths`() {
        val matcher = MvcRequestMatcher(null, "/metrics/")
        assertThat(matcher.matches(request("/metrics"))).isTrue()
        assertThat(matcher.matches(request("/metrics/"))).isTrue()
        assertThat(matcher.matches(request("/metrics/further"))).isTrue()
    }

    private fun request(path: String) = Mockito.mock(HttpServletRequest::class.java).also {
        Mockito.`when`(it.servletPath).thenReturn(path)
        Mockito.`when`(it.contextPath).thenReturn(path)
        Mockito.`when`(it.requestURI).thenReturn(path)
    }
}
