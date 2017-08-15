package de.zalando.zally.github

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
class Application
{
    @Bean
    fun initializeGithub(@Value("\${zally.oauthToken}") oauthToken: String): GitHub {
        return GitHub.connectUsingOAuth(oauthToken)
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
