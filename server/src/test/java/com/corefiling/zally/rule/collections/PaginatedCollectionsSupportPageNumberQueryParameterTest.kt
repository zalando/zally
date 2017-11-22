package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingRuleSet
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions
import org.junit.Test

class PaginatedCollectionsSupportPageNumberQueryParameterTest {

    val cut = PaginatedCollectionsSupportPageNumberQueryParameter(CoreFilingRuleSet())

    @Test
    fun withEmptyReturnsNull() {
        Assertions.assertThat(cut.validate(Swagger())).isNull()
    }

    @Test
    fun withPathParamAlreadyPresentReturnsNull() {
        val yaml = """
swagger: '2.0'
info:
  title: API Title
  version: 1.0.0
paths:
  '/things':
    parameters:
      - in: query
        name: pageNumber
        type: integer
        format: int32
        minimum: 1
        required: true
        description: The number of items to return per call
    get:
      responses:
        '200':
          description: describe me
          schema:
            type: array
            items:
              type: string
"""
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withOperationParamAlreadyPresentReturnsNull() {
        val yaml = """
swagger: '2.0'
info:
  title: API Title
  version: 1.0.0
paths:
  '/things':
    get:
      parameters:
        - in: query
          name: pageNumber
          type: integer
          format: int32
          minimum: 1
          required: true
          description: The number of items to return per call
      responses:
        '200':
          description: describe me
          schema:
            type: array
            items:
              type: string
"""
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withReferencedParamAlreadyPresentReturnsNull() {
        val yaml = """
swagger: '2.0'
info:
  title: API Title
  version: 1.0.0
paths:
  '/things':
    get:
      parameters:
        - ${'$'}ref: '#/parameters/PageNumber'
      responses:
        '200':
          description: describe me
          schema:
            type: array
            items:
              type: string
parameters:
  PageNumber:
    in: query
    name: pageNumber
    type: integer
    format: int32
    minimum: 1
    required: true
    description: The number of items to return per call
"""
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }

    @Test
    fun withNotReturningHeaderReturnsResource() {
        val yaml = """
swagger: '2.0'
info:
  title: API Title
  version: 1.0.0
paths:
  '/things':
    get:
      responses:
        '200':
          description: describe me
          schema:
            type: array
            items:
              type: string
"""
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))!!.paths)
                .hasSameElementsAs(listOf("paths /things GET parameters: does not include a valid pageNumber query parameter"))
    }
}