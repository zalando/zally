package org.zalando.zally.configuration

import org.zalando.zally.rule.api.Rule
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.annotation.AnnotationBeanNameGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.filter.AnnotationTypeFilter

/**
 * Register classes annotated with [Rule] as BeanDefinition prior to any Bean instantiation.
 */
@Configuration
class RulesDefinitionConfiguration {

    @Bean
    fun rulesDefinitions(): BeanDefinitionRegistryPostProcessor {
        return object : BeanDefinitionRegistryPostProcessor {
            @Throws(BeansException::class)
            override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
                val provider = ClassPathScanningCandidateComponentProvider(false)
                provider.addIncludeFilter(AnnotationTypeFilter(Rule::class.java))

                val nameGenerator = AnnotationBeanNameGenerator()

                for (def in provider.findCandidateComponents("")) {
                    val name = nameGenerator.generateBeanName(def, registry)
                    registry.registerBeanDefinition(name, def)
                }
            }

            @Throws(BeansException::class)
            override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
                // no op
            }
        }
    }
}
