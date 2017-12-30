package de.zalando.zally.configuration;

import de.zalando.zally.rule.RuleDetails;
import de.zalando.zally.rule.RulesManager;
import de.zalando.zally.rule.api.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Configuration
public class RulesManagerConfiguration {

    @Autowired
    private List<Rule> rules;

    @Bean
    public RulesManager rulesManager() {

        final List<RuleDetails> details = rules.stream()
                .map(instance -> new RuleDetails(instance.getRuleSet(), instance))
                .collect(toList());
        return new RulesManager(details);
    }
}
