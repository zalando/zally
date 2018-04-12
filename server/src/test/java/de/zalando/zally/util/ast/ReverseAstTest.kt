package de.zalando.zally.util.ast

import de.zalando.zally.rule.ObjectTreeReader
import de.zalando.zally.util.ResourceUtil.resourceToString
import io.swagger.parser.OpenAPIParser
import io.swagger.parser.SwaggerParser
import io.swagger.util.Json
import io.swagger.v3.parser.core.models.ParseOptions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReverseAstTest {
    @Test
    fun `create JSON pointers from Swagger 2 object`() {
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
        assertThat(ast.getPointer(description)).isEqualTo("#/paths/~1tests/get/responses/200/description")
    }

    @Test
    fun `create ignore marker from Swagger 2 object`() {
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

        var pointer = "#/paths/~1tests/get/responses/200/description"
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues("#/paths/~1tests/get/responses/200/description")).hasSize(1).contains("*")

        pointer = "#/paths/~1tests/get"
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = "#/paths/~1tests"
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = "#/paths"
        assertThat(ast.isIgnored(pointer, "*")).isFalse()
        assertThat(ast.getIgnoreValues(pointer)).isEmpty()

        pointer = "#/paths/others"
        assertThat(ast.isIgnored(pointer, "*")).isFalse()
        assertThat(ast.getIgnoreValues(pointer)).isEmpty()
    }

    @Test
    fun `create from Swagger 2 document`() {
        val content = resourceToString("fixtures/swagger2_petstore_expanded.yaml")
        val spec = SwaggerParser().parse(content)
        val ast = ReverseAst.fromObject(spec).build()
        assertThat(ast.getPointer(spec)).isEqualTo("#")
    }

    @Test
    fun `create from OpenApi 3 document`() {
        val content = resourceToString("fixtures/openapi3_petstore_expanded.json")
        val spec = OpenAPIParser().readContents(content, null, ParseOptions())
        val ast = ReverseAst.fromObject(spec).build()
        assertThat(ast.getPointer(spec)).isEqualTo("#")
    }

    @Test
    fun `create ignore marker from Swagger 2 JSON Node`() {
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

        var pointer = "#/paths/~1tests/get/responses/200/description"
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues("#/paths/~1tests/get/responses/200/description")).hasSize(1).contains("*")

        pointer = "#/paths/~1tests/get"
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = "#/paths/~1tests"
        assertThat(ast.isIgnored(pointer, "*")).isTrue()
        assertThat(ast.getIgnoreValues(pointer)).hasSize(1).contains("*")

        pointer = "#/paths"
        assertThat(ast.isIgnored(pointer, "*")).isFalse()
        assertThat(ast.getIgnoreValues(pointer)).isEmpty()

        pointer = "#/paths/others"
        assertThat(ast.isIgnored(pointer, "*")).isFalse()
        assertThat(ast.getIgnoreValues(pointer)).isEmpty()
    }
}
