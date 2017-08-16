package de.zalando.zally.github

import net.jadler.JadlerMocker
import net.jadler.mocking.Mocker
import net.jadler.stubbing.Stubber
import net.jadler.stubbing.server.StubHttpServerManager
import org.hamcrest.Matcher
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class GithubMock(private val base: JadlerMocker) : Stubber by base, Mocker by base, StubHttpServerManager by base {

    fun reset() {
        base.reset()
    }

    fun mockGet(path: String, filePath: String) {
        base.onRequest()
                .havingMethodEqualTo("GET")
                .havingPathEqualTo(path)
            .respond()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(filePath.loadResource())
    }

    fun mockGetBlob(path: String, filePath: String) {
        base.onRequest()
                .havingMethodEqualTo("GET")
                .havingHeaderEqualTo("Accept", "application/vnd.github.VERSION.raw")
                .havingPathEqualTo(path)
            .respond()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                .withBody(filePath.loadResource())
    }

    fun mockPost(path: String, filePath: String) {
        base.onRequest()
                .havingMethodEqualTo("POST")
                .havingPathEqualTo(path)
            .respond()
                .withStatus(HttpStatus.OK.value())
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody(filePath.loadResource())
    }

    fun verifyPost(path: String, matcher: Matcher<in String>) {
        base.verifyThatRequest()
                .havingMethodEqualTo("POST")
                .havingPathEqualTo(path)
                .havingBody(matcher)
                .receivedOnce()
    }
}