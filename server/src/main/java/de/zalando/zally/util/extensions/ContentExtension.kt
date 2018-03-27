package de.zalando.zally.util.extensions

import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.Schema

fun Content.allSchemas(): List<Schema<Any>> = values.map { it -> it.schema }