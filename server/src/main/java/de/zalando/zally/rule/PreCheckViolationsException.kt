package de.zalando.zally.rule

import de.zalando.zally.rule.api.Violation

class PreCheckViolationsException(
    val violations: List<Violation>
) : RuntimeException("Violations were detected before the rules check began.")
