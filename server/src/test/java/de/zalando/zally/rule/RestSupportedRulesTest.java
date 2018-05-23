package de.zalando.zally.rule;

import de.zalando.zally.apireview.RestApiBaseTest;
import de.zalando.zally.dto.RuleDTO;
import de.zalando.zally.rule.api.Severity;
import de.zalando.zally.util.ErrorResponse;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@TestPropertySource(properties = "zally.ignoreRules=TestCheckAlwaysReport3MustViolations")
public class RestSupportedRulesTest extends RestApiBaseTest {

    private static final List<String> IGNORED_RULES = Collections.singletonList("TestCheckAlwaysReport3MustViolations");

    @Autowired
    private RulesManager implementedRules;

    @Test
    public void testRulesCount() {
        assertThat(getSupportedRules().size()).isEqualTo(implementedRules.size());
    }

    @Test
    public void testRulesOrdered() {
        final List<RuleDTO> rules = getSupportedRules();
        for(int i=1;i<rules.size();++i) {
            final Severity prev = rules.get(i - 1).getType();
            final Severity next = rules.get(i).getType();
            assertTrue("Item #" + i + " is out of order:\n" +
                    rules.stream().map(Object::toString).collect(joining("\n")),
                    prev.compareTo(next)<=0);
        }
    }

    @Test
    public void testRulesFields() {
        for (RuleDTO rule : getSupportedRules()) {
            assertThat(rule.getCode()).isNotEmpty();
            assertThat(rule.getTitle()).isNotEmpty();
            assertThat(rule.getType()).isNotNull();
            assertThat(rule.getUrl()).isNotNull();
        }
    }

    @Test
    public void testIsActiveFlag() {
        for (RuleDTO rule : getSupportedRules()) {
            assertThat(rule.getActive()).isEqualTo(!IGNORED_RULES.contains(rule.getCode()));
        }
    }

    @Test
    public void testFilterByType() {

        int count = 0;
        count += getSupportedRules("MuST", null).size();
        count += getSupportedRules("ShOuLd", null).size();
        count += getSupportedRules("MaY", null).size();
        count += getSupportedRules("HiNt", null).size();

        assertThat(count).isEqualTo(implementedRules.size());
    }

    @Test
    public void testReturnsForUnknownType() {
        ResponseEntity<ErrorResponse> response = getSupportedRules("TOPKEK", null, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo(APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody().getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
        assertThat(response.getBody().getStatus()).isNotEmpty();
        assertThat(response.getBody().getDetail()).isNotEmpty();
    }

    @Test
    public void testFilterByActiveTrue() {
        List<RuleDTO> rules = getSupportedRules(null, true);
        assertThat(rules.size()).isEqualTo(implementedRules.size() - IGNORED_RULES.size());
    }

    @Test
    public void testFilterByActiveFalse() {
        List<RuleDTO> rules = getSupportedRules(null, false);
        assertThat(rules.size()).isEqualTo(IGNORED_RULES.size());
    }

}
