package de.zalando.zally.apireview;

import com.fasterxml.jackson.databind.JsonNode;
import de.zalando.zally.rule.AbstractRule;
import de.zalando.zally.rule.ApiValidator;
import de.zalando.zally.rule.CompositeRulesValidator;
import de.zalando.zally.rule.JsonRulesValidator;
import de.zalando.zally.rule.SwaggerRulesValidator;
import de.zalando.zally.rule.TestRuleSet;
import de.zalando.zally.rule.api.Check;
import de.zalando.zally.rule.api.Rule;
import de.zalando.zally.rule.api.Severity;
import de.zalando.zally.rule.api.Violation;
import de.zalando.zally.rule.zalando.InvalidApiSchemaRule;
import io.swagger.models.Swagger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
public class RestApiTestConfiguration {

    @Autowired
    private InvalidApiSchemaRule invalidApiRule;

    @Bean
    @Primary
    @Profile("test")
    public ApiValidator validator() {
        final List<Rule> rules = Arrays.asList(
            new TestCheckApiNameIsPresentJsonRule(),
            new TestCheckApiNameIsPresentRule(),
            new TestAlwaysGiveAHintRule()
        );
        return new CompositeRulesValidator(
                new SwaggerRulesValidator(rules, invalidApiRule),
                new JsonRulesValidator(rules, invalidApiRule));
    }

    /** Rule used for testing */
    @Component
    public static class TestCheckApiNameIsPresentJsonRule extends AbstractRule {

        public TestCheckApiNameIsPresentJsonRule() {
            super(new TestRuleSet());
        }

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

        @Override
        public String getTitle() {
            return "schema";
        }

        @Override
        public String getId() {
            return getClass().getSimpleName();
        }

        @Override
        public Severity getSeverity() {
            return Severity.MUST;
        }
    }

    /** Rule used for testing */
    @Component
    public static class TestCheckApiNameIsPresentRule extends AbstractRule {

        TestCheckApiNameIsPresentRule() {
            super(new TestRuleSet());
        }

        @Check(severity = Severity.MUST)
        public Violation validate(Swagger swagger) {
            if (swagger != null && swagger.getInfo().getTitle().contains("Product Service")) {
                return new Violation("dummy", Collections.emptyList());
            } else {
                return null;
            }
        }

        @Override
        public String getTitle() {
            return "Test Rule";
        }

        @Override
        public String getId() {
            return getClass().getSimpleName();
        }

        @Override
        public Severity getSeverity() {
            return Severity.MUST;
        }
    }

    /** Rule used for testing */
    @Component
    public static class TestAlwaysGiveAHintRule extends AbstractRule {
        public TestAlwaysGiveAHintRule() {
            super(new TestRuleSet());
        }

        @Check(severity = Severity.HINT)
        public Violation validate(Swagger swagger) {
            return new Violation("dummy", Collections.emptyList());
        }

        @Override
        public String getTitle() {
            return "Test Hint Rule";
        }

        @Override
        public String getId() {
            return getClass().getSimpleName();
        }

        @Override
        public Severity getSeverity() {
            return Severity.HINT;
        }
    }
}
