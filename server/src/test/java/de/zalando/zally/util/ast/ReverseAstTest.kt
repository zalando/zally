package de.zalando.zally.util.ast

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.ObjectTreeReader
import de.zalando.zally.util.resourceToString
import io.swagger.parser.OpenAPIParser
import io.swagger.parser.SwaggerParser
import io.swagger.util.Json
import io.swagger.v3.parser.OpenAPIResolver
import io.swagger.v3.parser.core.models.ParseOptions
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
        assertThat(ast.getPointer(description)).hasToString("/paths/~1tests/get/responses/200/description")
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

        var pointer = JsonPointer.compile("/paths/~1tests/get/responses/200/description")
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = JsonPointer.compile("/paths/~1tests/get")
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = JsonPointer.compile("/paths/~1tests")
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = JsonPointer.compile("/paths")
        assertThat(ast.isIgnored(pointer, "*")).isFalse()
        assertThat(ast.getIgnoreValues(pointer)).isEmpty()

        pointer = JsonPointer.compile("/paths/others")
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

        var pointer = JsonPointer.compile("/paths/~1tests/get/responses/200/description")
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = JsonPointer.compile("/paths/~1tests/get")
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = JsonPointer.compile("/paths/~1tests")
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = JsonPointer.compile("/paths")
        assertThat(ast.isIgnored(pointer, "*")).isFalse()
        assertThat(ast.getIgnoreValues(pointer)).isEmpty()

        pointer = JsonPointer.compile("/paths/others")
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
}
