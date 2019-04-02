package de.zalando.zally.integration

import de.zalando.zally.integration.github.SecurityUtil
import de.zalando.zally.integration.mock.EmbeddedPostgresqlConfiguration
import de.zalando.zally.integration.mock.GithubMock
import de.zalando.zally.integration.mock.JadlerRule
import de.zalando.zally.integration.mock.ZallyMock
import de.zalando.zally.integration.validation.ApiValidation
import de.zalando.zally.integration.validation.ValidationRepository
import net.jadler.JadlerMocker
import net.jadler.stubbing.server.jdk.JdkStubHttpServer
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
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
import uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [Application::class, EmbeddedPostgresqlConfiguration::class])
@ActiveProfiles("test")
@Sql("/sql/cleanup-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ValidationPersistenceTest {
    companion object {
        @ClassRule @JvmField val githubServer = JadlerRule(GithubMock(JadlerMocker(JdkStubHttpServer(8088)))) {
            it.mockGet("/user", "json/github-user-response.json") // required for app start
        }
        @ClassRule @JvmField val zallyServer = JadlerRule(ZallyMock(JadlerMocker(JdkStubHttpServer(9099))))
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var validationRepository: ValidationRepository

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
        githubServer.mock.mockGet("/repos/myUserName/zally/pulls/1/files", "json/github-pull-files.json")

        val body = "json/github-webhook-pullrequest.json".loadResource()
        val response = restTemplate.postForEntity("/github-webhook", webhookRequest(body), String::class.java)
        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))

        val validations = validationRepository.findAll()
        assertThat(validations.size, `is`(1))

        val validation = validations.first()
        assertThat(validation.apiValidations, hasSize(1))
        assertThat(validation.apiValidations.first().apiDefinition, `is`("json/github-api-yaml-blob.yaml".loadResource()))
        assertThat(validation.createdOn, `is`(notNullValue()))

        assertThat(validation.pullRequestInfo,
                sameJSONAs("json/pull-request-info-content.json".loadResource())
        )
        assertThat(validation.apiValidations.first().violations,
                sameJSONAs("json/zally-success-response.json".loadResource())
                        .allowingExtraUnexpectedFields()
                        .allowingAnyArrayOrdering()
        )
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
        assertThat(validation.apiValidations, `is`(empty<ApiValidation>()))
        assertThat(validation.createdOn, `is`(notNullValue()))
        assertThat(validation.pullRequestInfo,
                sameJSONAs("json/pull-request-info-content.json".loadResource())
        )
    }

    private fun webhookRequest(body: String) = HttpEntity(body, HttpHeaders().apply {
            add("X-GitHub-Event", "pull_request")
            add("X-Hub-Signature", SecurityUtil.sign(secret, body))
        })
}