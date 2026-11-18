package org.zalando.zally.apireview

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.zalando.zally.util.RawHttp
import java.io.File

/**
 * Golden master of the HTTP surface: status, selected headers and body of one request per interesting case, for the
 * success paths as well as every error the server can produce.
 *
 * The recorded file is `src/test/resources/golden/http-contract.txt`. A framework upgrade that changes an answer fails
 * this test; whoever upgrades then regenerates the file with `GOLDEN_UPDATE=true ./gradlew :zally-server:test --tests
 * '*GoldenMasterTest'` and the diff of the file in the pull request is the list of behaviour changes to approve.
 *
 * Volatile values (ids, timestamps, ports) are replaced by placeholders, see [normalize].
 */
class GoldenMasterTest : HttpContractTest() {
    private class Case(
        val name: String,
        val method: String,
        val path: String,
        val body: String? = null,
        val contentType: String? = "application/json",
        val accept: String? = null,
        val headers: Map<String, String> = emptyMap(),
        val management: Boolean = false,
        val omitBody: Boolean = false,
    )

    private val cases =
        listOf(
            // --- POST /api-violations
            Case("post json definition", "POST", "/api-violations", body = """{"api_definition":$TINY_API}"""),
            Case("post string definition", "POST", "/api-violations", body = """{"api_definition_string":${TINY_API.quoted()}}"""),
            Case("post accept yaml", "POST", "/api-violations", body = """{"api_definition":$TINY_API}""", accept = "application/x-yaml"),
            Case("post accept text yaml", "POST", "/api-violations", body = """{"api_definition":$TINY_API}""", accept = "text/x-yaml"),
            Case("post accept json", "POST", "/api-violations", body = """{"api_definition":$TINY_API}""", accept = "application/json"),
            Case("post accept anything", "POST", "/api-violations", body = """{"api_definition":$TINY_API}""", accept = "*/*"),
            Case("post accept html", "POST", "/api-violations", body = """{"api_definition":$TINY_API}""", accept = "text/html"),
            Case("post trailing slash", "POST", "/api-violations/", body = """{"api_definition":$TINY_API}"""),
            Case("post yaml suffix", "POST", "/api-violations.yaml", body = """{"api_definition":$TINY_API}"""),
            Case("post json suffix", "POST", "/api-violations.json", body = """{"api_definition":$TINY_API}"""),
            Case("post ignore rules", "POST", "/api-violations", body = """{"api_definition":$TINY_API,"ignore_rules":["TestCheckIsOpenApi3"]}"""),
            Case("post unparsable definition", "POST", "/api-violations", body = """{"api_definition":"no swagger definition"}"""),
            Case("post with user agent", "POST", "/api-violations", body = """{"api_definition":$TINY_API}""", headers = mapOf("User-Agent" to "unirest-java/1.3.11")),
            Case("post unknown fields are ignored", "POST", "/api-violations", body = """{"api_definition":$TINY_API,"unknown":1}"""),
            // --- POST /api-violations: errors
            Case("post malformed json", "POST", "/api-violations", body = """{"api_definition": """),
            Case("post empty body", "POST", "/api-violations", body = ""),
            Case("post json array", "POST", "/api-violations", body = "[]"),
            Case("post no definition", "POST", "/api-violations", body = """{"my_api":"dummy"}"""),
            Case("post wrong type", "POST", "/api-violations", body = """{"api_definition_url":42,"ignore_rules":"x"}"""),
            Case("post unknown host", "POST", "/api-violations", body = """{"api_definition_url":"http://remote-localhost/test.yaml"}"""),
            Case("post invalid url", "POST", "/api-violations", body = """{"api_definition_url":"not a url"}"""),
            Case("post text plain", "POST", "/api-violations", body = TINY_API, contentType = "text/plain"),
            Case("post no content type", "POST", "/api-violations", body = TINY_API, contentType = null),
            Case("post yaml content type", "POST", "/api-violations", body = "openapi: 3.0.0", contentType = "application/x-yaml"),
            // --- GET /api-violations/{id}
            Case("get review", "GET", "/api-violations/{id}"),
            Case("get review trailing slash", "GET", "/api-violations/{id}/"),
            Case("get review accept yaml", "GET", "/api-violations/{id}", accept = "application/x-yaml"),
            Case("get review yaml suffix", "GET", "/api-violations/{id}.yaml"),
            Case("get review json suffix", "GET", "/api-violations/{id}.json"),
            Case("get review unknown id", "GET", "/api-violations/6f1f1b1e-9a38-4c7e-8d0e-111111111111"),
            Case("get review invalid id", "GET", "/api-violations/not-a-uuid"),
            Case("get review accept html", "GET", "/api-violations/{id}", accept = "text/html"),
            Case("get api-violations without id", "GET", "/api-violations"),
            Case("get api-violations trailing slash", "GET", "/api-violations/"),
            Case("put review", "PUT", "/api-violations/{id}", body = "{}"),
            Case("delete review", "DELETE", "/api-violations/{id}"),
            Case("head review", "HEAD", "/api-violations/{id}"),
            Case("options review", "OPTIONS", "/api-violations/{id}"),
            // --- /supported-rules
            Case("rules", "GET", "/supported-rules"),
            Case("rules trailing slash", "GET", "/supported-rules/"),
            Case("rules accept yaml", "GET", "/supported-rules", accept = "application/x-yaml"),
            Case("rules accept text yaml", "GET", "/supported-rules", accept = "text/x-yaml"),
            Case("rules yaml suffix", "GET", "/supported-rules.yaml"),
            Case("rules json suffix", "GET", "/supported-rules.json"),
            Case("rules accept html", "GET", "/supported-rules", accept = "text/html"),
            Case("rules accept xml", "GET", "/supported-rules", accept = "application/xml"),
            Case("rules type filter", "GET", "/supported-rules?type=MUST"),
            Case("rules type filter lower case", "GET", "/supported-rules?type=must"),
            Case("rules invalid type", "GET", "/supported-rules?type=bogus"),
            Case("rules inactive", "GET", "/supported-rules?is_active=false"),
            Case("rules invalid is_active", "GET", "/supported-rules?is_active=maybe"),
            Case("rules post", "POST", "/supported-rules", body = "{}"),
            Case("rules options", "OPTIONS", "/supported-rules"),
            Case("rules preflight", "OPTIONS", "/supported-rules", headers = mapOf("Origin" to "https://example.org", "Access-Control-Request-Method" to "GET")),
            // --- /review-statistics
            Case("statistics", "GET", "/review-statistics"),
            Case("statistics trailing slash", "GET", "/review-statistics/"),
            Case("statistics yaml suffix", "GET", "/review-statistics.yaml"),
            Case("statistics accept yaml", "GET", "/review-statistics", accept = "application/x-yaml"),
            Case("statistics from", "GET", "/review-statistics?from=2000-01-01"),
            Case("statistics from and to", "GET", "/review-statistics?from=2000-01-01&to=2000-01-31"),
            Case("statistics user agent", "GET", "/review-statistics?user_agent=unirest"),
            Case("statistics empty user agent", "GET", "/review-statistics?user_agent="),
            Case("statistics to without from", "GET", "/review-statistics?to=2000-01-31"),
            Case("statistics from in the future", "GET", "/review-statistics?from=2999-01-01"),
            Case("statistics invalid date", "GET", "/review-statistics?from=yesterday"),
            Case("statistics invalid to", "GET", "/review-statistics?from=2000-01-01&to=31.01.2000"),
            Case("statistics post", "POST", "/review-statistics", body = "{}"),
            // --- twintip schema discovery
            Case("api", "GET", "/api"),
            Case("api trailing slash", "GET", "/api/", omitBody = true),
            Case("api accept json", "GET", "/api", accept = "application/json", omitBody = true),
            Case("api accept yaml", "GET", "/api", accept = "application/x-yaml"),
            Case("schema discovery", "GET", "/.well-known/schema-discovery"),
            Case("schema discovery accept yaml", "GET", "/.well-known/schema-discovery", accept = "application/x-yaml"),
            // --- unmapped
            Case("unknown path", "GET", "/unknown"),
            Case("unknown path post", "POST", "/unknown", body = "{}"),
            Case("unknown path accept yaml", "GET", "/unknown", accept = "application/x-yaml"),
            Case("root", "GET", "/"),
            Case("health on the main port", "GET", "/health"),
            Case("metrics on the main port", "GET", "/metrics"),
            // --- actuator on the management port
            Case("management health", "GET", "/health", management = true),
            Case("management health trailing slash", "GET", "/health/", management = true),
            Case("management health accept yaml", "GET", "/health", accept = "application/x-yaml", management = true),
            Case("management metrics", "GET", "/metrics", management = true, omitBody = true),
            Case("management metric", "GET", "/metrics/jvm.memory.used", management = true, omitBody = true),
            Case("management unknown metric", "GET", "/metrics/no.such.metric", management = true),
            Case("management info", "GET", "/info", management = true),
            Case("management env", "GET", "/env", management = true),
            Case("management unknown", "GET", "/unknown", management = true),
            Case("management api", "GET", "/api", management = true),
            Case("management health post", "POST", "/health", body = "{}", management = true),
        )

