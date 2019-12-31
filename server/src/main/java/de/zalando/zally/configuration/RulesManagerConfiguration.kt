package de.zalando.zally.configuration

import de.zalando.zally.rule.RuleDetails
import de.zalando.zally.rule.RulesManager
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.RuleSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RulesManagerConfiguration {

    private val ruleSets = mutableMapOf<Class<out RuleSet>, RuleSet>()

    @Autowired
    private lateinit var context: ApplicationContext

    @Bean
    fun rules(): Collection<Any> {
        return context.getBeansWithAnnotation(Rule::class.java).values
    }

    @Bean
    fun rulesManager(): RulesManager {
        val rules = context.getBean("rules", Collection::class.java)
        val details = rules
            .filterNotNull()
            .map { instance ->
                val rule = instance.javaClass.getAnnotation(Rule::class.java)
                val ruleSet = ruleSets.computeIfAbsent(rule.ruleSet.java) {
                    it.getConstructor().newInstance()
                }
                RuleDetails(ruleSet, rule, instance)
            }
        return RulesManager(details)
    }
}
