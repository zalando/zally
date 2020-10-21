package org.zalando.zally.configuration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.codehaus.jackson.annotate.JsonIgnoreProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import java.io.FileNotFoundException

@Configuration
class RemoteUrlsConfiguration {

    private val logger = LoggerFactory.getLogger(RemoteUrlsConfiguration::class.java)

    @Value("\${zally.remoteUrls.configFile}")
    lateinit var configFile: String

    @Bean
    fun createRemoteUrlsConfiguration(): RemoteUrls = loadConfig()

    @JsonIgnoreProperties(ignoreUnknown = true)
    class RemoteUrls {
        val remoteUrls: List<RemoteUrl> = arrayListOf()
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class RemoteUrl(
        val host: String,
        val oauth2: OAuth2ClientConfig
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class OAuth2ClientConfig(
        val grantType: String,
        val clientId: String,
        val clientSecret: String,
        val accessTokenUri: String,
        val scope: String
    )

    private fun loadConfig(): RemoteUrls = try {
        val configJson = File(configFile).readText()
        jacksonObjectMapper().readValue(configJson)
    } catch (exception: FileNotFoundException) {
        logger.warn("Remote URLs configuration file: $configFile cannot be found. No RemoteUrls are being configured. Please set the correct file path using the property zally.remoteUrls.configFile or the environment variable REMOTE_URLS_CONFIG_FILE")
        RemoteUrls() // Return empty config as default.
    }
}
