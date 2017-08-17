package de.zalando.zally.integration

import com.fasterxml.jackson.databind.ObjectMapper
import de.zalando.zally.integration.github.SecurityUtil
import de.zalando.zally.integration.jadler.GithubMock
import de.zalando.zally.integration.jadler.JadlerRule
import de.zalando.zally.integration.jadler.ZallyMock
import de.zalando.zally.integration.validation.ValidationRepository
import de.zalando.zally.integration.zally.ApiDefinitionResponse
import de.zalando.zally.integration.zally.ViolationType
import net.jadler.JadlerMocker
import net.jadler.stubbing.server.jdk.JdkStubHttpServer
import org.hamcrest.Matchers.`is`
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
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = arrayOf(Application::class))
@ActiveProfiles("test")
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
    fun shouldPersistValidation() {
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
        assertThat(validation.repositoryUrl, `is`("https://api.github.com/repos/myUserName/zally"))
        assertThat(validation.apiDefinition, `is`("json/github-api-yaml-blob.yaml".loadResource()))
        assertThat(validation.createdOn, `is`(notNullValue()))

        val violations = readToObject(validation.violations)
        assertThat(violations, `is`(readToObject("json/zally-success-response.json".loadResource())))
        assertThat(violations.violations!!.size, `is`(1))
        assertThat(violations.violations!!.first().title, `is`("Some Violation"))
        assertThat(violations.violations!!.first().paths, `is`(listOf("/abcde/")))
        assertThat(violations.violations!!.first().violationType, `is`(ViolationType.SHOULD))
    }

    private fun webhookRequest(body: String) = HttpEntity(body, HttpHeaders().apply {
            add("X-GitHub-Event", "pull_request")
            add("X-Hub-Signature", SecurityUtil.sign(secret, body))
        })

    private fun readToObject(input: String?) = jacksonObjectMapper.readValue(input, ApiDefinitionResponse::class.java)

}