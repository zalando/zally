package org.zalando.zally.apireview

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.RequestEntity
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
class RestCorsWithOAuthTest : RestApiBaseTest() {

    @Test
    fun shouldSupportCorsWhenOAuthIsEnabledOnAllResources() {
        assertThat(optionsRequest(API_VIOLATIONS_URL)).isEqualTo(HttpStatus.OK)
        assertThat(optionsRequest(SUPPORTED_RULES_URL)).isEqualTo(HttpStatus.OK)
        assertThat(optionsRequest(REVIEW_STATISTICS_URL)).isEqualTo(HttpStatus.OK)
    }

    private fun optionsRequest(url: String): HttpStatusCode {
        return restTemplate.exchange(url, HttpMethod.OPTIONS, RequestEntity.EMPTY, String::class.java).statusCode
    }
}
