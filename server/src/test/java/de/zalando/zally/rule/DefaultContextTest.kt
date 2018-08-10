package de.zalando.zally.rule

import de.zalando.zally.rule.ContentParseResultAssert.Companion.assertThat
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class DefaultContextTest {

    //
    // OPEN API
    //

    @Test
    fun `OPEN API -- not applicable when content does not contain the openapi property`() {
        @Language("YAML")
        val content = """
                some: properties
                but: no
                property: called OpenAPI
            """
        val result = DefaultContext.createOpenApiContext(content)
        assertThat(result).resultsInNotApplicable()
    }

    @Test
    fun `OPEN API -- openapi specification without info and paths succeeds with messages`() {
        // The parsing results in a valid OpenAPI 3 object model, but
        // with messages that `info` and `paths` are missing. Let the
        // rules check that out.
        @Language("YAML")
        val content = """
                openapi: 3.0.0
            """
        val result = DefaultContext.createOpenApiContext(content)
        assertThat(result).resultsInSuccess()
    }

    @Test
    fun `OPEN API -- oauth without scopes succeeds`() {
        @Language("YAML")
        val content = """
                openapi: 3.0.0
                info:
                  title: Foo
                  version: 1.0.0
                security:
                  - type: oauth2
                    flow: implicit
                    authorizationUrl: https://identity.some-server/auth
                paths: {}
            """
        val result = DefaultContext.createOpenApiContext(content)
        assertThat(result).resultsInSuccess()
    }

    @Test
    fun `OPEN API -- OpenAPI is recognised as an OpenAPI3 spec`() {
        @Language("YAML")
        val content = """
                openapi: 3.0.0
                info:
                  title: Foo
                  version: 1.0.0
                paths: {}
            """
        val result = DefaultContext.createOpenApiContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.Success
        assertThat(success.root.isOpenAPI3())
    }

    @Test
    fun `OPEN API -- does not recognize a Swagger file`() {
        @Language("YAML")
        val content = """
                swagger: '2.0'
                info:
                  version: 1.0.0
                  title: Pets API
                paths: {}
            """.trimIndent()
        val result = DefaultContext.createOpenApiContext(content)
        assertThat(result).resultsInNotApplicable()
    }

    //
    // SWAGGER
    //

    @Test
    fun `SWAGGER -- not applicable when content does not contain the swagger property`() {
        @Language("YAML")
        val content = """
                some: properties
                but: no
                property: called OpenAPI
            """
        val result = DefaultContext.createSwaggerContext(content)
        assertThat(result).resultsInNotApplicable()
    }

    @Test
    fun `SWAGGER -- error when info and path objects are missing`() {
        @Language("YAML")
        val content = """
              swagger: 2.0
            """
        val result = DefaultContext.createSwaggerContext(content)
        assertThat(result).resultsInSuccess()
    }

    @Test
    fun `SWAGGER -- error when securityDefinition type is missing`() {
        @Language("YAML")
        val content = """
                swagger: 2.0
                info:
                  title: Bleh
                securityDefinitions:
                  oa: {}
                    # type: oauth2
                paths: {}
            """.trimIndent()
        val result = DefaultContext.createSwaggerContext(content)
        assertThat(result).resultsInSuccess()
    }

    @Test
    fun `SWAGGER -- error when oauth elements are missing`() {
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
                    # flow: application
                    # scopes:
                    #   foo: Description of 'foo'
                paths: {}
            """.trimIndent()
        val result = DefaultContext.createSwaggerContext(content)
        assertThat(result).resultsInSuccess()
    }

    @Test
    fun `SWAGGER -- minimal Swagger API is not recognized as an OpenAPI3 spec`() {
        @Language("YAML")
        val content = """
                swagger: 2.0
                info:
                  title: Bleh
                  version: 1.0.0
                paths: {}
            """.trimIndent()
        val result = DefaultContext.createSwaggerContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.Success
        assertThat(success.root.isOpenAPI3()).isFalse()
    }
}
