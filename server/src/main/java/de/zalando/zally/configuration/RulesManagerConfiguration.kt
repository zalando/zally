package de.zalando.zally.configuration

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import de.zalando.zally.rule.RuleDetails
import de.zalando.zally.rule.RulesManager
import de.zalando.zally.rule.api.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class RulesManagerConfiguration {

    @Autowired
    private val context: ApplicationContext? = null

    @Value("\${rules-config-path}")
    private val rulesConfigPath: String? = null

    @Bean
    open fun createRulesConfig(): Config {
        return ConfigFactory.load(rulesConfigPath!!)
    }

    @Bean
    open fun rules(config: Config): Collection<Any> = RulesManagerConfiguration::class.java
        .classLoader
        .getResources("META-INF/services/" + Rule::class.qualifiedName)
        .toList()
        .flatMap { url ->
            url.readText()
            .lines()
            .filter { it.isNotEmpty() }
            .map {
                val clazz = Class.forName(it)
                val constructors = clazz.constructors
                if (constructors[0].parameters.isEmpty()) {
                    clazz.newInstance()
                } else {
                    constructors[0].newInstance(config)
                }
            }
        }

    @Bean
    open fun rulesManager(config: Config): RulesManager {
        val rules = context!!.getBean("rules", Collection::class.java)

        val details = rules
                .filterNotNull()
                .map { instance ->
                    val rule = instance.javaClass.getAnnotation(Rule::class.java)
                    val ruleSet = context!!.getBean(rule.ruleSet.java)
                    RuleDetails(ruleSet, rule, instance)
                }
        return RulesManager(details)
    }
}
