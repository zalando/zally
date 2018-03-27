package de.zalando.zally.configuration;

import de.zalando.zally.rule.RuleDetails;
import de.zalando.zally.rule.RulesManager;
import de.zalando.zally.rule.api.Rule;
import de.zalando.zally.rule.api.RuleSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Configuration
public class RulesManagerConfiguration {

    @Autowired
    private ApplicationContext context;

  @Bean
    public Collection<Object> rules() {
        return context.getBeansWithAnnotation(Rule.class).values();
    }

    @Bean
    public RulesManager rulesManager() {
        final Collection rules = context.getBean("rules", Collection.class);

        final List<RuleDetails> details = ((Collection<?>) rules).stream()
                .map(instance -> {
                    final Rule rule = instance.getClass().getAnnotation(Rule.class);
                    final RuleSet ruleSet = context.getBean(rule.ruleSet());
                    return new RuleDetails(ruleSet, rule, instance);
                })
                .collect(toList());
        return new RulesManager(details);
    }
}
