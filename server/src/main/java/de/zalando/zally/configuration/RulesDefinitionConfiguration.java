package de.zalando.zally.configuration;

import de.zalando.zally.rule.api.Rule;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * Register classes annotated with {@link Rule} as BeanDefinition prior to any Bean instantiation.
 */
@Configuration
public class RulesDefinitionConfiguration {

    @Bean
    public BeanDefinitionRegistryPostProcessor rulesDefinitions() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(@NotNull BeanDefinitionRegistry registry) throws BeansException {
                ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
                provider.addIncludeFilter(new AnnotationTypeFilter(Rule.class));

                BeanNameGenerator nameGenerator = new AnnotationBeanNameGenerator();

                for (BeanDefinition def : provider.findCandidateComponents("")) {
                    String name = nameGenerator.generateBeanName(def, registry);
                    registry.registerBeanDefinition(name, def);
                }
            }

            @Override
            public void postProcessBeanFactory(@NotNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
                // no op
            }
        };
    }

}
