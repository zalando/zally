package de.zalando.zally.dto;

import org.junit.Test;

import java.util.SortedSet;
import java.util.TreeSet;

import static de.zalando.zally.dto.ViolationType.HINT;
import static de.zalando.zally.dto.ViolationType.MUST;
import static de.zalando.zally.dto.ViolationType.values;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ViolationTypeTest {

    private final SortedSet<ViolationType> sorted = new TreeSet<>(asList(values()));

    @Test
    public void mostSevere() {
        assertEquals(MUST, sorted.first());
    }

    @Test
    public void leastSevere() {
        assertEquals(HINT, sorted.last());
    }
}
