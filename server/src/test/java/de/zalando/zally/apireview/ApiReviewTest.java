package de.zalando.zally.apireview;

import de.zalando.zally.dto.ApiDefinitionRequest;
import de.zalando.zally.rule.Result;
import de.zalando.zally.rule.TestRuleSet;
import de.zalando.zally.rule.api.Severity;
import de.zalando.zally.rule.api.Rule;
import de.zalando.zally.rule.zalando.InvalidApiSchemaRule;
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
        Result mustViolation1 = result(Severity.MUST, emptyList());
        Result mustViolation2 = result(Severity.MUST, emptyList());
        Result shouldViolation = result(Severity.SHOULD, emptyList());

        ApiReview apiReview = new ApiReview(new ApiDefinitionRequest(), "", asList(mustViolation1, mustViolation2, shouldViolation));

        assertThat(apiReview.getMustViolations()).isEqualTo(2);
        assertThat(apiReview.getShouldViolations()).isEqualTo(1);
        assertThat(apiReview.getMayViolations()).isEqualTo(0);
        assertThat(apiReview.getHintViolations()).isEqualTo(0);
    }

    @Test
    public void shouldCalculateNumberOfEndpoints() throws IOException {
        Result violation1 = result(Severity.MUST, asList("1", "2"));
        Result violation2 = result(Severity.MUST, asList("3"));

        String apiDefinition = ResourceUtil.resourceToString("fixtures/limitNumberOfResourcesValid.json");

        ApiReview apiReview = new ApiReview(new ApiDefinitionRequest(), apiDefinition, asList(violation1, violation2));

        assertThat(apiReview.getNumberOfEndpoints()).isEqualTo(2);
    }

    @Test
    public void shouldParseApiNameFromApiDefinition() throws IOException {
        String apiDefinition = ResourceUtil.resourceToString("fixtures/limitNumberOfResourcesValid.json");
        ApiReview apiReview = new ApiReview(new ApiDefinitionRequest(), apiDefinition, emptyList());
        assertThat(apiReview.getName()).isEqualTo("Test Service");
    }

    @NotNull
    private Result result(Severity severity, List<String> paths) {
        return new Result(new TestRuleSet(), InvalidApiSchemaRule.class.getAnnotation(Rule.class), "", severity, paths);
    }
}
