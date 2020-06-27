package org.zalando.zally.core.ast

import com.fasterxml.jackson.core.JsonPointer
import org.zalando.zally.core.ObjectTreeReader
import org.zalando.zally.core.toJsonPointer
import io.swagger.parser.OpenAPIParser
import io.swagger.parser.SwaggerParser
import io.swagger.util.Json
import io.swagger.v3.parser.OpenAPIResolver
import io.swagger.v3.parser.core.models.ParseOptions
import org.apache.commons.io.IOUtils
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

@Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "StringLiteralDuplication")
class ReverseAstTest {
    @Test
    fun `create JSON pointers from Swagger 2 object`() {
        @Language("yaml")
        val content = """
            swagger: '2.0'
            info:
              title: Things API
              description: Description of things
              version: '1.0.0'
            paths:
              "/tests":
                get:
                  responses:
                    '200':
                      description: OK
            """.trimIndent()

        val spec = SwaggerParser().parse(content)
        val ast = ReverseAst.fromObject(spec).build()

        val description = spec.paths?.get("/tests")?.get?.responses?.get("200")?.description
        assertThat(ast.getPointer(description!!)).hasToString("/paths/~1tests/get/responses/200/description")
    }

    @Test
    fun `create ignore marker from Swagger 2 object`() {
        @Language("yaml")
        val content = """
            swagger: '2.0'
            info:
              title: Things API
              description: Description of things
              version: '1.0.0'
            paths:
              "/tests":
                x-zally-ignore: "*"
                get:
                  responses:
                    '200':
                      description: OK
              "/others":
                get:
                  responses:
                    '200':
                      description: OK
            """.trimIndent()

        val spec = SwaggerParser().parse(content)
        val ast = ReverseAst.fromObject(spec).withExtensionMethodNames("getVendorExtensions").build()

        var pointer = "/paths/~1tests/get/responses/200/description".toJsonPointer()
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = "/paths/~1tests/get".toJsonPointer()
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = "/paths/~1tests".toJsonPointer()
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = "/paths".toJsonPointer()
        assertThat(ast.isIgnored(pointer, "*")).isFalse()
        assertThat(ast.getIgnoreValues(pointer)).isEmpty()

        pointer = "/paths/others".toJsonPointer()
        assertThat(ast.isIgnored(pointer, "*")).isFalse()
        assertThat(ast.getIgnoreValues(pointer)).isEmpty()
    }

    @Test
    fun `create from Swagger 2 document`() {
        val content = resourceToString("fixtures/swagger2_petstore_expanded.yaml")
        val spec = SwaggerParser().parse(content)
        val ast = ReverseAst.fromObject(spec).build()
        assertThat(ast.getPointer(spec)).hasToString("")
    }

    @Test
    fun `create from OpenApi 3 document`() {
        val content = resourceToString("fixtures/openapi3_petstore_expanded.json")
        val spec = OpenAPIParser().readContents(content, null, ParseOptions())
        val ast = ReverseAst.fromObject(spec).build()
        assertThat(ast.getPointer(spec)).hasToString("")
    }

    @Test
    fun `create ignore marker from Swagger 2 JSON Node`() {
        @Language("yaml")
        val content = """
            swagger: '2.0'
            info:
              title: Things API
              description: Description of things
              version: '1.0.0'
            paths:
              "/tests":
                x-zally-ignore: "*"
                get:
                  responses:
                    '200':
                      description: OK
              "/others":
                get:
                  responses:
                    '200':
                      description: OK
            """.trimIndent()

        val json = ObjectTreeReader().read(content)
        val map = Json.mapper().convertValue(json, Map::class.java)
        val ast = ReverseAst.fromObject(map).build()

        var pointer = "/paths/~1tests/get/responses/200/description".toJsonPointer()
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = "/paths/~1tests/get".toJsonPointer()
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = "/paths/~1tests".toJsonPointer()
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = "/paths".toJsonPointer()
        assertThat(ast.isIgnored(pointer, "*")).isFalse()
        assertThat(ast.getIgnoreValues(pointer)).isEmpty()

        pointer = "/paths/others".toJsonPointer()
        assertThat(ast.isIgnored(pointer, "*")).isFalse()
        assertThat(ast.getIgnoreValues(pointer)).isEmpty()
    }

