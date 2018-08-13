package de.zalando.zally.apireview;

import de.zalando.zally.dto.ApiDefinitionResponse;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

import static de.zalando.zally.util.ResourceUtilKt.readApiDefinition;
import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "zally.ignoreRules=TestCheckAlwaysReport3MustViolations")
public class RestApiIgnoreRulesTest extends RestApiBaseTest {
    @Test
    public void testShouldNotReportViolationsIfIgnoreIsPresent() throws IOException {
        ApiDefinitionResponse response = sendApiDefinition(readApiDefinition("fixtures/openapi3_petstore_expanded.json"));
        assertThat(response.getViolations()).isEmpty();
    }
}
