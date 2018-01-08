package de.zalando.zally.dto;

import de.zalando.zally.rule.api.Severity;
import org.junit.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static de.zalando.zally.rule.api.Severity.HINT;
import static de.zalando.zally.rule.api.Severity.MUST;
import static de.zalando.zally.rule.api.Severity.values;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ViolationTypeTest {

    private final SortedSet<Severity> sorted = new TreeSet<>(asList(values()));

    @Test
    public void mostSevere() {
        assertEquals(MUST, sorted.first());
    }

    @Test
    public void leastSevere() {
        assertEquals(HINT, sorted.last());
    }
}
