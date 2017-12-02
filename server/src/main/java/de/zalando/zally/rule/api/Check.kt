package de.zalando.zally.rule.api

import de.zalando.zally.dto.ViolationType

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Check(
        val severity: ViolationType
)
