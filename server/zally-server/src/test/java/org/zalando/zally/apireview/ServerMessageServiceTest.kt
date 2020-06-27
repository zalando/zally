package org.zalando.zally.apireview

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ServerMessageServiceTest {

    private val serverMessageService = ServerMessageService(
        listOf("unirest-java/1.3.11", "Zally-CLI/1.0"),
        "https://github.com/zalando/zally/releases"
    )

    @Test
    @Throws(Exception::class)
    fun shouldReturnEmptyStringIfUserAgentIsUpToDate() {
        val message = serverMessageService.serverMessage("Zally-CLI/1.1")

        assertThat(message).isEqualTo("")
    }

    @Test
    @Throws(Exception::class)
    fun shouldReturnDeprecationMessageIfUserAgentIsDeprecated() {
        val message = serverMessageService.serverMessage("Zally-CLI/1.0")

        assertThat(message).isEqualTo("Please update your CLI: https://github.com/zalando/zally/releases")
    }

    @Test
    @Throws(Exception::class)
    fun shouldReturnDeprecationMessageIfUserAgentIsNotSet() {
        val messageIfNull = serverMessageService.serverMessage(null)
        val messageIfEmptyString = serverMessageService.serverMessage("")

        assertThat(messageIfNull).isEqualTo("")
        assertThat(messageIfEmptyString).isEqualTo("")
    }
}
