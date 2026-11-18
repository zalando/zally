package org.zalando.zally.configuration

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.cfg.MapperBuilder
import tools.jackson.databind.json.JsonMapper
import tools.jackson.dataformat.yaml.YAMLMapper
import tools.jackson.module.kotlin.KotlinModule

/**
 * The server's own mappers. Jackson 3 ships the Java 8, `java.time` and parameter-name support in databind, so only
 * the Kotlin module is added. The mappers are immutable: all options are set on the builder.
 */
@Configuration
class JacksonObjectMapperConfiguration {
    @Bean
    @Primary
    fun createObjectMapper(): JsonMapper = configure(JsonMapper.builder()).build()

    @Bean
    @Qualifier("yamlObjectMapper")
    fun createYamlObjectMapper(): YAMLMapper = configure(YAMLMapper.builder()).build()

    private fun <M : ObjectMapper, B : MapperBuilder<M, B>> configure(builder: B): B =
        builder
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .addModule(KotlinModule.Builder().build())
}
