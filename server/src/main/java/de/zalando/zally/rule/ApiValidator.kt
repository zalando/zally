package de.zalando.zally.rule

import de.zalando.zally.dto.LocationResolver

interface ApiValidator {
    fun validate(swaggerContent: String, ignoreRules: List<String> = emptyList(), locationResolver: LocationResolver = LocationResolver.Empty): List<Violation>
}
