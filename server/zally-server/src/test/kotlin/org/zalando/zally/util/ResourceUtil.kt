package org.zalando.zally.util

import org.zalando.zally.dto.ApiDefinitionRequest
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset

fun resourceToString(resourceName: String): String {
    return IOUtils.toString(ClassLoader.getSystemResourceAsStream(resourceName), Charset.defaultCharset())
}

fun readApiDefinition(resourceName: String): ApiDefinitionRequest {
    return ApiDefinitionRequest.fromJson(resourceToString(resourceName))
}
