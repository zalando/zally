package de.zalando.zally.configuration

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RulesConfigConfiguration {

    @Value("\${rules-config-path}")
    private val rulesConfigPath: String? = null

    @Bean
    open fun createRulesConfig(): Config {
        return ConfigFactory.load(rulesConfigPath!!)
    }
}
