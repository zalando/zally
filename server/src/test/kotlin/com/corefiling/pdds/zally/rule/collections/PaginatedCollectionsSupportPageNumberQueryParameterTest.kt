package com.corefiling.pdds.zally.rule.collections

import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions
import org.junit.Test

class PaginatedCollectionsSupportPageNumberQueryParameterTest {

    val cut = PaginatedCollectionsSupportPageNumberQueryParameter()

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
                .hasSameElementsAs(listOf("/things GET parameters: does not include a valid pageNumber query parameter"))
    }

    @Test
    fun withMissingPagingParametersIssueOnNonExistentOperation() {
        val yaml = """
            swagger: '2.0'
            info:
              title: An API
              description: Description goes here.
              version: 1.0.0
            paths:
              /bundles:
                post:
                  description: Creates a new bundle.
                  parameters:
                    - name: bundle
                      in: body
                      description: Bundle to create.
                      required: true
                  responses:
                    201:
                      description: Bundle was created
              /bundles/{bundleId}:
                parameters:
                  - ${'$'}ref: '#/parameters/bundleId'
                get:
                  description: Get a single bundle.
                  responses:
                    200:
                      description: Bundle.
            parameters:
              bundleId:
                name: bundleId
                type: string
                format: uuid
                in: path
                description: ID of a bundle.
                required: true
              pageNumber:
                name: pageNumber
                in: query
                type: integer
                format: int32
                minimum: 1
                required: true
                description: Specifies page of results to start from. The first page is numbered 1.
              pageSize:
                name: pageSize
                in: query
                type: integer
                format: int32
                minimum: 1
                description: How many data-sets in one page of results.
            """.trimIndent()
        Assertions.assertThat(cut.validate(SwaggerParser().parse(yaml))).isNull()
    }
}
