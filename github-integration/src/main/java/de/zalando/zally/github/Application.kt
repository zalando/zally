package de.zalando.zally.github

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Feign
import feign.jackson.JacksonDecoder
import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling
import org.zalando.twintip.spring.SchemaResource

@Import(SchemaResource::class)
@EnableScheduling
@SpringBootApplication
class Application {

    @Bean
    fun githubClient(@Value("\${github.oauthToken}") oauthToken: String,
                     @Value("\${github.apiUrl}") apiUrl: String): GitHub {
        return GitHub.connectUsingOAuth(apiUrl, oauthToken)
    }

    @Bean
    fun zallyClient(@Value("\${zally.apiUrl}") apiUrl: String,
                    jacksonObjectMapper: ObjectMapper): ZallyClient {
        return Feign.builder()
                .decoder(JacksonDecoder(jacksonObjectMapper))
                .target(ZallyClient::class.java, apiUrl)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
