package org.zalando.zally.configuration

import com.google.common.collect.Maps
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext
import org.springframework.security.oauth2.client.OAuth2RestTemplate
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfiguration {

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    fun restTemplateRegistry(remoteUrls: RemoteUrlsConfiguration.RemoteUrls, defaultRestTemplate: RestTemplate): RestTemplateRegistry {
        val registry = RestTemplateRegistry(defaultRestTemplate)
        remoteUrls.remoteUrls.forEach { registry.registerForHost(it.host, createOAuth2RestTemplate(it.oauth2)) }
        return registry
    }

    private fun createOAuth2RestTemplate(oauth2Config: RemoteUrlsConfiguration.OAuth2ClientConfig): OAuth2RestTemplate {

        val resourceDetails = ClientCredentialsResourceDetails()
        resourceDetails.accessTokenUri = oauth2Config.accessTokenUri
        resourceDetails.clientId = oauth2Config.clientId
        resourceDetails.clientSecret = oauth2Config.clientSecret
        resourceDetails.grantType = oauth2Config.grantType
        resourceDetails.scope = mutableListOf(oauth2Config.scope)

        val clientContext = DefaultOAuth2ClientContext()
        return OAuth2RestTemplate(resourceDetails, clientContext)
    }

    /**
     * A Map that holds for each configured Host an OAuth2RestTemplate
     */
    class RestTemplateRegistry(private val defaultRestTemplate: RestTemplate) {

        private val registry: MutableMap<String, RestTemplate> = Maps.newHashMap()

        fun registerForHost(host: String, client: OAuth2RestTemplate) =
            registry.put(host, client)

        fun getForHost(host: String) = registry.getOrDefault(host, defaultRestTemplate)
    }
}
