package com.corefiling.zally.rule.resources

import com.corefiling.zally.rule.CoreFilingRuleSet
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions
import org.junit.Test

class CamelCaseParameterNamesTest {

    val cut = CamelCaseParameterNames(CoreFilingRuleSet())

    @Test
    fun withEmptyReturnsNull() {
        Assertions.assertThat(cut.validate(Swagger())).isNull()
    }

    @Test
    fun withNoParametersNull() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                post:
            """.trimIndent()
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withLowerCamelCasePathParametersNull() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things/{aThing}':
                post:
                  parameters:
                    - in: path
                      name: aThing
                      type: string
                      required: true
                  responses:
                    200:
                      description: Done
            """.trimIndent()
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withLowerCamelCaseQueryParametersNull() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                post:
                  parameters:
                    - in: query
                      name: aQuery
                      type: string
                      required: false
                  responses:
                    200:
                      description: Done
            """.trimIndent()
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withInvalidPathParametersReturnsParameterLocations() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things/{a_thing}':
                post:
                  parameters:
                    - in: path
                      name: a_thing
                      type: string
                      required: true
                  responses:
                    200:
                      description: Done
            """.trimIndent()

        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("/things/{a_thing} POST path parameter a_thing is not lowerCamelCase"))
    }

    @Test
    fun withInvalidQueryParametersReturnsParameterLocations() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                post:
                  parameters:
                    - in: query
                      name: A-QUERY
                      type: string
                      required: false
                  responses:
                    200:
                      description: Done
            """.trimIndent()

        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("/things/{a_thing} POST query parameter A-QUERY is not lowerCamelCase"))
    }
}