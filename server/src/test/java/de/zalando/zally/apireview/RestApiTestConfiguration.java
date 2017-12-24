package de.zalando.zally.apireview;

import com.fasterxml.jackson.databind.JsonNode;
import de.zalando.zally.TestRuleSet;
import de.zalando.zally.rule.AbstractRule;
import de.zalando.zally.rule.ApiValidator;
import de.zalando.zally.rule.CompositeRulesValidator;
import de.zalando.zally.rule.JsonRulesValidator;
import de.zalando.zally.rule.SwaggerRulesValidator;
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
        final TestRuleSet ruleSet = new TestRuleSet();
        final List<Rule> rules = Arrays.asList(
            new CheckApiNameIsPresentRule(ruleSet),
            new AlwaysGiveAHintRule(ruleSet),
            new CheckApiNameIsPresentJsonRule(ruleSet)
        );
        return new CompositeRulesValidator(
                new SwaggerRulesValidator(rules, invalidApiRule),
                new JsonRulesValidator(rules, invalidApiRule));
    }

    public static class CheckApiNameIsPresentJsonRule extends AbstractRule {

        public CheckApiNameIsPresentJsonRule(TestRuleSet ruleSet) {
            super(ruleSet);
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
            return "T" + getClass().getSimpleName();
        }

        @Override
        public Severity getSeverity() {
            return Severity.MUST;
        }
    }

    public static class CheckApiNameIsPresentRule extends AbstractRule {

        public CheckApiNameIsPresentRule(TestRuleSet ruleSet) {
            super(ruleSet);
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
            return "T" + getClass().getSimpleName();
        }

        @Override
        public Severity getSeverity() {
            return Severity.MUST;
        }

    }

    public static class AlwaysGiveAHintRule extends AbstractRule {
        public AlwaysGiveAHintRule(TestRuleSet ruleSet) {
            super(ruleSet);
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
            return "T" + getClass().getSimpleName();
        }

        @Override
        public Severity getSeverity() {
            return Severity.HINT;
        }

    }
}
