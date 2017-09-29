package de.zalando.zally.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("${zally.scanPackages:de.zalando.zally.rule}")
public class RulesConfigConfiguration {

    @Value("${rules-config-path}")
    private String rulesConfigPath;

    @Bean
    public Config createRulesConfig() {
        return ConfigFactory.load(rulesConfigPath);
    }
}
