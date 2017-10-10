package de.zalando.zally.dto;

public enum ViolationType {
    /*
     * Violation Types must be ordered from most to least severe as the enum ordering is
     * used to sort things elsewhere.
     */

    MUST("must"),
    SHOULD("should"),
    MAY("may"),

    /*
     * @deprecated Use MAY instead
     */
    @Deprecated() COULD("could"),
    HINT("hint");

    private final String metricIdentifier;

    ViolationType(String metricIdentifier) {
        this.metricIdentifier = metricIdentifier;
    }
}
