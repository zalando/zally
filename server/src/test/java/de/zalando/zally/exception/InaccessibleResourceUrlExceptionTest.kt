package de.zalando.zally.exception

import org.junit.Test
import org.springframework.http.HttpStatus

import org.junit.Assert.assertEquals

class InaccessibleResourceUrlExceptionTest {
    @Test
    fun shouldReturnParametersSpecifiedInConstructor() {
        val exception = InaccessibleResourceUrlException(
                "Test Message", HttpStatus.BAD_REQUEST)

        assertEquals("Test Message", exception.message)
        assertEquals(HttpStatus.BAD_REQUEST, exception.httpStatus)
    }
}
