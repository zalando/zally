package org.zalando.zally.apireview

import com.google.common.collect.ImmutableMap
import org.zalando.zally.configuration.WebMvcConfiguration
import org.zalando.zally.dto.ApiDefinitionRequest
import org.zalando.zally.exception.MissingApiDefinitionException
import org.zalando.zally.util.ErrorResponse
import org.zalando.zally.util.JadlerUtil
import org.zalando.zally.util.readApiDefinition
import org.zalando.zally.util.resourceToString
import net.jadler.Jadler.closeJadler
import net.jadler.Jadler.initJadlerUsing
import net.jadler.stubbing.server.jdk.JdkStubHttpServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.io.IOException

class RestApiViolationsTest : RestApiBaseTest() {

    @Autowired
    private lateinit var wac: WebApplicationContext

    @BeforeEach
    fun setUp() {
        initJadlerUsing(JdkStubHttpServer())
    }

    @AfterEach
    fun tearDown() {
        closeJadler()
    }

    @Test
    @Throws(IOException::class)
    fun shouldValidateGivenApiDefinition() {
        val response = sendApiDefinition(readApiDefinition("fixtures/openapi3_petstore_expanded.json"))

        val violations = response.violations
        assertThat(violations).hasSize(3)
        assertThat(violations[0].description).isEqualTo("TestCheckAlwaysReport3MustViolations #1")
        assertThat(violations[1].description).isEqualTo("TestCheckAlwaysReport3MustViolations #2")
        assertThat(violations[2].description).isEqualTo("TestCheckAlwaysReport3MustViolations #3")
        assertThat(response.externalId).isNotNull()
    }

    @Test
    @Throws(IOException::class)
    fun shouldReturnCounters() {
        val response = sendApiDefinition(readApiDefinition("fixtures/openapi3_petstore_expanded.json"))

        val count = response.violationsCount
        assertThat(count["must"]).isEqualTo(3)
        assertThat(count["should"]).isEqualTo(0)
        assertThat(count["may"]).isEqualTo(0)
        assertThat(count["hint"]).isEqualTo(0)
        assertThat(response.externalId).isNotNull()
    }

    @Test
    @Throws(IOException::class)
    fun shouldIgnoreRulesWithVendorExtension() {
        val response = sendApiDefinition(readApiDefinition("fixtures/openapi3_petstore_ignored.json"))

        val violations = response.violations
        assertThat(violations).isEmpty()
    }

    @Test
    @Throws(IOException::class)
    fun shouldIgnoreRulesWithApiParameter() {
        val request = readApiDefinition("fixtures/openapi3_petstore_expanded.json").copy(ignoreRules = listOf("TestCheckAlwaysReport3MustViolations"))
        val response = sendApiDefinition(request)

        val violations = response.violations
        assertThat(violations).isEmpty()
        assertThat(response.externalId).isNotNull()
    }

