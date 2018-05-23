package de.zalando.zally.util.ast

import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MethodCallRecorderTest {
    @Test
    fun `with non-null path`() {
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
        val recorder = MethodCallRecorder(spec)
        val specProxy = recorder.proxy

        val title = specProxy.info?.title
        assertThat(title).isEqualTo("Things API")
        assertThat(recorder.pointer).isEqualTo("#/info/title")

        val description = specProxy.paths?.get("/tests")?.get?.responses?.get("200")?.description
        assertThat(description).isEqualTo("OK")
        assertThat(recorder.pointer).isEqualTo("#/paths/~1tests/get/responses/200/description")
    }

    @Test
    fun `with null path`() {
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
        val recorder = MethodCallRecorder(spec)
        val specProxy = recorder.proxy

        val contactName = specProxy.info?.contact?.name
        assertThat(contactName).isNull()
        assertThat(recorder.pointer).isEqualTo("#/info/contact/name")

        val description = specProxy.paths?.get("/null")?.get?.responses?.get("200")?.description
        assertThat(description).isNull()
        assertThat(recorder.pointer).isEqualTo("#/paths/~1null/get/responses/200/description")
    }

    @Test
    fun `with ignore method`() {
        val content = """
            swagger: '2.0'
            info:
              title: Things API
              description: Description of things
              version: '1.0.0'
              x-audience: component-internal
            paths: {}
            """.trimIndent()

        val spec = SwaggerParser().parse(content)

        val skipRecorder = MethodCallRecorder(spec).skipMethods("getExtensions", "getVendorExtensions")
        val skipSpecProxy = skipRecorder.proxy

        val skipAudience = skipSpecProxy.info?.vendorExtensions?.get("x-audience")
        assertThat(skipAudience).isEqualTo("component-internal")
        assertThat(skipRecorder.pointer).isEqualTo("#/info/x-audience")

        val recorder = MethodCallRecorder(spec)
        val specProxy = recorder.proxy

        val audience = specProxy.info?.vendorExtensions?.get("x-audience")
        assertThat(audience).isEqualTo("component-internal")
        assertThat(recorder.pointer).isEqualTo("#/info/vendorExtensions/x-audience")
    }

    @Test
    fun `get non-existing extension`() {
        val content = """
            swagger: '2.0'
            info:
              title: Things API
              description: Description of things
              version: '1.0.0'
              x-audience: component-internal
            paths: {}
            """.trimIndent()

        val spec = SwaggerParser().parse(content)

        val recorder = MethodCallRecorder(spec).skipMethods("getExtensions", "getVendorExtensions")
        val specProxy = recorder.proxy

        val apiId = specProxy.info?.vendorExtensions?.get("x-api-id")
        assertThat(apiId).isNull()
    }
}
