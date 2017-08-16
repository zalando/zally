package de.zalando.zally.github

import de.zalando.zally.github.util.SecurityUtil
import net.jadler.JadlerMocker
import net.jadler.stubbing.server.jdk.JdkStubHttpServer
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
            mocker = JadlerMocker(JdkStubHttpServer(8088));
            mocker.start();
            
            mocker.onRequest()
                    .havingMethodEqualTo("GET")
                    .havingPathEqualTo("/user")
                    .respond()
                    .withStatus(OK.value())
                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .withBody("json/github-user-response.json".loadResource())
        }

        @AfterClass @JvmStatic fun teardown() {
            mocker.close();
        }
    }

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Value("\${zally.secret}")
    lateinit var secret: String

    @Before
    fun setUp() { }

    @After
    fun tearDown() { }

    @Test
    fun shouldStartPullRequestValidation() {
        val body = "{}"

        val headers = HttpHeaders().apply {
            add("X-GitHub-Event", "ping")
            add("X-Hub-Signature", SecurityUtil.sign(secret, body))
        }

        val response = restTemplate.postForEntity("/api-validation",  HttpEntity(body, headers), String::class.java)

        assertThat(response.statusCode, `is`(HttpStatus.ACCEPTED))
    }

}