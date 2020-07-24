package org.zalando.zally.core

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jsonschema.cfg.ValidationConfiguration
import com.github.fge.jsonschema.core.exceptions.ProcessingException
import com.github.fge.jsonschema.core.load.configuration.LoadingConfiguration
import com.github.fge.jsonschema.core.load.uri.URITranslatorConfiguration
import com.github.fge.jsonschema.core.report.ProcessingMessage
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.github.fge.jsonschema.messages.JsonSchemaValidationBundle
import com.github.fge.msgsimple.bundle.MessageBundle
import com.github.fge.msgsimple.load.MessageBundles
import com.github.fge.msgsimple.source.PropertiesMessageSource
import org.zalando.zally.rule.api.Violation
import java.io.IOException

class JsonSchemaValidator(val schema: JsonNode, schemaRedirects: Map<String, String> = mapOf()) {

    private object Keywords {
        const val oneOf = "oneOf"
        const val anyOf = "anyOf"
        const val additionalProperties = "additionalProperties"
    }

    private val factory: JsonSchemaFactory

    init {
        factory = createValidatorFactory(schemaRedirects)
    }

    @Throws(ProcessingException::class, IOException::class)
    fun validate(jsonToValidate: JsonNode): List<Violation> = factory
        .validator
        .validateUnchecked(schema, jsonToValidate, true)
        .map(this::toValidationMessage)
        .toList()

    private fun toValidationMessage(processingMessage: ProcessingMessage): Violation {
        val node = processingMessage.asJson()
        val keyword = node.path("keyword").textValue()
        val message = node.path("message").textValue().capitalize()
        val pointer = node.at("/instance/pointer")?.textValue()?.toJsonPointer()
            ?: JsonPointer.empty()

        return when (keyword) {
            Keywords.oneOf, Keywords.anyOf -> createValidationMessageWithSchemaRefs(node, message, pointer, keyword)
            Keywords.additionalProperties -> createValidationMessageWithSchemaPath(node, message, pointer)
            else -> Violation(message, pointer)
        }
    }

    private fun createValidationMessageWithSchemaRefs(
        node: JsonNode,
        message: String,
        pointer: JsonPointer,
        keyword: String
    ): Violation {
        val schemaPath = node.at("/schema/pointer").textValue()
        return if (!schemaPath.isNullOrBlank()) {
            val schemaRefNodes = schema.at("$schemaPath/$keyword")
            val schemaRefs = schemaRefNodes
                .map { it.path("\$ref") }
                .filterNot(JsonNode::isMissingNode)
                .joinToString("; ", transform = JsonNode::textValue)
            Violation(message + schemaRefs, pointer)
        } else {
            Violation(message, pointer)
        }
    }

    private fun createValidationMessageWithSchemaPath(
        node: JsonNode,
        message: String,
        pointer: JsonPointer
    ): Violation {
        val schemaPath = node.at("/schema/pointer").textValue()
        return Violation(message + schemaPath, pointer)
    }

    private fun createValidatorFactory(schemaRedirects: Map<String, String>): JsonSchemaFactory {
        val validationMessages = getValidationMessagesBundle()
        val validationConfiguration = ValidationConfiguration.newBuilder()
            .setValidationMessages(validationMessages)
            .freeze()

        val loadingConfig = createLoadingConfiguration(schemaRedirects)

        return JsonSchemaFactory.newBuilder()
            .setValidationConfiguration(validationConfiguration)
            .setLoadingConfiguration(loadingConfig)
            .freeze()
    }

    private fun createLoadingConfiguration(schemaRedirects: Map<String, String>): LoadingConfiguration? {
        val urlTranslatorConfig = URITranslatorConfiguration.newBuilder()
        schemaRedirects.forEach { (from, to) -> urlTranslatorConfig.addSchemaRedirect(from, to) }

        return LoadingConfiguration.newBuilder()
            .setURITranslatorConfiguration(urlTranslatorConfig.freeze())
            .freeze()
    }

    private fun getValidationMessagesBundle(): MessageBundle {
        val customValidationMessages = PropertiesMessageSource.fromResource("/schema-validation-messages.properties")
        return MessageBundles.getBundle(JsonSchemaValidationBundle::class.java)
            .thaw()
            .appendSource(customValidationMessages)
            .freeze()
    }
}
