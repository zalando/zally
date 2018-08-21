package de.zalando.zally.util

import de.zalando.zally.dto.ApiDefinitionRequest
import org.apache.commons.io.IOUtils

fun resourceToString(resourceName: String): String {
    return IOUtils.toString(ClassLoader.getSystemResourceAsStream(resourceName))
}

fun readApiDefinition(resourceName: String): ApiDefinitionRequest {
    return ApiDefinitionRequest.fromJson(resourceToString(resourceName))
}
