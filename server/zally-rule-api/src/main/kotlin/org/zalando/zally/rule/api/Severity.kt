package org.zalando.zally.rule.api

enum class Severity {
    /*
     * Violation Types must be ordered from most to least severe as the enum ordering is
     * used to sort things elsewhere.
     */
    MUST,
    SHOULD,
    MAY,
    HINT
}
