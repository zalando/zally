package de.zalando.zally.rule;

import de.zalando.zally.rule.api.Rule;
import de.zalando.zally.rule.api.RuleSet;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.net.URI;

/** RuleSet used to contain test rules */
@Component
public class TestRuleSet implements RuleSet {

    @NotNull
    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @NotNull
    @Override
    public String getTitle() {
        return "Test Rules";
    }

    @NotNull
    @Override
    public URI getUrl() {
        return URI.create("http://test.example.com/");
    }

    @NotNull
    @Override
    public URI url(@NotNull Rule rule) {
        return getUrl().resolve(rule.getId());
    }
}
