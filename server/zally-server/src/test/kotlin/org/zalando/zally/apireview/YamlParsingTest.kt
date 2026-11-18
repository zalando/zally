package org.zalando.zally.apireview

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.junit.jupiter.api.extension.ExtendWith

/**
 * A YAML definition must be parsed by swagger-parser itself. With snakeyaml 2 and an old swagger-parser the parser logs
 * `Error snake-parsing yaml content` and only works through a fallback, so every YAML review is noisy.
 */
@ExtendWith(OutputCaptureExtension::class)
class YamlParsingTest : HttpContractTest() {
    @Test
    fun `a yaml definition is parsed without falling back`(output: CapturedOutput) {
        val yaml = "openapi: 3.0.0\\ninfo:\\n  title: Smoke\\n  version: 1.0.0\\npaths: {}\\n"

        val response = send("POST", "/api-violations", """{"api_definition_string":"$yaml"}""")

        assertThat(response.status).isEqualTo(200)
        assertThat(output.all).doesNotContain("Error snake-parsing yaml content")
    }
}