    @Test
    @Suppress("UnsafeCallOnNullableType")
    fun `ast prefers pointers to shared locations`() {
        @Language("yaml")
        val content = """
            openapi: '3.0.0'
            info:
              title: Things API
              version: 1.0.0
            paths:
              /things:
                post:
                  description: Description of /things
                  parameters:
                    - "${'$'}ref": "#/components/parameters/SharedParam"
                  responses:
                    200:
                      description: Description of 200 response
            components:
              parameters:
                SharedParam:
                  name: p
                  in: query
                  description: Parameter p
                  required: true
                  schema:
                    type: string
                    format: uuid
                    default: "SchemaDefault!!"
                    example: "SchemaExample!!"
                  example: "ParameterExample!!"
            """.trimIndent()

        val parsed = OpenAPIParser().readContents(content, null, null).openAPI
        val resolved = OpenAPIResolver(parsed).resolve()
        val ast = ReverseAst.fromObject(resolved).build()

        val pathParam = resolved.paths["/things"]!!.post.parameters[0]
        val sharedParam = resolved.components.parameters["SharedParam"]!!

        val pathParamPointer = ast.getPointer(pathParam)
        val sharedParamPointer = ast.getPointer(sharedParam)

        assertThat(pathParamPointer)
            .isSameAs(sharedParamPointer)
            .hasToString("/components/parameters/SharedParam")
    }

    @Test
    fun `isIgnored even ignores unforeseen descendants of ignored nodes`() {
        @Language("yaml")
        val content = """
            swagger: '2.0'
            x-zally-ignore: [215, 218, 219]
            info:
              title: Some API
              version: '1.0.0'
              contact:
                name: Team X
                email: team@x.com
                url: https://team.x.com
            paths: {}
            """.trimIndent()

        val swagger = SwaggerParser().parse(content)
        val ast = ReverseAst.fromObject(swagger).withExtensionMethodNames("getVendorExtensions").build()

        assertThat(ast.isIgnored("".toJsonPointer(), "218")).isTrue()
        assertThat(ast.isIgnored("/info".toJsonPointer(), "218")).isTrue()
        assertThat(ast.isIgnored("/info/description".toJsonPointer(), "218")).isTrue()
    }

    @Test
    fun `OpenAPI extension JsonPointers are parsed correctly`() {
        @Language("yaml")
        val content = """
            openapi: 3.0.1
            info:
              title: Some API
              x-test-extension: 4
            paths: {}
            """.trimIndent()

        val parsed = OpenAPIParser().readContents(content, null, null).openAPI
        val ast = ReverseAst.fromObject(parsed).withExtensionMethodNames("getExtensions").build()

        assertThat(ast.getPointer(4)).isEqualTo(JsonPointer.compile("/info/x-test-extension"))
    }

    @Test
    fun `Swagger extension JsonPointers are parsed correctly`() {
        @Language("yaml")
        val content = """
            swagger: '2.0'
            info:
              title: Some API
              x-test-extension:
                multiple:
                  nested:
                    paths: 42
                  and:
                    another: 2
            paths: {}
            """.trimIndent()

        val swagger = SwaggerParser().parse(content)
        val ast = ReverseAst.fromObject(swagger).withExtensionMethodNames("getVendorExtensions").build()

        assertThat(ast.getPointer(42)).isEqualTo(JsonPointer.compile("/info/x-test-extension/multiple/nested/paths"))
        assertThat(ast.getPointer(2)).isEqualTo(JsonPointer.compile("/info/x-test-extension/multiple/and/another"))
    }

    @Test
    fun `isIgnored applies local AND inherited x-zally-ignore`() {
        @Language("yaml")
        val content = """
            swagger: '2.0'
            info:
              title: Some API
              version: '1.0.0'
              contact:
                name: Team X
                email: team@x.com
                url: https://team.x.com
              x-zally-ignore: [IGNORED_AT_INFO]
            paths: {}
            x-zally-ignore: [IGNORED_AT_ROOT]
            """.trimIndent()

        val swagger = SwaggerParser().parse(content)
        val ast = ReverseAst.fromObject(swagger)
            .withExtensionMethodNames("getVendorExtensions")
            .build()

        assertThat(ast.isIgnored("".toJsonPointer(), "IGNORED_AT_ROOT")).isTrue()
        assertThat(ast.isIgnored("".toJsonPointer(), "IGNORED_AT_INFO")).isFalse()
        assertThat(ast.isIgnored("/info".toJsonPointer(), "IGNORED_AT_ROOT")).isTrue()
        assertThat(ast.isIgnored("/info".toJsonPointer(), "IGNORED_AT_INFO")).isTrue()
        assertThat(ast.isIgnored("/info/contact".toJsonPointer(), "IGNORED_AT_ROOT")).isTrue()
        assertThat(ast.isIgnored("/info/contact".toJsonPointer(), "IGNORED_AT_INFO")).isTrue()
    }

    private fun resourceToString(resourceName: String): String =
        IOUtils.toString(ClassLoader.getSystemResourceAsStream(resourceName))
}
