package de.zalando.zally.integration

import de.zalando.zally.integration.github.SecurityUtil
import de.zalando.zally.integration.mock.EmbeddedPostgresqlConfiguration
import de.zalando.zally.integration.mock.GithubMock
import de.zalando.zally.integration.mock.JadlerRule
import de.zalando.zally.integration.mock.ZallyMock
import net.jadler.JadlerMocker
import net.jadler.stubbing.server.jdk.JdkStubHttpServer
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringRunner
import uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = [Application::class, EmbeddedPostgresqlConfiguration::class])
@ActiveProfiles("test")
@Sql("/sql/cleanup-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ValidationControllerIntegrationTest {
    companion object {
        @ClassRule @JvmField val githubServer = JadlerRule(GithubMock(JadlerMocker(JdkStubHttpServer(8088)))) {
            it.mockGet("/user", "json/github-user-response.json") // required for app start
        }
        @ClassRule @JvmField val zallyServer = JadlerRule(ZallyMock(JadlerMocker(JdkStubHttpServer(9099))))
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Value("\${github.secret}")
    lateinit var secret: String

    @Before
    fun setUp() {
        githubServer.mock.reset()
        zallyServer.mock.reset()
    }

    @Test
    fun shouldIgnoreUnsupportedEvent() {
        val body = "{}"

        val headers = HttpHeaders().apply {
            add("X-GitHub-Event", "ping")
            add("X-Hub-Signature", SecurityUtil.sign(secret, body))
        }

        val response = restTemplate.postForEntity("/github-webhook", HttpEntity(body, headers), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.NOT_FOUND))
    }

    @Test
    fun shouldSetFailedStatusOnMissingZallyConfigurationFile() {
        githubServer.mock.mockGet(
                "/repos/myUserName/zally/git/trees/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-tree-missing-zally-yaml.json")

        githubServer.mock.mockPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-commit-status-change.json")

        val body = "json/github-webhook-pullrequest.json".loadResource()
        val response = restTemplate.postForEntity("/github-webhook", webhookRequest(body), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        githubServer.mock.verifyPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                sameJSONAs("""
                    {
                        "state": "error"
                    }
                """).allowingExtraUnexpectedFields())
    }

    @Test
    fun shouldSetFailedStatusOnMissingSwaggerFile() {
        githubServer.mock.mockGet(
                "/repos/myUserName/zally/git/trees/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-tree-missing-swagger-file.json")

        githubServer.mock.mockPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-commit-status-change.json")

        githubServer.mock.mockGetBlob(
                "/repos/myUserName/zally/git/blobs/bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                "json/github-zally-yaml-blob.yaml")

        val body = "json/github-webhook-pullrequest.json".loadResource()
        val response = restTemplate.postForEntity("/github-webhook", webhookRequest(body), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        githubServer.mock.verifyPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                sameJSONAs("""
                    {
                        "state": "error"
                    }
                """).allowingExtraUnexpectedFields())
    }

    @Test
    fun shouldSetStatusSuccessOnValidZallyResponse() {
        githubServer.mock.mockGet(
                "/repos/myUserName/zally/git/trees/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-tree.json")

        githubServer.mock.mockGetBlob(
                "/repos/myUserName/zally/git/blobs/bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                "json/github-zally-yaml-blob.yaml")

        githubServer.mock.mockGetBlob(
                "/repos/myUserName/zally/git/blobs/cccccccccccccccccccccccccccccccccccccccc",
                "json/github-api-yaml-blob.yaml")

        zallyServer.mock.mockPost(
                "/api-violations",
                "json/zally-success-response.json")

        githubServer.mock.mockPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-commit-status-change.json")

        githubServer.mock.mockGet(
                "/repos/myUserName/zally/pulls/1/files",
                "json/github-pull-files.json")

        val body = "json/github-webhook-pullrequest.json".loadResource()
        val response = restTemplate.postForEntity("/github-webhook", webhookRequest(body), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        zallyServer.mock.verifyPost(
                "/api-violations",
                sameJSONAs("""
                    {
                        "api_definition": {
                            "info": {
                                "description": "Zalando's API Linter"
                            }
                        }
                    }
                """).allowingExtraUnexpectedFields()
        )

        githubServer.mock.verifyPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                sameJSONAs("""
                    {
                        "state": "success"
                    }
                """).allowingExtraUnexpectedFields())
    }

    @Test
    fun shouldSetStatusErrorIfZallyResponseContainsMustViolations() {
        githubServer.mock.mockGet(
                "/repos/myUserName/zally/git/trees/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-tree.json")

        githubServer.mock.mockGetBlob(
                "/repos/myUserName/zally/git/blobs/bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                "json/github-zally-yaml-blob.yaml")

        githubServer.mock.mockGetBlob(
                "/repos/myUserName/zally/git/blobs/cccccccccccccccccccccccccccccccccccccccc",
                "json/github-api-yaml-blob.yaml")

        zallyServer.mock.mockPost(
                "/api-violations",
                "json/zally-error-response.json")

        githubServer.mock.mockPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                "json/github-commit-status-change.json")

        githubServer.mock.mockGet(
                "/repos/myUserName/zally/pulls/1/files",
                "json/github-pull-files.json")

        val body = "json/github-webhook-pullrequest.json".loadResource()

        val response = restTemplate.postForEntity("/github-webhook", webhookRequest(body), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        zallyServer.mock.verifyPost(
                "/api-violations",
                sameJSONAs("""
                    {
                        "api_definition": {
                            "info": {
                                "description": "Zalando's API Linter"
                            }
                        }
                    }
                """).allowingExtraUnexpectedFields()
        )

        githubServer.mock.verifyPost(
                "/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                sameJSONAs("""
                    {
                        "state": "error"
                    }
                """).allowingExtraUnexpectedFields())
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