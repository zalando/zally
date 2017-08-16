package de.zalando.zally.github

import org.kohsuke.github.GitHub
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.EnableScheduling
import org.zalando.twintip.spring.SchemaResource

@Import(SchemaResource::class)
@EnableScheduling
@SpringBootApplication
class Application {
    @Bean @Lazy
    fun initializeGithub(@Value("\${zally.oauthToken}") oauthToken: String,
                         @Value("\${github.apiUrl}") apiUrl: String): GitHub {
        return GitHub.connectUsingOAuth(apiUrl, oauthToken)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
