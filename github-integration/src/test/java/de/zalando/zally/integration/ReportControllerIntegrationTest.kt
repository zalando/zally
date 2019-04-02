package de.zalando.zally.integration

import de.zalando.zally.integration.mock.EmbeddedPostgresqlConfiguration
import de.zalando.zally.integration.mock.GithubMock
import de.zalando.zally.integration.mock.JadlerRule
import net.jadler.JadlerMocker
import net.jadler.stubbing.server.jdk.JdkStubHttpServer
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.containsString
import org.junit.Assert.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [Application::class, EmbeddedPostgresqlConfiguration::class])
@ActiveProfiles("test")
@SqlGroup(
        Sql("/sql/validations-get-test-data.sql"),
    Sql("/sql/cleanup-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD))
class ReportControllerIntegrationTest {
    companion object {
        @ClassRule @JvmField val githubServer = JadlerRule(GithubMock(JadlerMocker(JdkStubHttpServer(8088)))) {
            it.mockGet("/user", "json/github-user-response.json") // required for app start
        }
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    // todo verify html?

    @Test
    fun shouldReturnExistingValidation() {
        val response = restTemplate.getForEntity("/reports/100", String::class.java)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        assertThat(response.body, containsString("https://github.com/myUserName/zally"))
        assertThat(response.body, containsString("100"))
        assertThat(response.body, containsString("SHOULD"))
        assertThat(response.body, containsString("description: Zalando&#39;s API Linter"))
    }

    @Test
    fun shouldReturnNotFoundIfReportsDoesNotExist() {
        val entity: HttpEntity<Any> = HttpEntity(HttpHeaders().apply { set(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE) })
        val response = restTemplate.exchange("/reports/555", HttpMethod.GET, entity, String::class.java)
        assertThat(response.statusCode, `is`(HttpStatus.NOT_FOUND))
    }
}