    @Test
    @Throws(IOException::class)
    fun shouldRespondWithBadRequestOnMalformedJson() {
        val responseEntity = sendApiDefinition(
            ApiDefinitionRequest.fromJson("{\"malformed\": \"dummy\""),
            ErrorResponse::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(BAD_REQUEST)
        assertThat(responseEntity.headers.contentType!!.toString()).isEqualTo(RestApiBaseTest.APPLICATION_PROBLEM_JSON)
        assertThat(responseEntity.body!!.title).isEqualTo(BAD_REQUEST.reasonPhrase)
        assertThat(responseEntity.body!!.status).isNotEmpty()
        assertThat(responseEntity.body!!.detail).isNotEmpty()
    }

    @Test
    @Throws(IOException::class)
    fun shouldRespondWithBadRequestWhenApiDefinitionFieldIsMissing() {
        val responseEntity = restTemplate.postForEntity(
            RestApiBaseTest.API_VIOLATIONS_URL, ImmutableMap.of("my_api", "dummy"), ErrorResponse::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(BAD_REQUEST)
        assertThat(responseEntity.headers.contentType!!.toString()).isEqualTo(RestApiBaseTest.APPLICATION_PROBLEM_JSON)
        assertThat(responseEntity.body!!.title).isEqualTo(BAD_REQUEST.reasonPhrase)
        assertThat(responseEntity.body!!.status).isNotEmpty()
        assertThat(responseEntity.body!!.detail).isEqualTo(MissingApiDefinitionException.MESSAGE)
    }

    @Test
    fun shouldRespondWithViolationWhenApiDefinitionFieldIsNotValidSwaggerDefinition() {
        val response = sendApiDefinition(
            ApiDefinitionRequest.fromJson("\"no swagger definition\"")
        )

        assertThat(response.violations).hasSize(5)
        assertThat(response.violations[0].title).isEqualTo("provide API specification using OpenAPI")
        assertThat(response.violations[0].description).isEqualTo("attribute openapi is not of type `object`")
        assertThat(response.violations[1].title).isEqualTo("TestCheckIsOpenApi3")
        assertThat(response.externalId).isNotNull()
    }

    @Test
    @Throws(IOException::class)
    fun shouldReadJsonSpecificationFromUrl() {
        val definitionUrl = JadlerUtil.stubResource("fixtures/openapi3_petstore_expanded.json")

        val violations = sendApiDefinition(
            ApiDefinitionRequest.fromUrl(definitionUrl)
        ).violations

        assertThat(violations).hasSize(3)
        assertThat(violations[0].description).isEqualTo("TestCheckAlwaysReport3MustViolations #1")
        assertThat(violations[1].description).isEqualTo("TestCheckAlwaysReport3MustViolations #2")
        assertThat(violations[2].description).isEqualTo("TestCheckAlwaysReport3MustViolations #3")
    }

    @Test
    @Throws(IOException::class)
    fun shouldReadYamlSpecificationFromUrl() {
        val definitionUrl = JadlerUtil.stubResource("fixtures/openapi3_petstore.yaml")

        val violations = sendApiDefinition(
            ApiDefinitionRequest.fromUrl(definitionUrl)
        ).violations

        assertThat(violations).hasSize(3)
        assertThat(violations[0].title).isEqualTo("TestCheckAlwaysReport3MustViolations")
    }

    @Test
    @Throws(Exception::class)
    fun shouldReturn404WhenHostNotRecognised() {
        val request = ApiDefinitionRequest.fromUrl("http://remote-localhost/test.yaml")
        val responseEntity = restTemplate.postForEntity(
            RestApiBaseTest.API_VIOLATIONS_URL, request, ErrorResponse::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(NOT_FOUND)
        assertThat(responseEntity.body!!.detail).isEqualTo("Unknown host while retrieving api definition url: remote-localhost")
    }

    @Test
    fun shouldReturn404WhenNotFound() {
        val responseEntity = sendApiDefinition(
            ApiDefinitionRequest.fromUrl(JadlerUtil.stubNotFound()),
            ErrorResponse::class.java
        )

        assertThat(responseEntity.statusCode).isEqualTo(NOT_FOUND)
        assertThat(responseEntity.body!!.detail).isEqualTo("404 Not Found: \"NotFound\" while retrieving api definition url")
    }

    @Test
    @Throws(IOException::class)
    fun shouldStoreSuccessfulApiReviewRequest() {
        sendApiDefinition(readApiDefinition("fixtures/openapi3_petstore_expanded.json"))
        assertThat(apiReviewRepository.count()).isEqualTo(1L)
        assertThat(apiReviewRepository.findAll().iterator().next().isSuccessfulProcessed).isTrue()
    }

    @Test
    fun shouldStoreUnsuccessfulApiReviewRequest() {
        sendApiDefinition(
            ApiDefinitionRequest.fromUrl(JadlerUtil.stubNotFound()),
            ErrorResponse::class.java
        )

        assertThat(apiReviewRepository.count()).isEqualTo(1L)
        assertThat(apiReviewRepository.findAll().iterator().next().isSuccessfulProcessed).isFalse()
    }

    @Test
    @Throws(Exception::class)
    fun shouldAcceptYamlAndRespondWithJson() {
        val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
        val requestBuilder = MockMvcRequestBuilders.post("/api-violations")
            .contentType(WebMvcConfiguration.MEDIA_TYPE_APP_XYAML)
            .accept(MediaType.APPLICATION_JSON)
            .content(resourceToString("fixtures/api_violations_request.yaml"))

        val result = mockMvc.perform(requestBuilder).andReturn()

        assertThat(result.response.status).isEqualTo(200)
        assertThat(result.response.contentType).isEqualTo(MediaType.APPLICATION_JSON_VALUE)
    }

    @Test
    @Throws(Exception::class)
    fun shouldNotAcceptYamlWithoutCorrectContentType() {
        val mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
        val requestBuilder = MockMvcRequestBuilders.post("/api-violations")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(resourceToString("fixtures/api_violations_request.yaml"))

        val result = mockMvc.perform(requestBuilder).andReturn()

        assertThat(result.response.status).isEqualTo(400)
    }
}
