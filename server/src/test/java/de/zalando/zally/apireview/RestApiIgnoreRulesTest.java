package de.zalando.zally.apireview;

import de.zalando.zally.dto.ApiDefinitionResponse;
import de.zalando.zally.dto.ViolationDTO;
import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static de.zalando.zally.util.ResourceUtil.readApiDefinition;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "zally.ignoreRules=H999")
public class RestApiIgnoreRulesTest extends RestApiBaseTest {

    @Test
    public void shouldIgnoreWithProperty() throws IOException {
        ApiDefinitionResponse response = sendApiDefinition(readApiDefinition("fixtures/api_spp.json"));

        List<ViolationDTO> violations = response.getViolations();
        assertThat(violations)
                .extracting("description","paths")
                .containsExactly(
                        new Tuple("dummy1", emptyList()),
                        new Tuple("schema incorrect", emptyList())
                );

        Map<String, Integer> count = response.getViolationsCount();
        assertThat(count.get("must")).isEqualTo(2);
        assertThat(count.get("should")).isEqualTo(0);
        assertThat(count.get("may")).isEqualTo(0);
        assertThat(count.get("hint")).isEqualTo(0);
    }

    @Test
    public void testIgnoreAllActiveWithVendorExtension() throws IOException {
        ApiDefinitionResponse response = sendApiDefinition(readApiDefinition("fixtures/api_spp_ignored_rules.json"));
        assertThat(response.getViolations()).isEmpty();
    }
}
