package org.zalando.zally.apireview

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.zalando.zally.util.RawHttp

/**
 * Pins the shape of error responses, which clients (the web UI reads `detail`) depend on: exactly the fields `title`,
 * `status` and `detail`, sent as `application/problem+json`. The wording of `detail` is framework text and is covered
 * by the golden master instead.
 */
class ErrorBodyTest : HttpContractTest() {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @ParameterizedTest(name = "{0} {1} -> {2}")
    @CsvSource(
        "POST, /api-violations, 400",
        "GET,  /api-violations/not-a-uuid, 400",
        "GET,  /supported-rules?type=bogus, 400",
        "GET,  /review-statistics?from=yesterday, 400",
        "GET,  /review-statistics?to=2000-01-31, 400",
        "GET,  /review-statistics?from=2999-01-01, 400",
        "PUT,  /api-violations/6f1f1b1e-9a38-4c7e-8d0e-111111111111, 405",
        "POST, /supported-rules, 405",
    )
    fun `errors have title, status and detail`(
        method: String,
        path: String,
        status: Int,
    ) {
        val response = send(method, path, body = if (method == "GET") null else "{")

        assertProblem(response, status)
        assertThat(fields(response)).containsExactlyInAnyOrder("title", "status", "detail")
        assertThat(json(response).path("detail").asText()).isNotBlank()
    }

    @Test
    fun `unknown review is not found without detail`() {
        val response = send("GET", "/api-violations/6f1f1b1e-9a38-4c7e-8d0e-111111111111")

        assertProblem(response, 404)
        assertThat(fields(response)).containsExactlyInAnyOrder("title", "status")
        assertThat(json(response).path("title").asText()).isEqualTo("Not Found")
    }

    @Test
    fun `missing api definition is a bad request with the exception message`() {
        val response = send("POST", "/api-violations", body = """{"my_api":"dummy"}""")

        assertProblem(response, 400)
        assertThat(json(response).path("title").asText()).isEqualTo("Bad Request")
        assertThat(json(response).path("detail").asText()).isEqualTo("An api definition is missing in api_definition field")
    }

    @Test
    fun `inaccessible url keeps the status of the exception`() {
        val response = send("POST", "/api-violations", body = """{"api_definition_url":"http://remote-localhost/test.yaml"}""")

        assertProblem(response, 404)
        assertThat(fields(response)).containsExactlyInAnyOrder("title", "status", "detail")
        assertThat(json(response).path("detail").asText())
            .isEqualTo("Unknown host while retrieving api definition url: remote-localhost")
    }

    @Test
    fun `unmapped route keeps the default error body`() {
        val response = send("GET", "/unknown")

        assertThat(response.status).isEqualTo(404)
        assertThat(response.contentType).isEqualTo("application/json")
        assertThat(fields(response)).containsExactlyInAnyOrder("timestamp", "status", "error", "path")
    }

    private fun assertProblem(
        response: RawHttp.Response,
        status: Int,
    ) {
        assertThat(response.status).isEqualTo(status)
        assertThat(response.contentType).isEqualTo("application/problem+json")
        assertThat(json(response).path("status").asInt()).isEqualTo(status)
    }

    private fun json(response: RawHttp.Response) = objectMapper.readTree(response.body)

    private fun fields(response: RawHttp.Response): List<String> = json(response).fieldNames().asSequence().toList()
}
