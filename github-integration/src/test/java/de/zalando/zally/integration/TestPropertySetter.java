package de.zalando.zally.integration;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Profile("test")
@Component
public class TestPropertySetter {

    @PostConstruct
    public void setProperty() {
        System.setProperty("OAUTH2_ACCESS_TOKENS", "zally=testaccesstoken");
    }

}