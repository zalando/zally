package org.zalando.zally.core.util

import org.zalando.zally.core.DefaultContextFactory
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.headers.Header
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class OpenApiUtilTest {

    @Test
    fun `getAllHeaders should return headers from components parameters`() {
        val api = OpenAPI().apply {
            components = Components().apply {
                parameters = mapOf("User-Agent" to Parameter().apply {
                    `in` = "header"
                    name = "User-Agent"
                })
            }
        }

        val headers = api.getAllHeaders()

        assertThat(headers).isNotNull
        assertThat(headers).hasSize(1)
    }

    @Test
    fun `getAllHeaders should return headers from paths`() {
        val api = OpenAPI().apply {
            val paths = Paths()
            val pathItem = PathItem()
            pathItem.get = Operation().apply {
                parameters = listOf(Parameter().apply {
                    `in` = "header"
                    name = "User-Agent"
                })
            }
            paths.addPathItem("path", pathItem)

            this.paths = paths
        }

        val headers = api.getAllHeaders()

        assertThat(headers).isNotNull
        assertThat(headers).hasSize(1)
    }

    @Test
    fun `getAllHeaders should return headers from components`() {
        val api = OpenAPI().apply {
            components = Components().apply {
                headers = mapOf("User-Agent" to Header())
            }
        }

        val headers = api.getAllHeaders()

        assertThat(headers).isNotNull
        assertThat(headers).hasSize(1)
    }

    @Test
    fun `getAllHeaders should return empty set if no headers specified`() {
        val api = OpenAPI()

        val headers = api.getAllHeaders()

        assertThat(headers).isNotNull
        assertThat(headers).isEmpty()
    }

    @Test
    fun `getAllSchemas should return component schemas`() {
        val api = OpenAPI().apply {
            components = Components().apply {
                schemas = mapOf("pet" to Schema<String>())
            }
        }

        val schemas = api.getAllSchemas()

        assertThat(schemas).isNotNull
        assertThat(schemas).hasSize(1)
    }

    @Test
    fun `getAllSchemas should return component response schemas`() {
        val api = OpenAPI().apply {
            components = Components().apply {
                responses = mapOf("response" to ApiResponse().apply {
                    schemas = mapOf("pet" to Schema<String>())
                })
            }
        }

        val schemas = api.getAllSchemas()

        assertThat(schemas).isNotNull
        assertThat(schemas).hasSize(1)
    }

    @Test
    fun `getAllSchemas should return component request body schemas`() {
        val api = OpenAPI().apply {
            components = Components().apply {
                requestBodies = mapOf("request" to RequestBody().apply {
                    schemas = mapOf("pet" to Schema<String>())
                })
            }
        }

        val schemas = api.getAllSchemas()

        assertThat(schemas).isNotNull
        assertThat(schemas).hasSize(1)
    }

    @Test
    fun `getAllSchemas should return operation parameter schemas`() {
        val api = OpenAPI().apply {
            val paths = Paths()
            val pathItem = PathItem()
            pathItem.get = Operation().apply {
                parameters = listOf(Parameter().apply {
                    `in` = "body"
                    schema = Schema<String>()
                })
            }
            paths.addPathItem("path", pathItem)
            this.paths = paths
        }

        val schemas = api.getAllSchemas()

        assertThat(schemas).isNotNull
        assertThat(schemas).hasSize(1)
    }

    @Test
    fun `getAllSchemas should return operation response schemas`() {
        val api = OpenAPI().apply {
            val paths = Paths()
            val pathItem = PathItem()
            pathItem.get = Operation().apply {
                val responses = ApiResponses()
                responses.addApiResponse("response", ApiResponse().apply {
                    val content = Content()
                    content.addMediaType("application/json", MediaType().apply {
                        schema = Schema<String>()
                    })
                    this.content = content
                })
                this.responses = responses
            }
            paths.addPathItem("path", pathItem)
            this.paths = paths
        }

        val schemas = api.getAllSchemas()

        assertThat(schemas).isNotNull
        assertThat(schemas).hasSize(1)
    }

    @Test
    fun `getAllSchemas should return operation request body schemas`() {
        val api = OpenAPI().apply {
            val paths = Paths()
            val pathItem = PathItem()
            pathItem.get = Operation().apply {
                requestBody = RequestBody().apply {
                    val content = Content()
                    content.addMediaType("application/json", MediaType().apply {
                        schema = Schema<String>()
                    })
                    this.content = content
                }
            }
            paths.addPathItem("path", pathItem)
            this.paths = paths
        }

        val schemas = api.getAllSchemas()

        assertThat(schemas).isNotNull
        assertThat(schemas).hasSize(1)
    }

    @Test
    fun `getAllSchemas should return an empty collection if no schemas are specified`() {
        val api = OpenAPI()

        val schemas = api.getAllSchemas()

        assertThat(schemas).isNotNull
        assertThat(schemas).isEmpty()
    }

    @Test
    fun `getAllTransitiveSchemas should return a set of all schemas`() {
        val api = OpenAPI().apply {
            components = Components().apply {
                schemas = mapOf("pet" to Schema<Any>().apply {
                    title = "pet"
                    properties = mapOf(
                        "name" to Schema<String>().apply { title = "name" },
                        "age" to Schema<Int>().apply { title = "age" }
                    )
                })
            }
        }

        val schemas = api.getAllTransitiveSchemas()

        assertThat(schemas).isNotNull
        assertThat(schemas).hasSize(2)
    }

    @Test
    fun `getAllTransitiveSchemas should return an empty set if no schemas are specified`() {
        val api = OpenAPI()

        val schemas = api.getAllTransitiveSchemas()

        assertThat(schemas).isNotNull
        assertThat(schemas).isEmpty()
    }

    @Test
    fun `getAllProperties should return a map of all properties`() {
        val api = OpenAPI().apply {
            components = Components().apply {
                schemas = mapOf("pet" to Schema<Any>().apply {
                    title = "pet"
                    properties = mapOf(
                        "name" to Schema<String>().apply { title = "name" },
                        "age" to Schema<Int>().apply { title = "age" }
                    )
                })
            }
        }

        val schemas = api.getAllProperties()

        assertThat(schemas).isNotNull
        assertThat(schemas).hasSize(2)
    }

    @Test
    fun `getAllProperties should return an empty set if no properties are specified`() {
        val api = OpenAPI()

        val schemas = api.getAllProperties()

        assertThat(schemas).isNotNull
        assertThat(schemas).isEmpty()
    }

    @Test
    fun `getAllParameters should return components parameters`() {
        val api = OpenAPI().apply {
            components = Components().apply {
                parameters = mapOf("pet" to Parameter().apply {
                    name = "name"
                })
            }
        }

        val schemas = api.getAllParameters()

        assertThat(schemas).isNotNull
        assertThat(schemas).hasSize(1)
    }

    @Test
    fun `getAllParameters should return paths parameters`() {
        val api = OpenAPI().apply {
            val paths = Paths()
            val pathItem = PathItem()
            pathItem.parameters = listOf(Parameter().apply {
                name = "name"
            })
            paths.addPathItem("path", pathItem)
            this.paths = paths
        }

        val schemas = api.getAllParameters()

        assertThat(schemas).isNotNull
        assertThat(schemas).hasSize(1)
    }

    @Test
    fun `getAllParameters should return path operation parameters`() {
        val api = OpenAPI().apply {
            val paths = Paths()
            val pathItem = PathItem()
            pathItem.get = Operation().apply {
                parameters = listOf(Parameter().apply {
                    name = "name"
                })
            }
            paths.addPathItem("path", pathItem)
            this.paths = paths
        }

        val schemas = api.getAllParameters()

        assertThat(schemas).isNotNull
        assertThat(schemas).hasSize(1)
    }

    @Test
    fun `getAllParameters should return an empty map if no parameters are specified`() {
        val api = OpenAPI()

        val schemas = api.getAllParameters()

        assertThat(schemas).isNotNull
        assertThat(schemas).isEmpty()
    }

    @Test
    fun `getAllTransitiveSchemas should be able to cope with recursive schemas`() {
        @Language("YAML")
        val spec = """
openapi: 3.0.1
paths:
  /products/{product_id}:
    get:
      responses:
        200:
          content:
            application/json:
              schema:
                ${'$'}ref: "#/components/schemas/FacetGetResponse"
components:
  schemas:
    FacetGetResponse:
      allOf:
        - ${'$'}ref: "#/components/schemas/ProductResource"
    ProductResource:
      allOf:
        - type: object
          properties:
            children:
              type: array
              items:
                ${'$'}ref: "#/components/schemas/ProductResource"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val schemas = context.api.getAllTransitiveSchemas()

        // the schema is the same
        assertThat(schemas).hasSize(1)
    }
}
