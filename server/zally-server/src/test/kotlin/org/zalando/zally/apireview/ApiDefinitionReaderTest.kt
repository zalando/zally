package org.zalando.zally.apireview

import net.jadler.Jadler.closeJadler
import net.jadler.Jadler.initJadlerUsing
import net.jadler.stubbing.server.jdk.JdkStubHttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.TEXT_HTML_VALUE
import org.springframework.web.client.RestTemplate
import org.zalando.zally.configuration.RestTemplateConfiguration
import org.zalando.zally.dto.ApiDefinitionRequest
import org.zalando.zally.exception.InaccessibleResourceUrlException
import org.zalando.zally.exception.MissingApiDefinitionException
import org.zalando.zally.util.JadlerUtil

class ApiDefinitionReaderTest {

    private val contentInJson = "{\"swagger\":\"2.0\"}"

    private lateinit var reader: ApiDefinitionReader

    @BeforeEach
    fun setUp() {
        initJadlerUsing(JdkStubHttpServer())
        reader = ApiDefinitionReader(RestTemplateConfiguration.RestTemplateRegistry(RestTemplate()))
    }

    @AfterEach
    fun tearDown() {
        closeJadler()
    }

    @Test
    fun shouldThrowMissingApiDefinitionExceptionWhenDefinitionIsNotFound() {
        assertThrows(MissingApiDefinitionException::class.java, { reader.read(ApiDefinitionRequest()) })
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

    @Test
    fun shouldErrorBadRequestWhenDefinitionFromUrlUnsuccessful() {
        val url = JadlerUtil.stubResource("test.json", "", HttpStatus.UNAUTHORIZED.value(), APPLICATION_JSON_VALUE)

        assertThrows(InaccessibleResourceUrlException::class.java, { reader.read(ApiDefinitionRequest.fromUrl(url)) })
    }

    @Test
    fun shouldErrorBadRequestWhenDefinitionFromUrlWrongContentType() {
        val url = JadlerUtil.stubResource("test.json", "", HttpStatus.OK.value(), TEXT_HTML_VALUE)

        assertThrows(InaccessibleResourceUrlException::class.java, { reader.read(ApiDefinitionRequest.fromUrl(url)) })
    }

    companion object {
        private const val APPLICATION_X_YAML_VALUE = "application/x-yaml"
    }
}
