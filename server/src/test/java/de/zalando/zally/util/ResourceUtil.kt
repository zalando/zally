package de.zalando.zally.util

import de.zalando.zally.dto.ApiDefinitionRequest
import org.apache.commons.io.IOUtils
import java.io.IOException

@Throws(IOException::class)
fun resourceToString(resourceName: String): String {
    return IOUtils.toString(ClassLoader.getSystemResourceAsStream(resourceName))
}

@Throws(IOException::class)
fun readApiDefinition(resourceName: String): ApiDefinitionRequest {
    return ApiDefinitionRequest.fromJson(resourceToString(resourceName))
}
