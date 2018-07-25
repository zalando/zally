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
        assertThat(recorder.pointer).hasToString("/info/title")

        val description = specProxy.paths?.get("/tests")?.get?.responses?.get("200")?.description
        assertThat(description).isEqualTo("OK")
        assertThat(recorder.pointer).hasToString("/paths/~1tests/get/responses/200/description")
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
        assertThat(recorder.pointer).hasToString("/info/contact/name")

        val description = specProxy.paths?.get("/null")?.get?.responses?.get("200")?.description
        assertThat(description).isNull()
        assertThat(recorder.pointer).hasToString("/paths/~1null/get/responses/200/description")
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
        assertThat(skipRecorder.pointer).hasToString("/info/x-audience")

        val recorder = MethodCallRecorder(spec)
        val specProxy = recorder.proxy

        val audience = specProxy.info?.vendorExtensions?.get("x-audience")
        assertThat(audience).isEqualTo("component-internal")
        assertThat(recorder.pointer).hasToString("/info/vendorExtensions/x-audience")
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

    @Test
    fun `ignores non-traversal method`() {
        val content = """
            swagger: '2.0'
            info:
              title: Things API
            """.trimIndent()
        val spec = SwaggerParser().parse(content)
        val recorder = MethodCallRecorder(spec)

        recorder.proxy.info
        assertThat(recorder.pointer).hasToString("/info")

        recorder.proxy.info.toString()
        assertThat(recorder.pointer).hasToString("/info")
    }
}
