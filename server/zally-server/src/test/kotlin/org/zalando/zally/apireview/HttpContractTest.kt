package org.zalando.zally.apireview

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalManagementPort
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.zalando.zally.Application
import org.zalando.zally.util.RawHttp

/**
 * Base for tests that look at the HTTP responses of the whole application as a client does, on the `test` profile
 * (security is `permitAll`; the `production` profile has its own tests). Shares its Spring context with
 * [RestApiBaseTest].
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [Application::class, RestApiTestConfiguration::class],
)
@ActiveProfiles("test", "limited-rules")
abstract class HttpContractTest {
    @LocalServerPort
    private var port = 0

    @LocalManagementPort
    private var managementPort = 0

    @Autowired
    private lateinit var apiReviewRepository: ApiReviewRepository

    @BeforeEach
    fun cleanDatabase() {
        apiReviewRepository.deleteAll()
    }

    protected fun send(
        method: String,
        path: String,
        body: String? = null,
        contentType: String? = "application/json",
        accept: String? = null,
        headers: Map<String, String> = emptyMap(),
        management: Boolean = false,
    ): RawHttp.Response =
        RawHttp.send(
            port = if (management) managementPort else port,
            method = method,
            path = path,
            body = body,
            contentType = contentType,
            accept = accept,
            headers = headers,
        )

    protected fun assertThatBody(response: RawHttp.Response) = assertThat(response.body)

    companion object {
        const val TINY_API = """{"openapi":"3.0.0","info":{"title":"Tiny","version":"1.0.0"},"paths":{}}"""
    }
}
