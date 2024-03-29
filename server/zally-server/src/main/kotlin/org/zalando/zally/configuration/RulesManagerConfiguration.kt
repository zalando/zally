package org.zalando.zally.configuration

import com.typesafe.config.Config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zalando.zally.core.RulesManager

@Configuration
class RulesManagerConfiguration {
    @Bean
    fun rulesManager(config: Config): RulesManager = RulesManager.fromClassLoader(config)
}
