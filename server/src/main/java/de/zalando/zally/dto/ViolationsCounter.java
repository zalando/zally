package de.zalando.zally.dto;

import de.zalando.zally.rule.Result;
import de.zalando.zally.rule.api.Severity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ViolationsCounter {
    private final Map<Severity, Integer> counters;

    public ViolationsCounter(List<Result> violations) {
        counters = violations.
            stream().
            collect(Collectors.groupingBy(Result::getViolationType, Collectors.summingInt(x -> 1)));
    }

    public Integer getCounter(Severity violationType) {
        return counters.getOrDefault(violationType, 0);
    }
}
