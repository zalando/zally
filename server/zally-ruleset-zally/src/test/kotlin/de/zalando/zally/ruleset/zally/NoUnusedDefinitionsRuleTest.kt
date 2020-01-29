package de.zalando.zally.ruleset.zally

import de.zalando.zally.test.ZallyAssertions
import de.zalando.zally.core.ObjectTreeReader
import org.intellij.lang.annotations.Language
import org.junit.Test

class NoUnusedDefinitionsRuleTest {

    private val ref = "\$ref"
    private val rule = NoUnusedDefinitionsRule()
    private val reader = ObjectTreeReader()

    @Test
    fun `checkSwagger with unreferenced definitions returns violations`() {

        @Language("YAML")
        val root = reader.read(
            """
            swagger: '2.0'
            definitions:
              Thing:
                type: object
            """.trimIndent()
        )

        val violations = rule.checkSwagger(root)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllEqualTo("Unused definition")
            .pointersEqualTo("/definitions/Thing")
    }

    @Test
    fun `checkSwagger with referenced definitions returns empty`() {

        @Language("YAML")
        val root = reader.read(
            """
            swagger: '2.0'
            definitions:
              Thing:
                type: object
                properties:
                  parent:
                    $ref: "#/definitions/Thing"
            """.trimIndent()
        )

        val violations = rule.checkSwagger(root)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkSwagger with discriminator enum returns empty`() {

        @Language("YAML")
        val root = reader.read(
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
                    enum: [Cat, Dog]
                required:
                - name
                - petType
              Cat:
                description: A representation of a cat
                allOf:
                - $ref: '#/definitions/Pet'
                - type: object
                  properties:
                    huntingSkill:
                      type: string
                      description: The measured skill for hunting
                      default: lazy
                      enum:
                      - clueless
                      - lazy
                      - adventurous
                      - aggressive
                  required:
                  - huntingSkill
              Dog:
                description: A representation of a dog
                allOf:
                - $ref: '#/definitions/Pet'
                - type: object
                  properties:
                    packSize:
                      type: integer
                      format: int32
                      description: the size of the pack the dog is from
                      default: 0
                      minimum: 0
                  required:
                  - packSize
            """.trimIndent()
        )

        val violations = rule.checkSwagger(root)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkSwagger with unreferenced parameter returns violations`() {

        @Language("YAML")
        val root = reader.read(
            """
            swagger: '2.0'
            parameters:
              Param:
                type: object
            """.trimIndent()
        )

        val violations = rule.checkSwagger(root)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllEqualTo("Unused parameter")
            .pointersEqualTo("/parameters/Param")
    }

    @Test
    fun `checkSwagger with referenced parameter returns no violations`() {

        @Language("YAML")
        val root = reader.read(
            """
            swagger: '2.0'
            parameters:
              Param:
                type: object
            paths:
              /things:
                parameters:
                - $ref: "#/parameters/Param"
            """.trimIndent()
        )

        val violations = rule.checkSwagger(root)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkSwagger with unreferenced response returns violations`() {

        @Language("YAML")
        val root = reader.read(
            """
            swagger: '2.0'
            responses:
              NotFound:
                description: Entity not found.
            """.trimIndent()
        )

        val violations = rule.checkSwagger(root)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllEqualTo("Unused response")
            .pointersEqualTo("/responses/NotFound")
    }

    @Test
    fun `checkSwagger with referenced response returns no violations`() {

        @Language("YAML")
        val root = reader.read(
            """
            swagger: '2.0'
            responses:
              NotFound:
                description: Entity not found.
            paths:
              /things:
                get:
                  responses:
                    404:
                      $ref: "#/responses/NotFound"
            """.trimIndent()
        )

        val violations = rule.checkSwagger(root)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkOpenAPI with unreferenced definitions returns violations`() {

        @Language("YAML")
        val root = reader.read(
            """
            openapi: 3.0.1
            components:
              schemas:
                Thing:
                  type: object
            """.trimIndent()
        )

        val violations = rule.checkOpenAPI(root)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllEqualTo("Unused schema")
            .pointersEqualTo("/components/schemas/Thing")
    }

    @Test
    fun `checkOpenAPI with referenced definitions returns empty`() {

        @Language("YAML")
        val root = reader.read(
            """
            openapi: 3.0.1
            components:
              schemas:
                Thing:
                  type: object
                  properties:
                    parent:
                      $ref: '#/components/schemas/Thing'
            """.trimIndent()
        )

        val violations = rule.checkOpenAPI(root)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkOpenAPI with discriminator enum returns empty`() {

        @Language("YAML")
        val root = reader.read(
            """
            openapi: 3.0.1
            components:
              schemas:
                Cat:
                  description: A representation of a cat
                  allOf:
                  - $ref: '#/components/schemas/Pet'
                  - required:
                    - huntingSkill
                    type: object
                    properties:
                      huntingSkill:
                        type: string
                        description: The measured skill for hunting
                        default: lazy
                        enum:
                        - clueless
                        - lazy
                        - adventurous
                        - aggressive
                Dog:
                  description: A representation of a dog
                  allOf:
                  - $ref: '#/components/schemas/Pet'
                  - required:
                    - packSize
                    type: object
                    properties:
                      packSize:
                        minimum: 0
                        type: integer
                        description: the size of the pack the dog is from
                        format: int32
                        default: 0
                Pet:
                  required:
                  - name
                  - petType
                  type: object
                  properties:
                    name:
                      type: string
                    petType:
                      type: string
                  discriminator:
                    propertyName: petType
                    mapping:
                      Cat: Cat
                      Dog: Dog
            """.trimIndent()
        )

        val violations = rule.checkOpenAPI(root)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkOpenAPI with unreferenced parameter returns violations`() {

        @Language("YAML")
        val root = reader.read(
            """
            openapi: 3.0.1
            components:
              parameters:
                Param:
                  in: header
                  schema:
                    type: object
            """.trimIndent()
        )

        val violations = rule.checkOpenAPI(root)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllEqualTo("Unused parameter")
            .pointersEqualTo("/components/parameters/Param")
    }

    @Test
    fun `checkOpenAPI with referenced parameter returns no violations`() {

        @Language("YAML")
        val root = reader.read(
            """
            openapi: 3.0.1
            paths:
              /things:
                parameters:
                - $ref: '#/components/parameters/Param'
            components:
              parameters:
                Param:
                  in: header
                  schema:
                    type: object
            """.trimIndent()
        )

        val violations = rule.checkOpenAPI(root)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `checkOpenAPI with unreferenced response returns violations`() {

        @Language("YAML")
        val root = reader.read(
            """
            openapi: 3.0.1
            components:
              responses:
                NotFound:
                  description: Entity not found.
            """.trimIndent()
        )

        val violations = rule.checkOpenAPI(root)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsAllEqualTo("Unused response")
            .pointersEqualTo("/components/responses/NotFound")
    }

    @Test
    fun `checkOpenAPI with referenced response returns no violations`() {

        @Language("YAML")
        val root = reader.read(
            """
            openapi: 3.0.1
            paths:
              /things:
                get:
                  responses:
                    404:
                      $ref: '#/components/responses/NotFound'
            components:
              responses:
                NotFound:
                  description: Entity not found.
            """.trimIndent()
        )

        val violations = rule.checkSwagger(root)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }
}
