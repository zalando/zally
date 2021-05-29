package org.zalando.zally.ruleset.zalando.model

import io.swagger.v3.oas.models.info.Info

fun Info.apiAudience(): ApiAudience = this.extensions?.get("x-audience").let { ApiAudience.parse(it.toString()) }
