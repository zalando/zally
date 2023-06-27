package org.zalando.zally.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.Arrays

@Configuration
class WebMvcConfiguration : WebMvcConfigurer {

    @Autowired
    @Qualifier("yamlObjectMapper")
    private lateinit var yamlObjectMapper: ObjectMapper

    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        configurer
            .favorPathExtension(true)
            .favorParameter(false)
            .ignoreAcceptHeader(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType(MediaType.APPLICATION_JSON.subtype, MediaType.APPLICATION_JSON)
            .mediaType(MEDIA_TYPE_TEXT_XYAML.subtype, MEDIA_TYPE_TEXT_XYAML)
            .mediaType(MEDIA_TYPE_APP_XYAML.subtype, MEDIA_TYPE_APP_XYAML)
    }

    override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        val yamlConverter = MappingJackson2HttpMessageConverter(yamlObjectMapper)
        yamlConverter.supportedMediaTypes = Arrays.asList(MEDIA_TYPE_TEXT_XYAML, MEDIA_TYPE_APP_XYAML)
        converters.add(yamlConverter)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedMethods("GET", "PUT", "POST", "DELETE", "HEAD", "OPTIONS", "PATCH")
            .allowedOrigins("*")
            .allowedHeaders("*")
    }

    companion object {
        val MEDIA_TYPE_APP_XYAML: MediaType = MediaType.valueOf("application/x-yaml")
        val MEDIA_TYPE_TEXT_XYAML: MediaType = MediaType.valueOf("text/x-yaml")
    }
}
