package de.zalando.zally.apireview;

import de.zalando.zally.dto.ApiDefinitionRequest;
import de.zalando.zally.dto.ViolationType;
import de.zalando.zally.rule.Result;
import de.zalando.zally.rule.api.Rule;
import de.zalando.zally.rule.api.RuleSet;
import de.zalando.zally.util.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiReviewTest {

    private Rule dummyRule = new Rule() {

        @NotNull
        @Override
        public RuleSet getRuleSet() {
            return null;
        }

        @NotNull
        @Override
        public String getTitle() {
            return null;
        }

        @NotNull
        @Override
        public String getId() {
            return null;
        }
    };

    @Test
    public void shouldAggregateRuleTypeCount() {
        Result mustViolation1 = new Result(dummyRule, "", "", ViolationType.MUST, Collections.emptyList());
        Result mustViolation2 = new Result(dummyRule, "", "", ViolationType.MUST, Collections.emptyList());
        Result shouldViolation = new Result(dummyRule, "", "", ViolationType.SHOULD, Collections.emptyList());

        ApiReview apiReview = new ApiReview(new ApiDefinitionRequest(), "", asList(mustViolation1, mustViolation2, shouldViolation));

        assertThat(apiReview.getMustViolations()).isEqualTo(2);
        assertThat(apiReview.getShouldViolations()).isEqualTo(1);
        assertThat(apiReview.getMayViolations()).isEqualTo(0);
        assertThat(apiReview.getHintViolations()).isEqualTo(0);
    }

    @Test
    public void shouldCalculateNumberOfEndpoints() throws IOException {
        Result violation1 = new Result(dummyRule, "", "", ViolationType.MUST, asList("1", "2"));
        Result violation2 = new Result(dummyRule, "", "", ViolationType.MUST, asList("3"));

        String apiDefinition = ResourceUtil.resourceToString("fixtures/limitNumberOfResourcesValid.json");

        ApiReview apiReview = new ApiReview(new ApiDefinitionRequest(), apiDefinition, asList(violation1, violation2));

        assertThat(apiReview.getNumberOfEndpoints()).isEqualTo(2);
    }

    @Test
    public void shouldParseApiNameFromApiDefinition() throws IOException {
        String apiDefinition = ResourceUtil.resourceToString("fixtures/limitNumberOfResourcesValid.json");
        ApiReview apiReview = new ApiReview(new ApiDefinitionRequest(), apiDefinition, Collections.emptyList());
        assertThat(apiReview.getName()).isEqualTo("Test Service");
    }
}
