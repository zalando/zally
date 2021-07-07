package org.zalando.zally.apireview

import org.zalando.zally.dto.ApiDefinitionRequest
import org.zalando.zally.exception.InaccessibleResourceUrlException
import org.zalando.zally.exception.MissingApiDefinitionException
import org.zalando.zally.util.JadlerUtil
import net.jadler.Jadler.closeJadler
import net.jadler.Jadler.initJadlerUsing
import net.jadler.stubbing.server.jdk.JdkStubHttpServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_HTML_VALUE
import org.springframework.web.client.RestTemplate

class ApiDefinitionReaderTest {

    private val contentInJson = "{\"swagger\":\"2.0\"}"

    private lateinit var reader: ApiDefinitionReader

    @Before
    fun setUp() {
        initJadlerUsing(JdkStubHttpServer())
        reader = ApiDefinitionReader(RestTemplate())
    }

    @After
    fun tearDown() {
        closeJadler()
    }

    @Test(expected = MissingApiDefinitionException::class)
    fun shouldThrowMissingApiDefinitionExceptionWhenDefinitionIsNotFound() {
        reader.read(ApiDefinitionRequest())
    }

    @Test
    fun shouldReturnStringWhenApiDefinitionIsFound() {
        val request = ApiDefinitionRequest(contentInJson, null, "http://zalando.de")
        val result = reader.read(request)
        assertEquals(contentInJson, result)
    }

    @Test
    fun shouldReadJsonSwaggerDefinitionFromUrl() {
        val url = JadlerUtil.stubResource("test.json", contentInJson)
        val result = reader.read(ApiDefinitionRequest.fromUrl(url))
        assertEquals(contentInJson, result)
    }

    @Test
    fun shouldReadYamlSwaggerDefinitionFromUrl() {
        val contentInYaml = "swagger: \"2.0\""
        val url = JadlerUtil.stubResource("test.yaml", contentInYaml, APPLICATION_X_YAML_VALUE)
        val result = reader.read(ApiDefinitionRequest.fromUrl(url))

        assertEquals(contentInYaml, result)
    }

    @Test
    fun shouldPreferRawSpecification() {
        val rawYaml = "raw: yaml"
        val request = ApiDefinitionRequest("{\"some\": \"json\"", rawYaml, "http://zalando.de")
        val result = reader.read(request)
        assertEquals(rawYaml, result)
    }

    @Test
    fun shouldRetryLoadingOfUrlIfEndsWithSpecialEncodedCharacters() {
        val url = JadlerUtil.stubResource("test.json", contentInJson)
        val result = reader.read(ApiDefinitionRequest.fromUrl("$url%3D%3D"))
        assertEquals(contentInJson, result)
    }

    @Test(expected = InaccessibleResourceUrlException::class)
    fun shouldErrorBadRequestWhenDefinitionFromUrlUnsuccessful() {
        val url = JadlerUtil.stubResource("test.json", "", HttpStatus.UNAUTHORIZED.value(), APPLICATION_JSON_VALUE)

        reader.read(ApiDefinitionRequest.fromUrl(url))
    }

    @Test(expected = InaccessibleResourceUrlException::class)
    fun shouldErrorBadRequestWhenDefinitionFromUrlWrongContentType() {
        val url = JadlerUtil.stubResource("test.json", "", HttpStatus.OK.value(), TEXT_HTML_VALUE)

        reader.read(ApiDefinitionRequest.fromUrl(url))
    }

    companion object {
        private const val APPLICATION_X_YAML_VALUE = "application/x-yaml"
    }
}
