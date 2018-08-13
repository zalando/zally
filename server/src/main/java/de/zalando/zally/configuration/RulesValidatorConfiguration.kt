package de.zalando.zally.configuration

import de.zalando.zally.rule.ApiValidator
import de.zalando.zally.rule.CompositeRulesValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
open class RulesValidatorConfiguration {

    @Autowired
    private val compositeValidator: CompositeRulesValidator? = null

    @Bean
    @Primary
    open fun validator(): ApiValidator? {
        return compositeValidator
    }
}
