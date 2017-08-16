package de.zalando.zally.github

import de.zalando.zally.github.util.SecurityUtil
import net.jadler.JadlerMocker
import net.jadler.stubbing.server.jdk.JdkStubHttpServer
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
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
        lateinit var githubMock: GithubMock
        lateinit var zallyMock: ZallyMock
        @BeforeClass
        @JvmStatic
        fun setup() {
            githubMock = GithubMock(JadlerMocker(JdkStubHttpServer(8088)))
            githubMock.start()

            githubMock.onRequest()
                    .havingMethodEqualTo("GET")
                    .havingPathEqualTo("/user")
                    .respond()
                    .withStatus(OK.value())
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("json/github-user-response.json".loadResource())

            zallyMock = ZallyMock(JadlerMocker(JdkStubHttpServer(9099)))
            zallyMock.start()
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            githubMock.close()
            zallyMock.close()
        }
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Value("\${github.secret}")
    lateinit var secret: String

    @Before
    fun setUp() {
        githubMock.reset()
        zallyMock.reset()
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

        val response = restTemplate.postForEntity("/api-validation", HttpEntity(body, headers), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        githubMock.verifyThatRequest()
                .havingMethodEqualTo("POST")
                .havingPath(Matchers.containsString("/statuses/"))
                .receivedNever()
    }

    @Test
    fun shouldSetFailedStatusOnMissingZallyConfigurationFile() {
        githubMock.mockGet(
                "/repos/myUserName/zally/git/trees/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-tree-missing-zally-yaml.json")

        githubMock.mockPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-commit-status-change.json")

        val body = "json/github-webhook-pullrequest.json".loadResource()
        val response = restTemplate.postForEntity("/api-validation", webhookRequest(body), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        githubMock.verifyPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                Matchers.containsString("error"))
    }

    @Test
    fun shouldSetFailedStatusOnMissingSwaggerFile() {
        githubMock.mockGet(
                "/repos/myUserName/zally/git/trees/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-tree-missing-swagger-file.json")

        githubMock.mockPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-commit-status-change.json")

        githubMock.mockGetBlob(
                "/repos/myUserName/zally/git/blobs/bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                "json/github-zally-yaml-blob.yaml")

        val body = "json/github-webhook-pullrequest.json".loadResource()
        val response = restTemplate.postForEntity("/api-validation", webhookRequest(body), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        githubMock.verifyPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                Matchers.containsString("error"))
    }

    @Test
    fun shouldSetStatusSuccessOnValidZallyResponse() {
        githubMock.mockGet(
                "/repos/myUserName/zally/git/trees/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-tree.json")

        githubMock.mockGetBlob(
                "/repos/myUserName/zally/git/blobs/bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                "json/github-zally-yaml-blob.yaml")

        githubMock.mockGetBlob(
                "/repos/myUserName/zally/git/blobs/cccccccccccccccccccccccccccccccccccccccc",
                "json/github-api-yaml-blob.yaml")

        zallyMock.mockPost(
                "/api-violations",
                "json/zally-success-response.json")

        githubMock.mockPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-commit-status-change.json")

        val body = "json/github-webhook-pullrequest.json".loadResource()
        val response = restTemplate.postForEntity("/api-validation", webhookRequest(body), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        zallyMock.verifyPost(
                "/api-violations",
                Matchers.containsString("Zalando's API Linter"))

        githubMock.verifyPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                Matchers.containsString("success"))
    }

    @Test
    fun shouldSetStatusErrorIfZallyResponseContainsMustViolations() {
        githubMock.mockGet(
                "/repos/myUserName/zally/git/trees/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-tree.json")

        githubMock.mockGetBlob(
                "/repos/myUserName/zally/git/blobs/bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                "json/github-zally-yaml-blob.yaml")

        githubMock.mockGetBlob(
                "/repos/myUserName/zally/git/blobs/cccccccccccccccccccccccccccccccccccccccc",
                "json/github-api-yaml-blob.yaml")

        zallyMock.mockPost(
                "/api-violations",
                "json/zally-error-response.json")

        githubMock.mockPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-commit-status-change.json")

        val body = "json/github-webhook-pullrequest.json".loadResource()

        val response = restTemplate.postForEntity("/api-validation", webhookRequest(body), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        zallyMock.verifyPost(
                "/api-violations",
                Matchers.containsString("Zalando's API Linter"))

        githubMock.verifyPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                Matchers.containsString("error"))
    }

    private fun webhookRequest(body: String): HttpEntity<String> {
        return HttpEntity(body, webhookHeaders(body))
    }

    private fun webhookHeaders(body: String): HttpHeaders {
        return HttpHeaders().apply {
            add("X-GitHub-Event", "pull_request")
            add("X-Hub-Signature", SecurityUtil.sign(secret, body))
        }
    }

}