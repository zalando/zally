package de.zalando.zally.util.extensions

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows

typealias OperationTuple = Map.Entry<PathItem.HttpMethod, Operation>

fun OAuthFlows.allFlows(): List<OAuthFlow> =
        listOf(implicit, password, clientCredentials, authorizationCode)
