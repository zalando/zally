package com.corefiling.zally.rule.operations

import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions
import org.junit.Test

class PutOperationsRequireBodyParameterTest {

    val cut = PutOperationsRequireBodyParameter()

    @Test
    fun withEmptyReturnsNull() {
        Assertions.assertThat(cut.validate(Swagger())).isNull()
    }

    @Test
    fun withHealthyPutReturnsNull() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                put:
                  parameters:
                    - in: body
                      name: thing
                      required: true
                      schema:
                        type: array
                        items:
                          type: string
                  responses:
                    200:
                      description: Done
            """.trimIndent()
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withMissingPutReturnsViolation() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                put:
                  responses:
                    200:
                      description: Done
            """.trimIndent()

        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("/things PUT body parameter is missing!"))
    }

    @Test
    fun withOptionalPutReturnsViolation() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                put:
                  parameters:
                    - in: body
                      name: thing
                      required: false
                      schema:
                        type: array
                        items:
                          type: string
                  responses:
                    200:
                      description: Done
            """.trimIndent()

        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("/things PUT body parameter 'thing' is optional!"))
    }
}