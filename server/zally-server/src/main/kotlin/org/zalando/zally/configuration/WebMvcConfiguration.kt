package org.zalando.zally.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverters
import org.springframework.http.converter.yaml.JacksonYamlHttpMessageConverter
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import tools.jackson.dataformat.yaml.YAMLMapper

@Configuration
class WebMvcConfiguration : WebMvcConfigurer {
    @Autowired
    @Qualifier("yamlObjectMapper")
    private lateinit var yamlObjectMapper: YAMLMapper

    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        configurer
            .favorParameter(false)
            .ignoreAcceptHeader(false)
            .defaultContentType(MediaType.APPLICATION_JSON)
            .mediaType(MediaType.APPLICATION_JSON.subtype, MediaType.APPLICATION_JSON)
            .mediaType(MEDIA_TYPE_TEXT_XYAML.subtype, MEDIA_TYPE_TEXT_XYAML)
            .mediaType(MEDIA_TYPE_APP_XYAML.subtype, MEDIA_TYPE_APP_XYAML)
    }

    override fun configureMessageConverters(builder: HttpMessageConverters.ServerBuilder) {
        val yamlConverter = JacksonYamlHttpMessageConverter(yamlObjectMapper)
        yamlConverter.supportedMediaTypes = listOf(MEDIA_TYPE_TEXT_XYAML, MEDIA_TYPE_APP_XYAML, MediaType.APPLICATION_YAML)
        builder.withYamlConverter(yamlConverter)
    }

    companion object {
        val MEDIA_TYPE_APP_XYAML: MediaType = MediaType.valueOf("application/x-yaml")
        val MEDIA_TYPE_TEXT_XYAML: MediaType = MediaType.valueOf("text/x-yaml")
    }
}
