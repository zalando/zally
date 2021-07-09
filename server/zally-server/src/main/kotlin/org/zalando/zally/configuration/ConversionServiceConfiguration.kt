package org.zalando.zally.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.support.DefaultConversionService

@Configuration
class ConversionServiceConfiguration {

    @Bean
    fun conversionService(): ConversionService {
        return DefaultConversionService()
    }
}
