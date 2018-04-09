package de.zalando.zally.util.ast

import de.zalando.zally.util.ResourceUtil.resourceToString
import io.swagger.parser.SwaggerParser
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
        val content = resourceToString("fixtures/api_spp.json")
        val spec = SwaggerParser().parse(content)
        val ast = ReverseAst.fromObject(spec).ignore(ignore).build()
        assertThat(ast.getPointer(spec)).isEqualTo("#")
    }
}
