package de.zalando.zally.util.extensions

import io.swagger.v3.oas.models.media.Schema

fun Schema<Any>.isObject(): Boolean = type == "object"

fun Schema<Any>.isRef(): Boolean = `$ref`.orEmpty().isEmpty()
