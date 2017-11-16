package de.zalando.zally.apireview;

import com.fasterxml.jackson.databind.JsonNode;
import de.zalando.zally.dto.ViolationType;
import de.zalando.zally.rule.AbstractRule;
import de.zalando.zally.rule.ApiValidator;
import de.zalando.zally.rule.CompositeRulesValidator;
import de.zalando.zally.rule.JsonRulesValidator;
import de.zalando.zally.rule.SwaggerRulesValidator;
import de.zalando.zally.rule.Violation;
import de.zalando.zally.rule.api.Check;
import de.zalando.zally.rule.api.Rule;
import de.zalando.zally.rule.api.RuleSet;
import de.zalando.zally.rule.zalando.InvalidApiSchemaRule;
import de.zalando.zally.rule.zalando.ZalandoRuleSet;
import io.swagger.models.Swagger;
import org.jetbrains.annotations.NotNull;
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
        final List<Rule> rules = Arrays.asList(
            new CheckApiNameIsPresentRule("Product Service"),
            new AlwaysGiveAHintRule()
        );
        return new CompositeRulesValidator(
                new SwaggerRulesValidator(rules, invalidApiRule),
                new JsonRulesValidator(Arrays.asList(new CheckApiNameIsPresentJsonRule(new ZalandoRuleSet())), invalidApiRule));
    }

    public static class CheckApiNameIsPresentJsonRule extends AbstractRule {

        public CheckApiNameIsPresentJsonRule(@NotNull RuleSet ruleSet) {
            super(ruleSet);
        }

        @Check
        public Iterable<Violation> validate(final JsonNode swagger) {
            JsonNode title = swagger.path("info").path("title");
            if (!title.isMissingNode() && title.textValue().contains("Product Service")) {
                return Arrays.asList(
                        new Violation(this, getTitle(), "schema incorrect", getViolationType(), getUrl(), Collections.emptyList()));
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public String getTitle() {
            return "schema";
        }

        @Override
        public ViolationType getViolationType() {
            return ViolationType.MUST;
        }

        @Override
        public String getUrl() {
            return "url";
        }

        @Override
        public String getCode() {
            return "M001";
        }

        @Override
        public String getGuidelinesCode() {
            return "000";
        }
    }

    public static class CheckApiNameIsPresentRule extends AbstractRule {

        private final String apiName;

        CheckApiNameIsPresentRule(String apiName) {
            super(new ZalandoRuleSet());
            this.apiName = apiName;
        }

        @Check
        public Violation validate(Swagger swagger) {
            if (swagger != null && swagger.getInfo().getTitle().contains(apiName)) {
                return new Violation(new CheckApiNameIsPresentRule(null), "dummy1", "dummy", ViolationType.MUST, "dummy", Collections.emptyList());
            } else {
                return null;
            }
        }

        @Override
        public String getUrl() {
            return null;
        }

        @Override
        public ViolationType getViolationType() {
            return ViolationType.MUST;
        }

        @Override
        public String getTitle() {
            return "Test Rule";
        }

        @Override
        public String getCode() {
            return "M999";
        }

        @Override
        public String getGuidelinesCode() {
            return "000";
        }
    }

    public static class AlwaysGiveAHintRule extends AbstractRule {
        public AlwaysGiveAHintRule() {
            super(new ZalandoRuleSet());
        }

        @Check
        public Violation validate(Swagger swagger) {
            return new Violation(
                new AlwaysGiveAHintRule(),
                "dummy2", "dummy", ViolationType.HINT, "dummy", Collections.emptyList());
        }

        @Override
        public String getUrl() {
            return null;
        }

        @Override
        public ViolationType getViolationType() {
            return ViolationType.MUST;
        }

        @Override
        public String getTitle() {
            return "Test Hint Rule";
        }

        @Override
        public String getCode() {
            return "H999";
        }

        @Override
        public String getGuidelinesCode() {
            return "000";
        }
    }
}
