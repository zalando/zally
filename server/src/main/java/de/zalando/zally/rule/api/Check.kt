package de.zalando.zally.rule.api

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Check(
        val severity: Severity
)
