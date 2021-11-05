package org.zalando.zally.task

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.StringReader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

abstract class MediaTypesConfigurationTask : DefaultTask() {

    private val host = "https://www.iana.org/assignments/media-types"
    private val jsonMapper = ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)


    @get:Input
    val mediaTypes: ListProperty<String> = project.objects.listProperty(String::class.java)

    @get:OutputFile
    val outputFile: RegularFileProperty = project.objects.fileProperty()


    @TaskAction
    fun generate() {
        val httpClient = HttpClient.newHttpClient()

        logger.info("Generate media types configuration from IANA media types list ($host)")
        val resultMediaTypes = ArrayList<String>()

        for (mediaType in mediaTypes.get()) {
            logger.debug("Download: ${host}/$mediaType.csv")
            val request = HttpRequest.newBuilder(URI.create("${host}/$mediaType.csv")).GET().build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body()

            var counter = 0;
            StringReader(response).forEachLine {
                if (!isHeader(it)) {
                    try {
                        val registeredMediaType = it.split(",")[1].trim()
                        if (registeredMediaType.isNotBlank()) {
                            resultMediaTypes.add(registeredMediaType)
                            counter++
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to get media type from $it", e)
                    }

                }
            }
            logger.info("Downloaded $counter media types from ${host}/$mediaType.csv")
        }


        jsonMapper.writeValue(outputFile.get().asFile, Configuration(MediaTypesRuleConfiguration(resultMediaTypes)))
    }

    private fun isHeader(str: String) = str == "Name,Template,Reference"
}

class MediaTypesRuleConfiguration(@get:JsonProperty("standard_media_types") val standardMediaTypes: List<String>)

class Configuration(@get:JsonProperty("MediaTypesRule") val mediaTypesRuleConfiguration: MediaTypesRuleConfiguration)

