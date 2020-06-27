package org.zalando.zally.configuration

import org.zalando.zally.core.DefaultContextFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.regex.Pattern

@Configuration
class ContextFactoryConfiguration {

    @Value("\${zally.propagateAuthorizationUrls:}")
    private lateinit var propagateAuthorizationUrls: List<Pattern>

    @Bean
    fun contextFactory(): DefaultContextFactory {
        return DefaultContextFactory()
    }
}
