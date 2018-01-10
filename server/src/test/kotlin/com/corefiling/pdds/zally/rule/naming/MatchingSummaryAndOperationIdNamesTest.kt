package com.corefiling.pdds.zally.rule.naming

import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions
import org.junit.Test

class MatchingSummaryAndOperationIdNamesTest {

    val cut = MatchingSummaryAndOperationIdNames()

    @Test
    fun withEmptyReturnsNull() {
        Assertions.assertThat(cut.validate(Swagger())).isNull()
    }

    @Test
    fun withMatchingIdAndSummaryReturnsNull() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                get:
                  operationId: getListOfThings
                  summary: Get list of Things.
                  responses:
                    200:
                      description: Done
            """.trimIndent()
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withMissingIdReturnsViolation() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                get:
                  summary: Get list of Things.
                  responses:
                    200:
                      description: Done
            """.trimIndent()
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("/things GET has no operationId!"))
    }

    @Test
    fun withMissingSummaryReturnsViolation() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                get:
                  operationId: getListOfThings
                  responses:
                    200:
                      description: Done
            """.trimIndent()
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("/things GET has no summary!"))
    }

    @Test
    fun withMismatchingIdAndSummaryReturnsViolation() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                get:
                  operationId: someidorother
                  summary: Do URL magic stuff :)
                  responses:
                    200:
                      description: Done
            """.trimIndent()
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("/things GET has operationId 'someidorother' but expected 'doURLMagicStuff' to match 'Do URL magic stuff :)'"))
    }

    @Test
    fun withMismatchingIdAndSummaryTLRsReturnsViolation() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                get:
                  operationId: someidorother
                  summary: URL (TLR) madness.
                  responses:
                    200:
                      description: Done
            """.trimIndent()
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("/things GET has operationId 'someidorother' but expected 'urlTLRMadness' to match 'URL (TLR) madness.'"))
    }
}
