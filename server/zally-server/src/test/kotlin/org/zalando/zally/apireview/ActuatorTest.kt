package org.zalando.zally.apireview

import tools.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

/**
 * The actuator runs on its own port (`management.server.port`, 7979 by default) with base path `/`, exposing exactly
 * `health` and `metrics`. Deployments and the docker-compose health check depend on this.
 */
class ActuatorTest : HttpContractTest() {
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `health is up at the root of the management port`() {
        val response = send("GET", "/health", management = true)

        assertThat(response.status).isEqualTo(200)
        assertThat(objectMapper.readTree(response.body).path("status").asString()).isEqualTo("UP")
    }

    @Test
    fun `metrics lists the meter names and serves a single meter`() {
        val names = objectMapper.readTree(send("GET", "/metrics", management = true).body).path("names")
        assertThat(names.values().map { it.asString() }).contains("jvm.memory.used", "http.server.requests")

        val meter = send("GET", "/metrics/jvm.memory.used", management = true)
        assertThat(meter.status).isEqualTo(200)
        assertThat(objectMapper.readTree(meter.body).path("name").asString()).isEqualTo("jvm.memory.used")
    }

    @Test
    fun `only health and metrics are exposed`() {
        listOf("/info", "/env", "/beans", "/prometheus", "/actuator/health").forEach {
            assertThat(send("GET", it, management = true).status).describedAs(it).isEqualTo(404)
        }
    }

    @Test
    fun `actuator endpoints are not served on the application port`() {
        assertThat(send("GET", "/health").status).isEqualTo(404)
        assertThat(send("GET", "/metrics").status).isEqualTo(404)
    }
}
