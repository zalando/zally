package de.zalando.zally.dto;

import de.zalando.zally.rule.Result;
import de.zalando.zally.rule.api.Severity;
import de.zalando.zally.rule.api.Rule;
import de.zalando.zally.rule.zalando.AvoidTrailingSlashesRule;
import de.zalando.zally.rule.zalando.ZalandoRuleSet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;


public class ViolationsCounterTest {
    private static void assertCounters(Map<Severity, Integer> expectedCounters, List<Result> violations)
        throws AssertionError {

        final ViolationsCounter counter = new ViolationsCounter(violations);
        for (Severity violationType : expectedCounters.keySet()) {
            assertEquals(expectedCounters.get(violationType), counter.getCounter(violationType));
        }
    }

    @Test
    public void returnsZerosWhenViolationListIsEmpty() {
        final List<Result> violations = new ArrayList<>();
        final Map<Severity, Integer> expectedCounters = new HashMap<>();
        expectedCounters.put(Severity.MUST, 0);
        expectedCounters.put(Severity.SHOULD, 0);
        expectedCounters.put(Severity.MAY, 0);
        expectedCounters.put(Severity.HINT, 0);

        assertCounters(expectedCounters, violations);
    }

    @Test
    public void countsMustViolations() {
        final List<Result> violations = new ArrayList<>();
        IntStream.range(0, 5).forEach(
            (i) -> violations.add(generateViolation(Severity.MUST))
        );
        final Map<Severity, Integer> expectedCounters = new HashMap<>();
        expectedCounters.put(Severity.MUST, 5);
        expectedCounters.put(Severity.SHOULD, 0);
        expectedCounters.put(Severity.MAY, 0);
        expectedCounters.put(Severity.HINT, 0);

        assertCounters(expectedCounters, violations);
    }

    @Test
    public void countsShouldViolations() {
        final List<Result> violations = new ArrayList<>();
        IntStream.range(0, 5).forEach(
            (i) -> violations.add(generateViolation(Severity.SHOULD))
        );
        final Map<Severity, Integer> expectedCounters = new HashMap<>();
        expectedCounters.put(Severity.MUST, 0);
        expectedCounters.put(Severity.SHOULD, 5);
        expectedCounters.put(Severity.MAY, 0);
        expectedCounters.put(Severity.HINT, 0);
        assertCounters(expectedCounters, violations);
    }

    @Test
    public void countsMayViolations() {
        final List<Result> violations = new ArrayList<>();
        IntStream.range(0, 5).forEach(
            (i) -> violations.add(generateViolation(Severity.MAY))
        );
        final Map<Severity, Integer> expectedCounters = new HashMap<>();
        expectedCounters.put(Severity.MUST, 0);
        expectedCounters.put(Severity.SHOULD, 0);
        expectedCounters.put(Severity.MAY, 5);
        expectedCounters.put(Severity.HINT, 0);

        assertCounters(expectedCounters, violations);
    }

    @Test
    public void countsHintViolations() {
        final List<Result> violations = new ArrayList<>();
        IntStream.range(0, 5).forEach(
            (i) -> violations.add(generateViolation(Severity.HINT))
        );
        final Map<Severity, Integer> expectedCounters = new HashMap<>();
        expectedCounters.put(Severity.MUST, 0);
        expectedCounters.put(Severity.SHOULD, 0);
        expectedCounters.put(Severity.MAY, 0);
        expectedCounters.put(Severity.HINT, 5);

        assertCounters(expectedCounters, violations);
    }

    private Result generateViolation(Severity violationType) {
        return new Result(new ZalandoRuleSet(),
                AvoidTrailingSlashesRule.class.getAnnotation(Rule.class),
                "Test Description",
                violationType,
                "#/pointer");
    }
}
