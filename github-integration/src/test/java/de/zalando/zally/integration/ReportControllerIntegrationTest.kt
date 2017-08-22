package de.zalando.zally.integration

import de.zalando.zally.integration.jadler.GithubMock
import de.zalando.zally.integration.jadler.JadlerRule
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
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.jdbc.SqlGroup
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = arrayOf(Application::class))
@ActiveProfiles("test")
@SqlGroup(
        Sql("/sql/validations-get-test-data.sql"),
        Sql(scripts = arrayOf("/sql/cleanup-data.sql"), executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD))
class ReportControllerIntegrationTest {
    companion object {
        @ClassRule @JvmField val githubServer = JadlerRule(GithubMock(JadlerMocker(JdkStubHttpServer(8088)))) {
            it.mockGet("/user", "json/github-user-response.json")//required for app start
        }
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    //todo test 404
    //todo verify html?

    @Test
    fun shouldReturnExistingValidation() {
        val response = restTemplate.getForEntity("/reports/100", String::class.java)
        assertThat(response.statusCode, `is`(HttpStatus.OK))
        assertThat(response.body, containsString("https://api.github.com/repos/myUserName/zally"))
        assertThat(response.body, containsString("100"))
        assertThat(response.body, containsString("SHOULD"))
        assertThat(response.body, containsString("description: Zalando&#39;s API Linter"))
    }

}