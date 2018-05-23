package de.zalando.zally.apireview;

import de.zalando.zally.dto.ApiDefinitionRequest;
import de.zalando.zally.rule.Result;
import de.zalando.zally.rule.TestRuleSet;
import de.zalando.zally.rule.api.Severity;
import de.zalando.zally.rule.api.Rule;
import de.zalando.zally.rule.zalando.UseOpenApiRule;
import de.zalando.zally.util.ResourceUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class ApiReviewTest {

    @Test
    public void shouldAggregateRuleTypeCount() {
        Result mustViolation1 = result(Severity.MUST, "#/pointer1");
        Result mustViolation2 = result(Severity.MUST, "#/pointer2");
        Result shouldViolation = result(Severity.SHOULD, "#/pointer3");

        ApiReview apiReview = new ApiReview(new ApiDefinitionRequest(), null, "", asList(mustViolation1, mustViolation2, shouldViolation));

        assertThat(apiReview.getMustViolations()).isEqualTo(2);
        assertThat(apiReview.getShouldViolations()).isEqualTo(1);
        assertThat(apiReview.getMayViolations()).isEqualTo(0);
        assertThat(apiReview.getHintViolations()).isEqualTo(0);
    }

    @Test
    public void shouldCalculateNumberOfEndpoints() throws IOException {
        Result violation1 = result(Severity.MUST, "#/pointer1");
        Result violation2 = result(Severity.MUST, "#/pointer2");

        String apiDefinition = ResourceUtil.resourceToString("fixtures/limitNumberOfResourcesValid.json");

        ApiReview apiReview = new ApiReview(new ApiDefinitionRequest(), null, apiDefinition, asList(violation1, violation2));

        assertThat(apiReview.getNumberOfEndpoints()).isEqualTo(2);
    }

    @Test
    public void shouldParseApiNameFromApiDefinition() throws IOException {
        String apiDefinition = ResourceUtil.resourceToString("fixtures/limitNumberOfResourcesValid.json");
        ApiReview apiReview = new ApiReview(new ApiDefinitionRequest(), null, apiDefinition, emptyList());
        assertThat(apiReview.getName()).isEqualTo("Test Service");
    }

    @NotNull
    private Result result(Severity severity, String pointer) {
        return new Result(new TestRuleSet(), UseOpenApiRule.class.getAnnotation(Rule.class), "", severity, pointer);
    }
}
