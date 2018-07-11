package de.zalando.zally.rule.api

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
annotation class Check(
    val severity: Severity
)
