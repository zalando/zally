package de.zalando.zally.statistic;

import de.zalando.zally.apireview.RuleViolation;
import de.zalando.zally.rule.api.Severity;

public class ViolationStatistic {

    private String name;
    private Severity type;
    private int occurrence;

    ViolationStatistic() {}

    ViolationStatistic(RuleViolation violation, int occurrence) {
        this.name = violation.getName();
        this.type = violation.getType();
        this.occurrence = occurrence;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Severity getType() {
        return type;
    }

    public void setType(Severity type) {
        this.type = type;
    }

    public int getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(int occurrence) {
        this.occurrence = occurrence;
    }
}
