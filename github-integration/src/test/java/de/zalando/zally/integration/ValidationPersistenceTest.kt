package de.zalando.zally.integration

import com.fasterxml.jackson.databind.ObjectMapper
import de.zalando.zally.integration.github.SecurityUtil
import de.zalando.zally.integration.jadler.GithubMock
import de.zalando.zally.integration.jadler.JadlerRule
import de.zalando.zally.integration.jadler.ZallyMock
import de.zalando.zally.integration.validation.ValidationRepository
import net.jadler.JadlerMocker
import net.jadler.stubbing.server.jdk.JdkStubHttpServer
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.skyscreamer.jsonassert.JSONAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = arrayOf(Application::class))
@ActiveProfiles("test")
@Sql(scripts = arrayOf("/sql/cleanup-data.sql"), executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ValidationPersistenceTest {
    companion object {
        @ClassRule @JvmField val githubServer = JadlerRule(GithubMock(JadlerMocker(JdkStubHttpServer(8088)))) {
            it.mockGet("/user", "json/github-user-response.json")//required for app start
        }

        @ClassRule @JvmField val zallyServer = JadlerRule(ZallyMock(JadlerMocker(JdkStubHttpServer(9099))))
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var validationRepository: ValidationRepository

    @Autowired
    lateinit var jacksonObjectMapper: ObjectMapper

    @Value("\${github.secret}")
    lateinit var secret: String

    @Before
    fun setUp() {
        githubServer.mock.reset()
        zallyServer.mock.reset()
    }

    @Test
    fun shouldPersistSuccessfulValidation() {
        githubServer.mock.mockGet("/repos/myUserName/zally/git/trees/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "json/github-tree.json")
        githubServer.mock.mockGetBlob("/repos/myUserName/zally/git/blobs/bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "json/github-zally-yaml-blob.yaml")
        githubServer.mock.mockGetBlob("/repos/myUserName/zally/git/blobs/cccccccccccccccccccccccccccccccccccccccc", "json/github-api-yaml-blob.yaml")
        zallyServer.mock.mockPost("/api-violations", "json/zally-success-response.json")
        githubServer.mock.mockPost("/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "json/github-commit-status-change.json")

        val body = "json/github-webhook-pullrequest.json".loadResource()
        val response = restTemplate.postForEntity("/github-webhook", webhookRequest(body), String::class.java)
        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        val validations = validationRepository.findAll()
        assertThat(validations.size, `is`(1))

        val validation = validations.first()
        assertThat(validation.apiDefinition, `is`("json/github-api-yaml-blob.yaml".loadResource()))
        assertThat(validation.createdOn, `is`(notNullValue()))

        JSONAssert.assertEquals("json/pull-request-info-content.json".loadResource(), validation.pullRequestInfo, true)
        JSONAssert.assertEquals("json/zally-success-response.json".loadResource(), validation.violations, false)
    }

    @Test
    fun shouldPersistFailedValidation() {
        githubServer.mock.mockGet("/repos/myUserName/zally/git/trees/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "json/github-tree-missing-zally-yaml.json")
        githubServer.mock.mockPost("/repos/myUserName/zally/statuses/aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "json/github-commit-status-change.json")

        val body = "json/github-webhook-pullrequest.json".loadResource()
        val response = restTemplate.postForEntity("/github-webhook", webhookRequest(body), String::class.java)
        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        val validations = validationRepository.findAll()
        assertThat(validations.size, `is`(1))

        val validation = validations.first()
        assertThat(validation.apiDefinition, `is`(nullValue()))
        assertThat(validation.createdOn, `is`(notNullValue()))
        assertThat(validation.violations, `is`("null"))

        JSONAssert.assertEquals("json/pull-request-info-content.json".loadResource(), validation.pullRequestInfo, true)
    }

    private fun webhookRequest(body: String) = HttpEntity(body, HttpHeaders().apply {
            add("X-GitHub-Event", "pull_request")
            add("X-Hub-Signature", SecurityUtil.sign(secret, body))
        })

}