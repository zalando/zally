package de.zalando.zally.rule.zally

import de.zalando.zally.getOpenApiContextFromContent
import de.zalando.zally.getSwaggerContextFromContent
import de.zalando.zally.rule.ZallyAssertions
import de.zalando.zally.testConfig
import org.intellij.lang.annotations.Language
import org.junit.Test

@Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "TooManyFunctions", "StringLiteralDuplication")
class CaseCheckerRuleTest {

    private val cut = CaseCheckerRule(testConfig)

    @Test
    fun `checkPropertyNames returns violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            definitions:
              Defined:
                properties:
                  InVaLiD!:
                    type: boolean
            """.trimIndent()
        )

        val violations = cut.checkPropertyNames(context)

        ZallyAssertions
            .assertThat(violations)
            .pointersEqualTo("/definitions/Defined/properties/InVaLiD!")
            .descriptionsAllMatch("Property 'InVaLiD!' does not match .*".toRegex())
    }

    @Test
    fun `checkPathParameterNames with InVaLiD! returns violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            paths:
              /things:
                post:
                  parameters:
                  - in: path
                    name: InVaLiD!
            """.trimIndent()
        )

        val violations = cut.checkPathParameterNames(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Path parameter 'InVaLiD!' does not match .*".toRegex())
            .pointersEqualTo("/paths/~1things/post/parameters/0")
    }

    @Test
    fun `checkPathParameterNames with kebab-case returns no violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            paths:
              /things:
                post:
                  parameters:
                  - in: path
                    name: kebab-case
            """.trimIndent()
        )

        val violations = cut.checkPathParameterNames(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkPathParameterNames with snake_case returns no violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            paths:
              /things:
                post:
                  parameters:
                  - in: path
                    name: snake_case
            """.trimIndent()
        )

        val violations = cut.checkPathParameterNames(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkQueryParameterNames returns violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            paths:
              /things:
                post:
                  parameters:
                  - in: query
                    name: InVaLiD!
            """.trimIndent()
        )

        val violations = cut.checkQueryParameterNames(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Query parameter 'InVaLiD!' does not match .*".toRegex())
            .pointersEqualTo("/paths/~1things/post/parameters/0")
    }

    @Test
    fun `checkHeaderNames returns violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            paths:
              /things:
                post:
                  parameters:
                  - in: header
                    name: InVaLiD!
            """.trimIndent()
        )

        val violations = cut.checkHeaderNames(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Header 'InVaLiD!' does not match .*".toRegex())
            .pointersEqualTo("/paths/~1things/post/parameters/0")
    }

    @Test
    fun `checkPathSegments returns violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            paths:
              /things/{param}//InVaLiD:
                post:
            """.trimIndent()
        )

        val violations = cut.checkPathSegments(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Path segment 'InVaLiD' does not match .*".toRegex())
            .pointersEqualTo("/paths/~1things~1{param}~1~1InVaLiD")
    }

    @Test
    fun `checkDiscriminatorValues with invalid mapping returns violations`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent(
            """
            openapi: '3.0.0'
            components:
              schemas:
                Pet:
                  type: object
                  required:
                  - pet_type
                  properties:
                    pet_type:
                      type: string
                  discriminator:
                    propertyName: pet_type
                    mapping:
                      InVaLiD: Cat
            """.trimIndent()
        )

        val violations = cut.checkDiscriminatorValues(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Discriminator value 'InVaLiD' does not match .*".toRegex())
            .pointersEqualTo("/components/schemas/Pet/discriminator")
    }

    @Test
    fun `checkDiscriminatorValues with invalid enum returns violations`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent(
            """
            openapi: '3.0.0'
            components:
              schemas:
                Pet:
                  type: object
                  required:
                  - pet_type
                  properties:
                    pet_type:
                      type: string
                      enum: [ InVaLiD ]
                  discriminator:
                    propertyName: pet_type
            """.trimIndent()
        )

        val violations = cut.checkDiscriminatorValues(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Discriminator property enum value 'InVaLiD' does not match .*".toRegex())
            .pointersEqualTo("/components/schemas/Pet/properties/pet_type")
    }

    @Test
    fun `checkDiscriminatorValues with invalid swagger enum returns violations`() {
        @Language("YAML")
        val context = getSwaggerContextFromContent(
            """
            swagger: '2.0'
            definitions:
              Pet:
                type: object
                discriminator: petType
                properties:
                  name:
                    type: string
                  petType:
                    type: string
                    enum: [InVaLiD]
                required:
                - name
                - petType
            """.trimIndent()
        )

        val violations = cut.checkDiscriminatorValues(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Discriminator property enum value 'InVaLiD' does not match .*".toRegex())
            .pointersEqualTo("/definitions/Pet/properties/petType")
    }

    @Test
    fun `checkEnumValues with invalid discriminator returns no violations`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent(
            """
            openapi: '3.0.0'
            components:
              schemas:
                Pet:
                  type: object
                  required:
                  - pet_type
                  properties:
                    pet_type:
                      type: string
                      enum: [ InVaLiD ]
                  discriminator:
                    propertyName: pet_type
            """.trimIndent()
        )

        val violations = cut.checkEnumValues(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkEnumValues with invalid enum returns violations`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent(
            """
            openapi: '3.0.0'
            components:
              schemas:
                Pet:
                  type: object
                  properties:
                    huntingSkill:
                      type: string
                      enum:
                      - InVaLiD
            """.trimIndent()
        )

        val violations = cut.checkEnumValues(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Enum value 'InVaLiD' does not match .*".toRegex())
            .pointersEqualTo("/components/schemas/Pet/properties/huntingSkill")
    }

    @Test
    fun `checkEnumValues with invalid parameter enum returns violations`() {
        @Language("YAML")
        val context = getOpenApiContextFromContent(
            """
            openapi: '3.0.0'
            components:
              parameters:
                huntingSkill:
                  name: huntingSkill
                  in: query
                  schema:
                    type: string
                    enum:
                    - InVaLiD
            """.trimIndent()
        )

        val violations = cut.checkEnumValues(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllMatch("Enum value 'InVaLiD' does not match .*".toRegex())
            .pointersEqualTo("/components/parameters/huntingSkill/schema")
    }
}
