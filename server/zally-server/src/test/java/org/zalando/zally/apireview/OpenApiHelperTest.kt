package org.zalando.zally.apireview

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OpenApiHelperTest {

    @Test
    fun shouldParseApiTitle() {
        val content = """
            openapi: 3.0.1
            info:
              title: Awesome API
            """.trimIndent()

        val title = OpenApiHelper.extractApiName(content)

        assertThat(title).isEqualTo("Awesome API")
    }

    @Test
    fun shouldReturnNullIfTitleIsNotSet() {
        val content = ""

        val title = OpenApiHelper.extractApiName(content)

        assertThat(title).isNull()
    }

    @Test
    fun shouldParseApiId() {
        val content = """
            openapi: 3.0.1
            info:
              x-api-id: 48aa0090-25ef-11e8-b467-0ed5f89f718b
            """.trimIndent()

        val apiId = OpenApiHelper.extractApiId(content)

        assertThat(apiId).isEqualTo("48aa0090-25ef-11e8-b467-0ed5f89f718b")
    }

    @Test
    fun shouldReturnNullIfApiIdIsNotSet() {
        val content = ""

        val apiId = OpenApiHelper.extractApiId(content)

        assertThat(apiId).isNull()
    }
}
