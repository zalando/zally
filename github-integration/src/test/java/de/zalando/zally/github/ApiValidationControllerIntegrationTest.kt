package de.zalando.zally.github

import de.zalando.zally.github.util.SecurityUtil
import net.jadler.JadlerMocker
import net.jadler.stubbing.server.jdk.JdkStubHttpServer
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.*
import org.junit.Assert.assertThat
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = arrayOf(Application::class))
@ActiveProfiles("test")
class ApiValidationControllerIntegrationTest {
    companion object {
        lateinit var mocker: JadlerMocker;
        @BeforeClass @JvmStatic fun setup() {
            mocker = JadlerMocker(JdkStubHttpServer(8088))
            mocker.start()
            
            mocker.onRequest()
                    .havingMethodEqualTo("GET")
                    .havingPathEqualTo("/user")
                    .respond()
                    .withStatus(OK.value())
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("json/github-user-response.json".loadResource())
        }

        @AfterClass @JvmStatic fun teardown() {
            mocker.close()
        }
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Value("\${zally.secret}")
    lateinit var secret: String

    @Before
    fun setUp() {
        mocker.reset()
    }

    @After
    fun tearDown() {
    }

    @Test
    fun shouldIgnoreUnsupportedEvent() {
        val body = "{}"

        val headers = HttpHeaders().apply {
            add("X-GitHub-Event", "ping")
            add("X-Hub-Signature", SecurityUtil.sign(secret, body))
        }

        val response = restTemplate.postForEntity("/api-validation",  HttpEntity(body, headers), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        mocker.verifyThatRequest()
                .havingMethodEqualTo("POST")
                .havingPath(Matchers.containsString("/statuses/"))
                .receivedNever()
    }

    @Test
    fun shouldSetFailedStatusOnMissingZallyConfigurationFile() {
        mocker.onRequest()
                .havingMethodEqualTo("GET")
                .havingPathEqualTo("/repos/myUserName/zally/git/trees/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                .respond()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("json/github-tree-missing-zally-yaml.json".loadResource())

        mocker.onRequest()
                .havingMethodEqualTo("POST")
                .havingPathEqualTo("/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                .respond()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("json/github-commit-status-change.json".loadResource())

        val body = "json/github-webhook-pullrequest.json".loadResource()

        val headers = HttpHeaders().apply {
            add("X-GitHub-Event", "pull_request")
            add("X-Hub-Signature", SecurityUtil.sign(secret, body))
        }

        val response = restTemplate.postForEntity("/api-validation",  HttpEntity(body, headers), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        mocker.verifyThatRequest()
                .havingMethodEqualTo("POST")
                .havingPathEqualTo("/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                .receivedOnce()
    }

    @Test
    fun shouldSetFailedStatusOnMissingSwaggerFile() {
        mocker.onRequest()
                .havingMethodEqualTo("GET")
                .havingPathEqualTo("/repos/myUserName/zally/git/trees/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                .respond()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("json/github-tree-missing-swagger-file.json".loadResource())

        mocker.onRequest()
                .havingMethodEqualTo("POST")
                .havingPathEqualTo("/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                .respond()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("json/github-commit-status-change.json".loadResource())

        mocker.onRequest()
                .havingMethodEqualTo("GET")
                .havingHeaderEqualTo("Accept", "application/vnd.github.VERSION.raw")
                .havingPathEqualTo("/repos/myUserName/zally/git/blobs/bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
                .respond()
                .withStatus(OK.value())
                .withHeader(CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .withBody("json/github-zally-yaml-blob.yaml".loadResource())

        val body = "json/github-webhook-pullrequest.json".loadResource()

        val headers = HttpHeaders().apply {
            add("X-GitHub-Event", "pull_request")
            add("X-Hub-Signature", SecurityUtil.sign(secret, body))
        }

        val response = restTemplate.postForEntity("/api-validation",  HttpEntity(body, headers), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        mocker.verifyThatRequest()
                .havingMethodEqualTo("POST")
                .havingPathEqualTo("/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                .receivedOnce()
    }
}