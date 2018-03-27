package de.zalando.zally.util.extensions

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem

fun PathItem.operationsMap(handler: (OperationTuple) -> Boolean): Map<PathItem.HttpMethod, Operation> =
        readOperationsMap().filter(handler)
