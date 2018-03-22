package de.zalando.zally.util

import de.zalando.zally.util.ResourceUtil.resourceToString
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class OpenApiWalkerTest {
    private val ignore = HashSet(Arrays.asList(
            io.swagger.models.ArrayModel::class.java,
            io.swagger.models.ComposedModel::class.java,
            io.swagger.models.ModelImpl::class.java
    )) as Collection<Class<*>>?

    @Test
    fun `create Swagger 2 JSON pointers`() {
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
        val map = OpenApiWalker.walk(spec, ignore)
        assertThat(map).hasSize(13)

        val description = spec.paths?.get("/tests")?.get?.responses?.get("200")?.description
        assertThat(map[description]?.pointer).isEqualTo("#/paths/~1tests/get/responses/200/description")
    }

    @Test
    fun `add Swagger 2 ignore marker`() {
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
        val map = OpenApiWalker.walk(spec, ignore)
        assertThat(map).hasSize(14)

        val description = spec.paths?.get("/tests")?.get?.responses?.get("200")?.description
        assertThat(map[description]?.marker).isEqualTo(OpenApiWalker.Marker.X_ZALLY_IGNORE)
        assertThat(map[description]?.markerValue).isEqualTo("*")

        val get = spec.paths?.get("/tests")?.get
        assertThat(map[get]?.marker).isEqualTo(OpenApiWalker.Marker.X_ZALLY_IGNORE)
        assertThat(map[get]?.markerValue).isEqualTo("*")

        val testsPath = spec.paths?.get("/tests")
        assertThat(map[testsPath]?.marker).isNull()
        assertThat(map[testsPath]?.markerValue).isNull()
    }


    @Test
    fun `walk Swagger document`() {
        val content = resourceToString("fixtures/api_spp.json")
        val map = SwaggerParser().parse(content)
        val result = OpenApiWalker.walk(map, ignore)
        assertThat(result).isNotEmpty
        // println(result.values.map { "${it.pointer} ${it.markerValue}" }.joinToString("\n"))
        // println("mapped ${result.size} paths")
    }
}
