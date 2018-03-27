package de.zalando.zally.apireview;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;


import de.zalando.zally.rule.ApiAdapter;
import de.zalando.zally.rule.TestRuleSet;
import de.zalando.zally.rule.api.Check;
import de.zalando.zally.rule.api.Rule;
import de.zalando.zally.rule.api.Severity;
import de.zalando.zally.rule.api.Violation;
import de.zalando.zally.rule.zalando.UseOpenApiRule;

@Configuration
public class RestApiTestConfiguration {

    @Autowired
    private UseOpenApiRule invalidApiRule;

    @Bean
    @Primary
    @Profile("test")
    public Collection<Object> rules() {
        return Arrays.asList(
                new TestCheckApiNameIsPresentJsonRule(),
                new TestCheckApiNameIsPresentRule(),
                new TestAlwaysGiveAHintRule(),
                invalidApiRule
        );
    }

    /** Rule used for testing */
    @Rule(
            ruleSet = TestRuleSet.class,
            id = "TestCheckApiNameIsPresentJsonRule",
            severity = Severity.MUST,
            title = "schema"
    )
    public static class TestCheckApiNameIsPresentJsonRule {

        @Check(severity = Severity.MUST)
        public Iterable<Violation> validate(final JsonNode swagger) {
            JsonNode title = swagger.path("info").path("title");
            if (!title.isMissingNode() && title.textValue().contains("Product Service")) {
                return Arrays.asList(
                        new Violation("schema incorrect", Collections.emptyList()));
            } else {
                return Collections.emptyList();
            }
        }
    }

    /** Rule used for testing */
    @Rule(
            ruleSet = TestRuleSet.class,
            id = "TestCheckApiNameIsPresentRule",
            severity = Severity.MUST,
            title = "Test Rule"
    )
    public static class TestCheckApiNameIsPresentRule {

        @Check(severity = Severity.MUST)
        public Violation validate(ApiAdapter adapter) {
            OpenAPI openAPI = adapter.getOpenAPI();
            if (openAPI.getInfo().getTitle().contains("Product Service")) {
                return new Violation("dummy", Collections.emptyList());
            } else {
                return null;
            }
        }
    }

    /** Rule used for testing */
    @Rule(
            ruleSet = TestRuleSet.class,
            id = "TestAlwaysGiveAHintRule",
            severity = Severity.HINT,
            title = "Test Hint Rule"
    )
    public static class TestAlwaysGiveAHintRule {

        @Check(severity = Severity.HINT)
        public Violation validate(ApiAdapter adapter) {
            return new Violation("dummy", Collections.emptyList());
        }
    }
}
