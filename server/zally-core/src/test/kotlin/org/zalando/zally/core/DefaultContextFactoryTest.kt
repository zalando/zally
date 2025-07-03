package org.zalando.zally.core

import com.fasterxml.jackson.core.JsonPointer
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.core.ContentParseResultAssert.Companion.assertThat
import org.zalando.zally.core.util.getAllParameters
import org.zalando.zally.rule.api.Violation

class DefaultContextFactoryTest {

    private val defaultContextFactory = DefaultContextFactory()

    //
    // OPEN API
    //

    @Test
    fun `OPEN API -- not applicable when content is a String without any properties`() {
        @Language("YAML")
        val content = "Pets API".trimIndent()
        val result = defaultContextFactory.parseOpenApiContext(content)

        assertThat(result).resultsInNotApplicable()
    }

    @Test
    fun `OPEN API -- Openapi property is expected to be String according to the OpenAPI3 spec`() {
        @Language("YAML")
        val content = """
                openapi: {la: 3.0.0}
            """
        val result = defaultContextFactory.parseOpenApiContext(content)
        assertThat(result).resultsInErrors(
            Violation(
                description = "attribute openapi is not of type `string`",
                pointer = JsonPointer.empty()
            )
        )
    }

    @Test
    fun `OPEN API -- not applicable when content does not contain the openapi property`() {
        @Language("YAML")
        val content = """
                some: properties
                but: no
                property: called OpenAPI
            """
        val result = defaultContextFactory.parseOpenApiContext(content)
        assertThat(result).resultsInNotApplicable()
    }

    @Test
    fun `OPEN API -- OpenAPI 3-1 without info and paths succeeds with messages`() {
        // The parsing results in a valid OpenAPI 3.1 object model, but
        // with messages that `info` and `paths` are missing. Let the
        // rules check that out.
        @Language("YAML")
        val content = """
                openapi: 3.1.0
            """
        val result = defaultContextFactory.parseOpenApiContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.ParsedSuccessfully
        assertThat(success.result.isOpenAPI3()).isTrue()
    }

    @Test
    fun `OPEN API -- OpenAPI 3-1 with oauth but without scopes succeeds`() {
        @Language("YAML")
        val content = """
                openapi: 3.1.0
                info:
                  title: Foo
                  version: 1.0.0
                security:
                  - type: oauth2
                    flow: implicit
                    authorizationUrl: https://identity.some-server/auth
                paths: {}
            """
        val result = defaultContextFactory.parseOpenApiContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.ParsedSuccessfully
        assertThat(success.result.isOpenAPI3()).isTrue()
    }

    @Test
    fun `OPEN API -- OpenAPI 3-1 is recognised as an OpenAPI3 spec`() {
        @Language("YAML")
        val content = """
                openapi: 3.1.0
                info:
                  title: Foo
                  version: 1.0.0
                paths: {}
            """
        val result = defaultContextFactory.parseOpenApiContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.ParsedSuccessfully
        assertThat(success.result.isOpenAPI3()).isTrue()
    }

    @Test
    fun `OPEN API -- OpenAPI 3-1-0 all refs are resolved`() {
        @Language("YAML")
        val content = """
                openapi: '3.1.0'
                info:
                  title: API 1
                  contact: 
                    name: "Team One"
                  version: 1.0.0
                paths:
                  /items/{item-id}:
                    get:
                      parameters:
                        - ${'$'}ref: "#/components/parameters/PathParameter"
                      responses: 
                        default:
                         description: Response
                         content:                        
                           application/json:
                             schema: 
                               type: string
                components: 
                  parameters:
                    PathParameter:
                      in: path  
                      name: item-id                            
                      description: The id of the pet to retrieve
                      required: true
                      schema:
                        type: string
        """.trimIndent()

        val result = defaultContextFactory.parseOpenApiContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.ParsedSuccessfully
        assertThat(success.result.isOpenAPI3()).isTrue()

        val params = success.result.api.getAllParameters()
        assertThat(params).hasSize(2)
        assertThat(params).allMatch { it.name == "item-id" }
        assertThat(params).allMatch { it.`$ref` == null }
    }

    @Test
    fun `OPEN API -- OpenAPI 3-0-0 without info and paths succeeds`() {
        // The parsing results in a valid OpenAPI 3 object model, but
        // with messages that `info` and `paths` are missing. Let the
        // rules check that out.
        @Language("YAML")
        val content = """
                openapi: 3.0.0
            """
        val result = defaultContextFactory.parseOpenApiContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.ParsedSuccessfully
        assertThat(success.result.isOpenAPI3()).isTrue()
    }

    @Test
    fun `OPEN API -- OpenAPI 3-0-1 without info and paths succeeds`() {
        // The parsing results in a valid OpenAPI 3 object model, but
        // with messages that `info` and `paths` are missing. Let the
        // rules check that out.
        @Language("YAML")
        val content = """
                openapi: 3.0.1
            """
        val result = defaultContextFactory.parseOpenApiContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.ParsedSuccessfully
        assertThat(success.result.isOpenAPI3()).isTrue()
    }

    @Test
    fun `OPEN API -- OpenAPI 3-0-2 without info and paths succeeds`() {
        // The parsing results in a valid OpenAPI 3 object model, but
        // with messages that `info` and `paths` are missing. Let the
        // rules check that out.
        @Language("YAML")
        val content = """
                openapi: 3.0.2
            """
        val result = defaultContextFactory.parseOpenApiContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.ParsedSuccessfully
        assertThat(success.result.isOpenAPI3()).isTrue()
    }