    @Test
    fun `http responses match the golden master`() {
        assertThat(cases.map { it.name }).describedAs("case names must be unique").doesNotHaveDuplicates()
        val actual = record()

        if (System.getenv("GOLDEN_UPDATE") == "true") {
            // A left-over variable would make this test pass whatever the server answers.
            assertThat(System.getenv("CI")).describedAs("GOLDEN_UPDATE must not be set on CI").isNull()
            GOLDEN_FILE.parentFile.mkdirs()
            GOLDEN_FILE.writeText(actual.values.joinToString("\n"))
            return
        }

        val expected = blocks(GOLDEN_FILE.takeIf { it.exists() }?.readText().orEmpty())
        val differences =
            (actual.keys + expected.keys).distinct().mapNotNull { name ->
                when {
                    name !in expected -> "NEW      $name\n${actual.getValue(name)}"
                    name !in actual -> "MISSING  $name"
                    actual[name] != expected[name] -> "CHANGED  $name\n--- expected\n${expected[name]}\n--- actual\n${actual[name]}"
                    else -> null
                }
            }

        assertThat(differences)
            .describedAs("differences from ${GOLDEN_FILE.path} (regenerate with GOLDEN_UPDATE=true)")
            .isEmpty()
    }

    private fun record(): Map<String, String> {
        val id =
            ID_PATTERN
                .find(
                    send("POST", "/api-violations", """{"api_definition":$TINY_API}""", headers = mapOf("User-Agent" to "golden-master")).body,
                )!!
                .value

        return cases.associate { case ->
            val response =
                send(
                    method = case.method,
                    path = case.path.replace("{id}", id),
                    body = case.body,
                    contentType = case.contentType,
                    accept = case.accept,
                    headers = case.headers,
                    management = case.management,
                )
            case.name to describe(case, response).trimEnd() + "\n"
        }
    }

