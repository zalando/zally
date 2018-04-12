package de.zalando.zally.util.ast

import de.zalando.zally.util.ResourceUtil.resourceToString
import io.swagger.parser.OpenAPIParser
import io.swagger.parser.SwaggerParser
import io.swagger.v3.parser.converter.SwaggerConverter
import io.swagger.v3.parser.core.models.ParseOptions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class ReverseAstTest {
    private val ignore = HashSet(Arrays.asList(
            io.swagger.models.ArrayModel::class.java,
            io.swagger.models.ComposedModel::class.java,
            io.swagger.models.ModelImpl::class.java
    )) as Collection<Class<*>>?

    @Test
    fun `create with Swagger 2 JSON pointers`() {
        val content = """
            {
              "swagger": "2.0",
              "info": {
                "title": "Things API",
                "description": "Description of things",
                "version": "1.0.0"
              },
              "paths": {
                "/tests": {
                  "get": {
                    "responses": {
                      "200": {
                        "description": "OK"
                      }
                    }
                  }
                }
              }
            }
            """.trimIndent()

        val spec = SwaggerParser().parse(content)
        val ast = ReverseAst.fromObject(spec).ignore(ignore).build()

        val description = spec.paths?.get("/tests")?.get?.responses?.get("200")?.description
        assertThat(ast.getPointer(description)).isEqualTo("#/paths/~1tests/get/responses/200/description")
    }

    @Test
    fun `create with Swagger 2 ignore marker`() {
        val content = """
            {
              "swagger": "2.0",
              "info": {
                "title": "Things API",
                "description": "Description of things",
                "version": "1.0.0"
              },
              "paths": {
                "/tests": {
                  "x-zally-ignore": "*",
                  "get": {
                    "responses": {
                      "200": {
                        "description": "OK"
                      }
                    }
                  }
                }
              }
            }
            """.trimIndent()

        val spec = SwaggerParser().parse(content)
        val ast = ReverseAst.fromObject(spec).ignore(ignore).build()

        val description = spec.paths?.get("/tests")?.get?.responses?.get("200")?.description
        assertThat(ast.isIgnored(description)).isTrue()
        assertThat(ast.getIgnoreValue(description)).isEqualTo("*")

        val get = spec.paths?.get("/tests")?.get
        assertThat(ast.isIgnored(get)).isTrue()
        assertThat(ast.getIgnoreValue(get)).isEqualTo("*")

        val testsPath = spec.paths?.get("/tests")
        assertThat(ast.isIgnored(testsPath)).isFalse()
        assertThat(ast.getIgnoreValue(testsPath)).isNull()
    }

    @Test
    fun `create from Swagger 2 document`() {
        val content = resourceToString("fixtures/swagger2_petstore_expanded.yaml")
        val spec = SwaggerParser().parse(content)
        val ast = ReverseAst.fromObject(spec).ignore(ignore).build()
        assertThat(ast.getPointer(spec)).isEqualTo("#")
    }

    @Test
    fun `create from OpenApi 3 document`() {
        val content = resourceToString("fixtures/openapi3_petstore_expanded.yaml")
        val spec = OpenAPIParser().readContents(content, null, ParseOptions())
        val ast = ReverseAst.fromObject(spec).ignore(ignore).build()
        assertThat(ast.getPointer(spec)).isEqualTo("#")
    }

    @Test
    fun `with conversion from Swagger 2 and identical AST values`() {
        val content = resourceToString("fixtures/swagger2_petstore_expanded.yaml")
        val swaggerDeserializationResult = SwaggerParser().readWithInfo(content)
        val swaggerSpec = swaggerDeserializationResult.swagger
        val openApiSpec = SwaggerConverter().convert(swaggerDeserializationResult).openAPI

        val openApiAst = ReverseAst.fromObject(openApiSpec).ignore(ignore).build()
        val swaggerAst = ReverseAst.fromObject(swaggerSpec).ignore(ignore).build()

        val description = openApiSpec.paths?.get("/pets")?.get?.responses?.default?.description

        assertThat(description).isEqualTo("unexpected error")
        assertThat(openApiAst.getPointer(description)).isEqualTo("#/paths/~1pets/get/responses/default/description")
        assertThat(swaggerAst.getPointer(description)).isEqualTo("#/paths/~1pets/get/responses/default/description")
    }

    @Test
    fun `with conversion from Swagger 2 document and incompatible AST values`() {
        val content = resourceToString("fixtures/swagger2_petstore_expanded.yaml")
        val swaggerDeserializationResult = SwaggerParser().readWithInfo(content)
        val swaggerSpec = swaggerDeserializationResult.swagger
        val openApiSpec = SwaggerConverter().convert(swaggerDeserializationResult).openAPI

        val openApiAst = ReverseAst.fromObject(openApiSpec).build()
        val swaggerAst = ReverseAst.fromObject(swaggerSpec).build()

        val openApiPet = openApiSpec.components.schemas["Pet"]
        val openApiPetNode = openApiAst.getNode(openApiPet)

        assertThat(openApiAst.getPointer(openApiPet)).isEqualTo("#/components/schemas/Pet")
        assertThat(swaggerAst.getPointer(openApiPetNode)).isEqualTo("#/definitions/Pet")

        val openApiUrl = openApiSpec.servers[0].url
        val openApiUrlNode = openApiAst.getNode(openApiUrl)

        assertThat(openApiAst.getPointer(openApiUrl)).isEqualTo("#/servers/0/url")
        assertThat(swaggerAst.getPointer(openApiUrlNode)).isEqualTo("#/basePath")
    }
}
