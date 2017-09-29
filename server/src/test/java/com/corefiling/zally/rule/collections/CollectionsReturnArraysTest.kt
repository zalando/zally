package com.corefiling.zally.rule.collections

import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CollectionsReturnArraysTest {

    @Test
    fun withEmptyReturnsNull() {
        assertThat(CollectionsReturnArrays().validate(Swagger())).isNull()
    }

    @Test
    fun withAlreadyReturningArrayReturnsNull() {
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
        val swagger = SwaggerParser().parse(yaml)
        val cut = CollectionsReturnArrays()

        assertThat(cut.validate(swagger)).isNull()
    }

    @Test
    fun withNotReturningArrayReturnsResource() {
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
            type: object
  '/things/{id}':
    parameters:
      - name: id
        in: path
        type: string
        required: true
"""
        val swagger = SwaggerParser().parse(yaml)
        val cut = CollectionsReturnArrays()

        assertThat(cut.validate(swagger)!!.paths)
                .hasSameElementsAs(listOf("/things"))
    }
}