package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.rule.api.Context
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class IdentifyResourcesViaPathSegmentsTest {

    private val rule = IdentifyResourcesViaPathSegments()

    @Test
    fun `should return a violation if path starts with parameter`() {
        val violations = rule.pathStartsWithResource(withPath("/{merchant-id}"))

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*must start with a resource*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1{merchant-id}")
    }

    @Test
    fun `should return a violation if path doesn't start with a resource`() {
        val violations = rule.pathStartsWithResource(withPath("/"))

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*must start with a resource*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1")
    }

    @Test
    fun `should not return any violations if path doesn't start with a parameter`() {
        assertThat(rule.pathStartsWithResource(withPath("/orders"))).isEmpty()
        assertThat(rule.pathStartsWithResource(withPath("/orders/{order-id}"))).isEmpty()
    }

    @Test
    fun `should return a violation if path contains successive parameters`() {
        val violations = rule
            .pathDoesNotContainSuccessiveParameters(withPath("/merchants/{merchant-id}/{address-id}"))

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*must not contain successive parameters*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1merchants~1{merchant-id}~1{address-id}")
    }

    @Test
    fun `should not return any violations if path doesn't contain successive parameters`() {
        assertThat(rule.pathDoesNotContainSuccessiveParameters(withPath("/customers"))).isEmpty()
        assertThat(rule.pathDoesNotContainSuccessiveParameters(withPath("/customers/{customer-id}"))).isEmpty()
        assertThat(
            rule.pathDoesNotContainSuccessiveParameters(
                withPath("/customers/{customer-id}/addresses/primary")
            )
        ).isEmpty()
    }

    @Test
    fun `should return a violation if path parameter contains prefix`() {
        val violations = rule.pathParameterDoesNotContainPrefixAndSuffix(
            withPath("/orders/de-{order-id}")
        )

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*must not contain prefixes and suffixes*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1orders~1de-{order-id}")
    }

    @Test
    fun `should return a violation if path parameter contains suffix`() {
        val violations = rule.pathParameterDoesNotContainPrefixAndSuffix(
            withPath("/orders/{order-id}-de")
        )

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*must not contain prefixes and suffixes*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1orders~1{order-id}-de")
    }

    @Test
    fun `should not return any violations if path parameter doesn't have a prefix or suffix`() {
        assertThat(rule.pathParameterDoesNotContainPrefixAndSuffix(withPath("/"))).isEmpty()
        assertThat(rule.pathParameterDoesNotContainPrefixAndSuffix(withPath("/orders"))).isEmpty()
        assertThat(rule.pathParameterDoesNotContainPrefixAndSuffix(withPath("/orders/{id}"))).isEmpty()
        assertThat(rule.pathParameterDoesNotContainPrefixAndSuffix(withPath("/orders/{id}/items"))).isEmpty()
    }

    private fun withPath(path: String): Context {
        @Language("YAML")
        val content = """
            openapi: 3.0.0
            paths:
              $path: {}
        """.trimIndent()

        return DefaultContextFactory().getOpenApiContext(content)
    }
}
