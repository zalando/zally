package org.zalando.zally.configuration

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RulesConfigConfiguration {

    @Value("\${rules-config-path}")
    private lateinit var rulesConfigPath: String

    @Bean
    fun createRulesConfig(): Config {
        return ConfigFactory.load(rulesConfigPath)
    }
}
