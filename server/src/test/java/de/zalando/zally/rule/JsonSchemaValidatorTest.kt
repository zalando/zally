package de.zalando.zally.rule

import com.google.common.io.Resources
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JsonSchemaValidatorTest {

    @Test
    fun shouldLoadSchemaFromResourcesWithRef() {
        val onlineSchema = "http://simple-referenced-schema.json/schema"
        val localResource = Resources.getResource("schemas/simple-referenced-schema.json").toString()

        val file = "schemas/simple-schema.json"
        val schemaUrl = Resources.getResource(file)
        val json = ObjectTreeReader().read(schemaUrl)
        val jsonSchemaValidator = JsonSchemaValidator(file, json, mapOf(onlineSchema to localResource))

        val jsonToValidate = ObjectTreeReader().read("""
        {
            "firstName": "MyName",
            "lastName": "MyLastName",
            "age": -10
        }
        """)

        val valResult = jsonSchemaValidator.validate(jsonToValidate)
        assertThat(valResult.isSuccess).isFalse()
        assertThat(valResult.messages[0].message).isEqualTo("numeric instance is lower than the required minimum (minimum: 0, found: -10)")
    }

    @Test
    fun shouldLoadSwaggerSchemaFromResourcesWithRef() {
        val onlineSchema = "http://json-schema.org/draft-04/schema"
        val localResource = Resources.getResource("schemas/json-schema.json").toString()

        val file = "schemas/openapi-2-schema.json"
        val schemaUrl = Resources.getResource(file)
        val json = ObjectTreeReader().read(schemaUrl)
        var jsonSchemaValidator = JsonSchemaValidator(file, json, mapOf(onlineSchema to localResource))

        val specJson = ObjectTreeReader().read(Resources.getResource("fixtures/api_tinbox.yaml"))

        val valResult = jsonSchemaValidator.validate(specJson)
        assertThat(valResult.isSuccess).isFalse()
        assertThat(valResult.messages[0].message).isEqualTo("instance failed to match at least one required schema among 2")
    }
}
