package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingRuleSet
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions
import org.junit.Test

class PaginatedCollectionsSupportPageSizeQueryParameterTest {

    val cut = PaginatedCollectionsSupportPageSizeQueryParameter(CoreFilingRuleSet())

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
        name: pageSize
        type: integer
        format: int32
        minimum: 1
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
          name: pageSize
          type: integer
          format: int32
          minimum: 1
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
        - ${'$'}ref: '#/parameters/pageSize'
      responses:
        '200':
          description: describe me
          schema:
            type: array
            items:
              type: string
parameters:
  pageSize:
    in: query
    name: pageSize
    type: integer
    format: int32
    minimum: 1
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
                .hasSameElementsAs(listOf("/things GET"))
    }
}