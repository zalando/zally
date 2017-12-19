package com.corefiling.zally.rule.operations

import com.corefiling.zally.rule.CoreFilingRuleSet
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions
import org.junit.Test

class PostResponding200ConsideredSuspiciousTest {

    val cut = PostResponding200ConsideredSuspicious(CoreFilingRuleSet())

    @Test
    fun withEmptyReturnsNull() {
        Assertions.assertThat(cut.validate(Swagger())).isNull()
    }

    @Test
    fun withPost201OnCollectionReturnsNull() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                get:
                  responses:
                    200:
                      description: OK
                      schema:
                        type: array
                        items:
                          type: string
                post:
                  responses:
                    201:
                      description: Created
            """.trimIndent()

        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withPost200OnCollectionReturnsViolation() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/things':
                get:
                  responses:
                    200:
                      description: OK
                      schema:
                        type: array
                        items:
                          type: string
                post:
                  responses:
                    200:
                      description: Created
            """.trimIndent()

        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("/things POST response 200 OK probably should be a 201 Created"))
    }

    @Test
    fun withPost202OnResourceReturnsNull() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/do-some-action':
                post:
                  responses:
                    202:
                      description: Accepted
            """.trimIndent()

        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withPost200OnResourceReturnsViolation() {
        val yaml = """
            swagger: '2.0'
            info:
              title: API Title
              version: 1.0.0
            paths:
              '/do-some-action':
                post:
                  responses:
                    200:
                      description: Accepted
            """.trimIndent()

        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("/do-some-action POST response 200 OK probably should be a 202 Accepted"))
    }
}