package de.zalando.zally.configuration

import com.typesafe.config.Config
import de.zalando.zally.rule.RulesManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RulesManagerConfiguration {
    @Bean
    fun rulesManager(config: Config): RulesManager = RulesManager.fromClassLoader(config)
}
