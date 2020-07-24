package org.zalando.zally.core

import com.google.common.io.Resources
import org.zalando.zally.test.ZallyAssertions.assertThat
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
        val jsonSchemaValidator = JsonSchemaValidator(json, mapOf(onlineSchema to localResource))

        val jsonToValidate = ObjectTreeReader().read(
            """
        {
            "firstName": "MyName",
            "lastName": "MyLastName",
            "age": -10
        }
        """
        )

        val valResult = jsonSchemaValidator.validate(jsonToValidate)
        assertThat(valResult.isEmpty()).isFalse()
        assertThat(valResult[0].description).isEqualTo("Numeric instance is lower than the required minimum (minimum: 0, found: -10)")
    }

    @Test
    fun shouldLoadSwaggerSchemaFromResourcesWithRef() {
        val onlineSchema = "http://json-schema.org/draft-04/schema"
        val localResource = Resources.getResource("schemas/json-schema.json").toString()

        val file = "schemas/swagger-schema.json"
        val schemaUrl = Resources.getResource(file)
        val json = ObjectTreeReader().read(schemaUrl)
        var jsonSchemaValidator = JsonSchemaValidator(json, mapOf(
            onlineSchema to localResource,
            "http://swagger.io/v2/schema.json" to schemaUrl.toString()
        ))
        val specJson = ObjectTreeReader().read(Resources.getResource("fixtures/api_tinbox.yaml"))

        val valResult = jsonSchemaValidator.validate(specJson)
        assertThat(valResult.isEmpty()).isFalse()
        assertThat(valResult[0].description).isEqualTo("Instance failed to match at least one required schema among 2")
    }

    @Test
    fun `invalid schemas result in empty violation pointers`() {
        val reader = ObjectTreeReader()

        val ref = "\$ref"
        val json = reader.read("""{ "$ref": "#unresolvable" }""".trimIndent())

        val result = JsonSchemaValidator(json)
            .validate(reader.read("""{ "key": "value" }"""))

        assertThat(result)
            .isNotEmpty
            .pointersAllEqualTo("")
            .descriptionsAllEqualTo("""JSON Reference "#unresolvable" cannot be resolved""")
    }
}