    private fun describe(
        case: Case,
        response: RawHttp.Response,
    ): String =
        buildString {
            appendLine("### ${case.name}")
            append("> ${case.method} ${case.path}")
            if (case.management) append("  [management port]")
            appendLine()
            case.accept?.let { appendLine("> Accept: $it") }
            case.headers.forEach { (name, value) -> appendLine("> $name: $value") }
            if (case.body != null) {
                appendLine("> Content-Type: ${case.contentType}")
                appendLine("> body: ${case.body}")
            } else if (case.contentType == null) {
                appendLine("> (no content type)")
            }
            appendLine("< ${response.status}")
            HEADERS.forEach { name ->
                response.headers[name]?.let { appendLine("< $name: ${normalize(it.joinToString(", "))}") }
            }
            if (case.omitBody) {
                appendLine("< body: <omitted, ${if (response.body.startsWith("{")) "json object" else "other"}>")
            } else {
                appendLine("< body: ${normalize(response.body)}")
            }
        }

    private fun blocks(text: String): Map<String, String> =
        text.split("\n### ").filter { it.isNotBlank() }.associate {
            val block = "### " + it.removePrefix("### ").trimEnd() + "\n"
            block.lineSequence().first().removePrefix("### ") to block
        }

    private fun normalize(value: String): String =
        value
            .replace(UUID_PATTERN, "<uuid>")
            .replace(Regex("\"timestamp\":\"[^\"]+\""), "\"timestamp\":\"<timestamp>\"")
            .replace(Regex("timestamp: \"?[^\\n\"]+\"?"), "timestamp: <timestamp>")
            .replace(Regex("localhost:\\d+"), "localhost:<port>")

    companion object {
        private val GOLDEN_FILE = File("src/test/resources/golden/http-contract.txt")
        private val UUID_PATTERN = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
        private val ID_PATTERN = Regex("(?<=\"external_id\":\")[0-9a-f-]{36}")

        private val HEADERS =
            listOf(
                "content-type",
                "allow",
                "location",
                "www-authenticate",
                "vary",
                "content-disposition",
                "access-control-allow-origin",
                "access-control-allow-methods",
                "access-control-allow-headers",
                "access-control-max-age",
            )

        private fun String.quoted() = "\"" + replace("\"", "\\\"") + "\""
    }
}
