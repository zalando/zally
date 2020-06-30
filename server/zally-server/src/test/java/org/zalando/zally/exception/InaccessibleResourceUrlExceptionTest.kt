package org.zalando.zally.exception

import org.junit.Assert.assertEquals
import org.junit.Test
import org.springframework.http.HttpStatus

class InaccessibleResourceUrlExceptionTest {
    @Test
    fun shouldReturnParametersSpecifiedInConstructor() {
        val exception = InaccessibleResourceUrlException(
            "Test Message", HttpStatus.BAD_REQUEST
        )

        assertEquals("Test Message", exception.message)
        assertEquals(HttpStatus.BAD_REQUEST, exception.httpStatus)
    }
}
