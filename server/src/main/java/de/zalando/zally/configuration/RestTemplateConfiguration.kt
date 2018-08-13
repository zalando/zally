package de.zalando.zally.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
open class RestTemplateConfiguration {

    @Bean
    open fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