    @Test
    fun `OPEN API -- OpenAPI 3-0-3 without info and paths succeeds`() {
        // The parsing results in a valid OpenAPI 3 object model, but
        // with messages that `info` and `paths` are missing. Let the
        // rules check that out.
        @Language("YAML")
        val content = """
                openapi: 3.0.3
            """
        val result = defaultContextFactory.parseOpenApiContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.ParsedSuccessfully
        assertThat(success.result.isOpenAPI3()).isTrue()
    }

    @Test
    fun `OPEN API -- OpenAPI 3-0-x with oauth but without scopes succeeds`() {
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
        val result = defaultContextFactory.parseOpenApiContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.ParsedSuccessfully
        assertThat(success.result.isOpenAPI3()).isTrue()
    }

    @Test
    fun `OPEN API -- OpenAPI 3-0-x is recognised as an OpenAPI3 spec`() {
        @Language("YAML")
        val content = """
                openapi: 3.0.0
                info:
                  title: Foo
                  version: 1.0.0
                paths: {}
            """
        val result = defaultContextFactory.parseOpenApiContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.ParsedSuccessfully
        assertThat(success.result.isOpenAPI3()).isTrue()
    }

    @Test
    fun `OPEN API -- OpenAPI 2-0 is not recognize as OpenAPI3 spec`() {
        @Language("YAML")
        val content = """
                swagger: '2.0'
                info:
                  version: 1.0.0
                  title: Pets API
                paths: {}
        """.trimIndent()
        val result = defaultContextFactory.parseOpenApiContext(content)
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
        val result = defaultContextFactory.parseSwaggerContext(content)
        assertThat(result).resultsInNotApplicable()
    }

    @Test
    fun `SWAGGER -- OpenAPI 1-0 without info and path objects succeeds`() {
        @Language("YAML")
        val content = """
              swagger: 1.0
            """
        val result = defaultContextFactory.parseSwaggerContext(content)
        assertThat(result).resultsInSuccess()
    }

    @Test
    fun `SWAGGER -- OpenAPI 2-0 without info and path objects succeeds`() {
        @Language("YAML")
        val content = """
              swagger: 2.0
            """
        val result = defaultContextFactory.parseSwaggerContext(content)
        assertThat(result).resultsInSuccess()
    }

    @Test
    fun `SWAGGER -- OpenAPI 2-0 without security definition type succeeds`() {
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
        val result = defaultContextFactory.parseSwaggerContext(content)
        assertThat(result).resultsInSuccess()
    }

    @Test
    fun `SWAGGER -- OpenAPI 2-0 without oauth elements succeeds`() {
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
        val result = defaultContextFactory.parseSwaggerContext(content)
        assertThat(result).resultsInSuccess()
    }

    @Test
    fun `SWAGGER -- OpenAPI 2-0 is not recognized as an OpenAPI3 spec`() {
        @Language("YAML")
        val content = """
                swagger: 2.0
                info:
                  title: Bleh
                  version: 1.0.0
                paths: {}
        """.trimIndent()
        val result = defaultContextFactory.parseSwaggerContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.ParsedSuccessfully
        assertThat(success.result.isOpenAPI3()).isFalse()
    }

    @Test
    fun `SWAGGER -- OpenAPI 2-0 with recursive-model-extension succeeds`() {
        @Language("YAML")
        val content = """
            swagger: '2.0'
            info:
              title: Tree API
              version: 1.0.0
            paths:
              '/tree':
                get:
                  responses:
                    200:
                      description: List of nodes.
                      schema:
                        ${'$'}ref: '#/definitions/ReadNode'
            definitions:
              WriteNode:
                type: object
                properties:
                  name:
                    type: string
                  children:
                    type: array
                    items:
                      ${'$'}ref: '#/definitions/WriteNode'
              ReadNode:
                allOf:
                  - ${'$'}ref: '#/definitions/WriteNode'
                  - type: object
                    properties:
                      extra: # property that WriteNode doesn't have
                        type: string
                      children: # children redefined to be ReadNode rather than WriteNode
                        type: array
                        items:
                          ${'$'}ref: '#/definitions/ReadNode'
        """.trimIndent()
        val result = defaultContextFactory.parseSwaggerContext(content)
        assertThat(result).resultsInSuccess()
        val success = result as ContentParseResult.ParsedSuccessfully
        assertThat(success.result.isOpenAPI3()).isFalse()
    }

    @Test
    fun `OpenAPI Resolve NPE workaround is avoided when converted Swagger has components with null schemas`() {
        // This Swagger, after being converted, causes the `components` property to exist (not
        // null), but having a null `schemas`, which causes the NPE.
        @Language("YAML")
        val content = """
          swagger: '2.0'
          info:
            version: 1.0.0
            title: Swagger Petstore
            description: A sample API that uses a petstore as an example to demonstrate features
              in the swagger-2.0 specification
            termsOfService: http://swagger.io/terms/
            contact:
              name: Swagger API Team
            license:
              name: MIT
          paths:
            /identifier-types/{identifier_type}/source-ids/{source_identifier}:
              get:
                tags:
                  - id-mappings
                description: List of identifiers associated with the source id.
                parameters:
                  - ${'$'}ref: '#/parameters/identifier_type'
                  - ${'$'}ref: '#/parameters/source_identifier'
                  - ${'$'}ref: '#/parameters/target_identifier_type'
                responses:
                  200:
                    description: The identifiers associated with the source id.
                    schema:
                      ${'$'}ref: '#/definitions/IdMappingResults'
                  401:
                    description: User is not authenticated
                  403:
                    description: User is not authorized
                  404:
                    description: Identifier is not found
                    schema:
                      ${'$'}ref: '#definitions/Problem'
                security:
                  - oauth2:
                    - 'cross-device-graph-service.read'
        """.trimIndent()
        val result = defaultContextFactory.parseSwaggerContext(content)
        ContentParseResultAssert.assertThat(result).resultsInSuccess()
    }
}
