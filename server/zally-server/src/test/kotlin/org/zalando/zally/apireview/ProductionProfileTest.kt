package org.zalando.zally.apireview

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalManagementPort
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.zalando.zally.Application
import org.zalando.zally.util.RawHttp
import org.zalando.zally.util.StubTokenInfoServer

/**
 * Starts the application with the `production` profile, i.e. with the real OAuth resource server configuration that
 * every other test replaces with `permitAll`. Token validation goes to [StubTokenInfoServer].
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [Application::class, RestApiTestConfiguration::class],
)
@ActiveProfiles("production", "limited-rules")
abstract class ProductionProfileTest {
    @LocalServerPort
    private var port = 0

    @LocalManagementPort
    private var managementPort = 0

    protected fun get(
        path: String,
        token: String? = null,
    ) = send("GET", path, token)

    protected fun managementGet(
        path: String,
        token: String? = null,
    ) = send("GET", path, token, port = managementPort)

    protected fun send(
        method: String,
        path: String,
        token: String? = null,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
        port: Int = this.port,
    ) = RawHttp.send(port, method, path, token, body, headers = headers)

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("TOKEN_INFO_URI") { StubTokenInfoServer.url }
            registry.add("management.server.port") { "0" }
        }
    }
}
