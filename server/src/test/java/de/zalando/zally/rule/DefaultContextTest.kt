package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.ResourceUtil.resourceToString
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.intellij.lang.annotations.Language
import org.junit.Test

class DefaultContextTest {
    @Test
    fun createSwaggerContextFromSwaggerJson() {
        val content = resourceToString("fixtures/swagger2_petstore_expanded.yaml")
        val context = DefaultContext.createSwaggerContext(content)
        assertThat(context).isNotNull
        assertThat(context?.api).isInstanceOf(OpenAPI::class.java)
        assertThat(context?.violation("", context.api.info.title)?.pointer).hasToString("/info/title")
    }

    @Test
    fun createOpenApiContextFromSwaggerJson() {
        val content = resourceToString("fixtures/swagger2_petstore_expanded.yaml")
        val context = DefaultContext.createOpenApiContext(content)
        assertThat(context).isNull()
    }

    @Test
    fun createOpenApiContextFromOpenApiJson() {
        val content = resourceToString("fixtures/openapi3_petstore_expanded.json")
        val context = DefaultContext.createOpenApiContext(content)
        assertThat(context).isNotNull
        assertThat(context?.api).isInstanceOf(OpenAPI::class.java)
        assertThat(context?.violation("", context.api.info.title)?.pointer).hasToString("/info/title")
    }

    @Test
    fun `should recognize the used OpenAPI 2 (aka Swagger)`() {
        val openapi3Context = DefaultContext.createSwaggerContext("""
        swagger: '2.0'
        info:
          version: 1.0.0
          title: Pets API
        paths: {}
        """.trimIndent())!!

        assertThat(openapi3Context.isOpenAPI3()).isFalse()
    }

    @Test
    fun `should recognize the used OpenAPI 3`() {
        val openapi3Context = DefaultContext.createOpenApiContext("""
        openapi: 3.0.1
        info:
          title: Pets API
          version: 1.0.0
        paths: {}
        """.trimIndent())!!

        assertThat(openapi3Context.isOpenAPI3()).isTrue()
    }

    class PreChecks {

        private fun assertHasPreCheckViolations(content: String): List<Violation> {
            val thrown = catchThrowable { DefaultContext.createSwaggerContext(content, true) }
            assertThat(thrown).isInstanceOf(PreCheckViolationsException::class.java)
            return (thrown as PreCheckViolationsException).violations
        }

        @Test
        fun `spec without INFO causes pre-check violations`() {
            // Specific case where converting from Swagger to OpenAPI 3 (using the `Context`
            // object) would throw an exception. New behaviour tested here: the returned `Context`
            // is null because the file was not parsed (convertible, here).
            @Language("YAML")
            val content = """
                truc: muche
            """.trimIndent()
            val violations = assertHasPreCheckViolations(content)
            assertThat(violations).contains(
                Violation("", JsonPointer.compile("/"))
            )

        }

        @Test
        fun `spec without OAUTH scopes should not crash`() {
            // Specific case where converting from Swagger to OpenAPI 3 (using the `Context`
            // object) would throw an exception. New behaviour tested here: the returned `Context`
            // is null because the file was not parsed (convertible, here).
            @Language("YAML")
            val content = """
                swagger: 2.0
                info:
                  title: Bleh
                securityDefinitions:
                  oa:
                    type: oauth2
                    flow: application
                    # scopes:
                    #   foo: Description of 'foo'
                paths: {}
            """.trimIndent()
            val violations = assertHasPreCheckViolations(content)
            assertThat(violations).hasSameElementsAs(listOf(
                TODO()
            ))

        }
    }
}
